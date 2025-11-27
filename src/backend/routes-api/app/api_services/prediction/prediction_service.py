from .prediction_repository import  get_routes_by_edge_geoindex, get_matched_runs_with_geoindex_coincidences, get_simplified_end_to_end_runs, get_scored_runs, get_simplified_end_to_end_run
from app.utils.dtw_subsequence import dtw_subsequence
from app.utils.latlon_to_unit_vector import latlon_list_to_unit_vectors
from app.utils.ramer_douglas_peucker import ramer_douglas_peucker
from app.consts import  H3_ROUTE_EDGE_RESOLUTION, H3_ROUTE_EDGE_MAX_K_NEIGHBORS, H3_MATCHED_POINT_SEARCH_RESOLUTION, H3_MATCHED_POINT_SEARCH_MAX_K_NEIGHBORS, GEOINDEX_BATCH_SIZE, MAX_PARALLEL_GEOINDEX_BATCHES, MAX_PARALLEL_PREDICTION_BATCHES, COMPARATION_LOW_THRESHOLD, DTW_WINDOW_PERCENTAGE_SHORT_ROUTES, MAX_ROUTE_LAST_SEGMENT_DISTANCE_M, ROUTE_DEVIATION_COMPARISON_THRESHOLD
from app.utils.h3_index import get_h3_cell_with_neighbors, build_cell_to_points_map
from app.utils.generate_current_period_keys import generate_current_period_keys
from app.api_services.simplification.simplification_service import simplify_route_service
from collections import defaultdict
import asyncio
import numpy.typing as npt
import numpy as np
from app.utils.get_route_last_segment import get_route_last_segment

async def get_matched_runs_with_most_coincidences(
    allowed_route_ids: list[str],
    coords: list[tuple[float, float]],
):

    # Crear mapa {geoíndice: conjunto de puntos dentro de la celda y vecinos}
    cell_to_points_map = build_cell_to_points_map(coords, resolution=H3_MATCHED_POINT_SEARCH_RESOLUTION, k_neighbors=H3_MATCHED_POINT_SEARCH_MAX_K_NEIGHBORS)

    geoindex_list = list(cell_to_points_map.keys())

    # Crear batches de geoíndices para limitar tamaño de consultas
    batches = [
        geoindex_list[i : i + GEOINDEX_BATCH_SIZE]
        for i in range(0, len(geoindex_list), GEOINDEX_BATCH_SIZE)
    ]

    # Concurrencia de consultas por batches
    semaphore = asyncio.Semaphore(MAX_PARALLEL_GEOINDEX_BATCHES)
    run_point_matches = defaultdict(set)

    async def process_batch(batch):
        async with semaphore:
            batch_results = await get_matched_runs_with_geoindex_coincidences(
                batch, allowed_route_ids
            )

            # procesar resultados de inmediato (sin guardar intermedios grandes)
            for r in batch_results:
                run_id = r["endToEndRunId"]
                for geoindex in r["matchedGeoIndexes"]:
                    for point in cell_to_points_map.get(geoindex, []):
                        run_point_matches[run_id].add(point)

    # Ejecutar batches concurrentemente
    tasks = [asyncio.create_task(process_batch(batch)) for batch in batches]
    for fut in asyncio.as_completed(tasks):
        await fut  # libera memoria a medida que terminan

    # Conteo final y ordenar
    final_results = [
        {
            "endToEndRunId": run_id,
            "matchedInputPoints": len(points),
        }
        for run_id, points in run_point_matches.items()
    ]

    final_results.sort(key=lambda x: x["matchedInputPoints"], reverse=True)

    complete_result_log = {"type": "complete_matched_runs_with_coincidences", "data": str(final_results)}

    # Retornar solo endToEndRunId los que tengan el mayor número de coincidencias (considerando empates)
    max_matches = final_results[0]["matchedInputPoints"] if final_results else 0
    top_runs_ete_ids = [result["endToEndRunId"] for result in final_results if result["matchedInputPoints"] == max_matches]
    return top_runs_ete_ids, complete_result_log

