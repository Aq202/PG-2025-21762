from fastapi import Request, HTTPException
from bson import ObjectId
from .prediction_service import get_route_prediction_service, compare_routes_service


async def get_route_prediction_controller(request: Request, coords_param: str, destination_param: str, start_param: str, route_id: str, prev_prediction: str | None):
    # Validar que coords exista y tenga contenido suficiente
    if not coords_param or len(coords_param) < 3:
        raise HTTPException(
            status_code=400,
            detail="Las coordenadas son requeridas"
        )
    
    # Validar formato de coordenadas
    coord_pairs = coords_param.split(";")
    coords = []
    for pair in coord_pairs:
        try:
            lat, lon = pair.split(",")
            coords.append((float(lat), float(lon)))
        except Exception:
            raise HTTPException(
                status_code=422,
                detail="Formato de coordenadas inválido. Debe ser lat,long;lat,long;lat,long"
            )
        
    # Validar routeId
    if not route_id:
        raise HTTPException(
            status_code=400,
            detail="El parámetro 'route_id' es requerido"
        )
    elif not ObjectId.is_valid(route_id):
        raise HTTPException(
            status_code=422,
            detail="El parámetro 'route_id' tiene un formato inválido"
        )
            
            
    # Validar formato de destino
    dest_coord = None
    if destination_param:
        try:
            dest_lat, dest_lon = destination_param.split(",")
            dest_coord = (float(dest_lat), float(dest_lon))
        except Exception:
            raise HTTPException(
                status_code=422,
                detail="Formato de destino inválido. Debe ser lat,long"
            )
    else:
        raise HTTPException(
            status_code=400,
            detail="El parámetro 'dest' es requerido"
        )

    # Validar formato de inicio
    start_coord = None
    if start_param:
        try:
            start_lat, start_lon = start_param.split(",")
            start_coord = (float(start_lat), float(start_lon))
        except Exception:
            raise HTTPException(
                status_code=422,
                detail="Formato de inicio inválido. Debe ser lat,long"
            )
    else:
        raise HTTPException(
            status_code=400,
            detail="El parámetro 'start' es requerido"
        )
    

    best_route_fit, metric, logs = await get_route_prediction_service(coords, start_coord, dest_coord, route_id, prev_prediction)

    return { "best_fit": best_route_fit, "metric": metric, "logs": logs }

async def get_route_similarity_metric_controller(request: Request, coords1_param: str, coords2_param: str):
    # Validar que coords exista y tenga contenido suficiente
    if not coords1_param or len(coords1_param) < 3:
        raise HTTPException(
            status_code=400,
            detail="Las coordenadas de la primera ruta son requeridas"
        )
    
    if not coords2_param or len(coords2_param) < 3:
        raise HTTPException(
            status_code=400,
            detail="Las coordenadas de la segunda ruta son requeridas"
        )
    
    # Validar formato de coordenadas en ruta1
    coord_pairs_1 = coords1_param.split(";")
    coords1 = []
    for pair in coord_pairs_1:
        try:
            lat, lon = pair.split(",")
            coords1.append((float(lat), float(lon)))
        except Exception:
            raise HTTPException(
                status_code=422,
                detail="Formato de coordenadas en ruta 1 inválido. Debe ser lat,long;lat,long;lat,long"
            )
            
    # Validar formato de coordenadas en ruta2
    coord_pairs_2 = coords2_param.split(";")
    coords2 = []
    for pair in coord_pairs_2:
        try:
            lat, lon = pair.split(",")
            coords2.append((float(lat), float(lon)))
        except Exception:
            raise HTTPException(
                status_code=422,
                detail="Formato de coordenadas en ruta 2 inválido. Debe ser lat,long;lat,long;lat,long"
            )
        
    metric = await compare_routes_service(coords1, coords2)

    return { "metric": metric }