async def prioritize_runs_by_score(
    route_id: str,
    end_to_end_run_ids: list[str],
):  
    """
    Priorizar una lista de end-to-end runs según su puntuación histórica.
    Devuelve la lista ordenada de end-to-end-run-ids, primero los mejor puntuados.
    Si algunos no tienen puntuación, se añaden al final, manteniedo su orden original.
    @param route_id: ID de la ruta.
    @param end_to_end_run_ids: Lista de end-to-end-run-ids a priorizar.
    @return: Lista ordenada de end-to-end-run-ids.
    """
    score_period_keys = generate_current_period_keys()
    scored_ete_runs = await get_scored_runs(
        route_id, end_to_end_run_ids, score_period_keys, 0
    )
    
    scored_ete_runs_set = set(scored_ete_runs) if scored_ete_runs else set()
    non_scored = [ete_run_id for ete_run_id in end_to_end_run_ids if ete_run_id not in scored_ete_runs_set]

    scored_ete_runs.extend(non_scored)
    return scored_ete_runs

async def make_deep_search_for_similar_routes(
        simplified_coords:list[tuple[float, float]],
        simplified_coords_vec:npt.NDArray[np.float64],
        start_coord: tuple[float, float],
        dest_coord: tuple[float, float],
        route_id: str,
        ete_runs_to_exclude: set[str] = set(),
    ):
    """
    Servicio para obtener la ruta en el histórico que mejor se ajuste a las coordenadas proporcionadas.
    Las coordenadas deben ser un subgrafo Pn de las rutas.
    @return: end-to-end-run-id
    """

    logs = []

    # Si hay una ubicación de destino, obtener rutas que se dirijan hacia un lugar cercano
    if start_coord and dest_coord:
        start_location_geoindexes = get_h3_cell_with_neighbors(start_coord, resolution=H3_ROUTE_EDGE_RESOLUTION, k_neighbors=H3_ROUTE_EDGE_MAX_K_NEIGHBORS)
        end_location_geoindexes = get_h3_cell_with_neighbors(dest_coord, resolution=H3_ROUTE_EDGE_RESOLUTION, k_neighbors=H3_ROUTE_EDGE_MAX_K_NEIGHBORS )
        routes_with_similar_dest_filter = await get_routes_by_edge_geoindex(start_location_geoindexes, end_location_geoindexes)

        logs.append({"type": "filtered_routes_by_destination", "data": str(routes_with_similar_dest_filter)})
    else: 
        raise ValueError("Se requiere una ubicación de inicio y destino para predecir la ruta.")

    # Obtener rutas que contienen más puntos en comun con la ruta de entrada
    ete_runs_id_list, coincidences_log = await get_matched_runs_with_most_coincidences(routes_with_similar_dest_filter, simplified_coords)
    logs.append(coincidences_log)
    logs.append({"type": "matched_runs_with_most_coincidences", "data": str(ete_runs_id_list)})

    # Priorizar rutas por puntuación histórica
    sorted_runs_id_list = await prioritize_runs_by_score(route_id, ete_runs_id_list)
    logs.append({"type": "sorted_runs_by_score", "data": str(sorted_runs_id_list)})
    
    # Crear lotes de comparación
    for i in range(0, len(sorted_runs_id_list), MAX_PARALLEL_PREDICTION_BATCHES):
        batch = sorted_runs_id_list[i:i + MAX_PARALLEL_PREDICTION_BATCHES]

        # Excluir rutas que no deban considerarse
        if len(ete_runs_to_exclude) > 0:
            for ete_run_id in batch:
                if ete_run_id in ete_runs_to_exclude:
                    batch.remove(ete_run_id)

                    logs.append({"type":"excluded_ete_run_from_batch", "data":f"Excluding endToEndRunId: {ete_run_id} from comparison batch."})

        # Obtener puntos simplificados del batch
        simplified_run_points = await get_simplified_end_to_end_runs(batch)

        for end_to_end_run_id in batch:

            route_points = simplified_run_points.get(end_to_end_run_id) # Obtener puntos del viaje ete

            if not route_points:
                continue
            
            route_points_vec = latlon_list_to_unit_vectors(route_points)
            similarity_metric = dtw_subsequence(simplified_coords_vec, route_points_vec)

            logs.append({"type":"route_comparison_metric", "data":f"RouteId: {end_to_end_run_id}, Metric: {similarity_metric}"})    

            # Retornar el primero con métrica menor al umbral
            if similarity_metric < COMPARATION_LOW_THRESHOLD:
                logs.append({"type":"accepted_route_due_to_metric", "data":f"RouteId: {end_to_end_run_id}, Metric: {similarity_metric}"})
                return end_to_end_run_id, similarity_metric, logs
            else:
                logs.append({"type":"rejected_route_due_to_metric", "data":f"RouteId: {end_to_end_run_id}, Metric: {similarity_metric}"})

    # Si no se encontró ninguno bajo el umbral
    logs.append({"type":"no_route_found_below_threshold", "data":"No se encontró una ruta con métrica aceptable."})
    return None, None, logs

async def determine_if_route_deviation_occurred(
    prev_predicted_end_to_end_run_id: str,
    simmplified_coords: list[tuple[float, float]],
    simplified_coords_vec: npt.NDArray[np.float64],
) :

    # Obtener puntos finales de la ruta
    last_segment_points = get_route_last_segment(simmplified_coords, MAX_ROUTE_LAST_SEGMENT_DISTANCE_M)
    last_segment_points_vec = latlon_list_to_unit_vectors(last_segment_points)
    
    # Obtener puntos de la predicción previa
    prev_prediction_points = await get_simplified_end_to_end_run(prev_predicted_end_to_end_run_id)
    prev_prediction_points_vec = latlon_list_to_unit_vectors(prev_prediction_points)

    similarity_metric_short = dtw_subsequence(last_segment_points_vec, prev_prediction_points_vec, window_size_percentage=DTW_WINDOW_PERCENTAGE_SHORT_ROUTES)

    if similarity_metric_short <= ROUTE_DEVIATION_COMPARISON_THRESHOLD:
        # Si el segmento encontrado es similar, hacer comparación completa
        similarity_metric = dtw_subsequence(simplified_coords_vec, prev_prediction_points_vec)
        return similarity_metric > COMPARATION_LOW_THRESHOLD, similarity_metric
    else:
        # Desviación detectada directamente
        return True, similarity_metric_short

async def get_route_prediction_service(
    coords:list[tuple[float, float]], 
    start_coord: tuple[float, float],
    dest_coord: tuple[float, float],
    route_id: str,
    prev_predicted_end_to_end_run_id: str = None
):

    logs = []

    # Simplificar ruta de entrada
    simplified_coords = await simplify_route_service(coords)
    simplified_coords_vec = latlon_list_to_unit_vectors(simplified_coords)

    # Si hay una predicción previa, verificar si hubo desviación
    if prev_predicted_end_to_end_run_id:
        deviation_occurred, deviation_metric = await determine_if_route_deviation_occurred(
            prev_predicted_end_to_end_run_id,
            simplified_coords,
            simplified_coords_vec,
        )
        logs.append({"type":"route_deviation_check", "data":f"DeviationOccurred: {deviation_occurred}, Metric: {deviation_metric}"})

        if not deviation_occurred:
            logs.append({"type":"no_route_deviation_detected", "data":f"Maintaining previous prediction: {prev_predicted_end_to_end_run_id}"})
            return prev_predicted_end_to_end_run_id, None, logs
        else:
            logs.append({"type":"route_deviation_detected", "data":"Proceeding to new route prediction."})

    # Realizar búsqueda profunda de rutas similares
    predicted_end_to_end_run_id, similarity_metric, prediction_logs = await make_deep_search_for_similar_routes(
        simplified_coords,
        simplified_coords_vec,
        start_coord,
        dest_coord,
        route_id,
        ete_runs_to_exclude = {prev_predicted_end_to_end_run_id} if prev_predicted_end_to_end_run_id else set()
    )
    
    logs.extend(prediction_logs)
    return predicted_end_to_end_run_id, similarity_metric, logs


async def compare_routes_service(coords1: list[tuple[float, float]], coords2: list[tuple[float, float]]):
    """
    Servicio para comparar dos rutas usando la métrica DTW.
    @param coords1: Lista de coordenadas (latitud, longitud) que representan la ruta 1.
    @param coords2: Lista de coordenadas (latitud, longitud) que representan la ruta 2.
    @return: Métrica DTW entre las dos rutas.
    """
    
    # Convertir coords a vectores unitarios 3D
    coords1_vec = latlon_list_to_unit_vectors(coords1)
    coords2_vec = latlon_list_to_unit_vectors(coords2)
    
    # Obtener métrica
    similarity_metric = dtw_subsequence(coords1_vec, coords2_vec)
    

    return similarity_metric 