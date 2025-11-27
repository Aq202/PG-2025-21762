from app.consts import MATCHED_RUN_POINTS_COLLECTION, SEARCH_POINTS_MAX_RADIUS, ROUTE_COLLECTION, SIMPLIFIED_RUN_POINTS_COLLECTION, RUN_SCORES_COLLECTION
from app.db.mongodb import get_db
from collections import defaultdict
from bson import ObjectId
import logging
import time

async def get_routes_by_edge_geoindex(
        start_location_geoindex_list: list[str], end_location_geoindex_list: list[str]):
    """
    Obtener rutas según los geoindexes de inicio y fin.
    @param start_location_geoindex_list: Lista de indices a buscar para la ubicación de inicio.
    @param end_location_geoindex_list: Lista de indices a buscar para la ubicación de fin.
    """
    db = get_db()
    route_collection = db[ROUTE_COLLECTION]

    query = {
        "startLocationGeoIndex": { "$in": start_location_geoindex_list},
        "endLocationGeoIndex": { "$in": end_location_geoindex_list}
    }

    results = await route_collection.find(query, {"_id": 1}).to_list(length=None)
    return [r["_id"] for r in results]

async def get_matched_runs_with_geoindex_coincidences(geoindex_list: list[str], allowed_route_ids: list[str]):
    db = get_db()
    collection = db[MATCHED_RUN_POINTS_COLLECTION]

    allowed_route_ids_obj = [ObjectId(r) for r in allowed_route_ids]

    start_time = time.perf_counter()

    pipeline = [
        {
            "$match": {
                "routeId": {"$in": allowed_route_ids_obj},
                "geoIndex": {"$in": geoindex_list},
            }
        },
        {
            # Agrupar por runId y acumular celdas únicas
            "$group": {
                "_id": "$endToEndRunId",
                "matchedGeoIndexes": {"$addToSet": "$geoIndex"}
            }
        },
        {
            "$project": {
                "_id": 0,
                "endToEndRunId": "$_id",
                "matchedGeoIndexes": 1
            }
        }
    ]

    results = await collection.aggregate(pipeline).to_list(length=None)

    elapsed = (time.perf_counter() - start_time) * 1000  # ms
    logging.info(f"get_matched_runs_with_geoindex_coincidences() ejecutado en {elapsed:.2f} ms. "
                f"Documentos devueltos: {len(results)}")

    return results


async def get_simplified_end_to_end_runs(end_to_end_run_ids: list[str]):
    """
    Obtener el listado de puntos de rutas simplificados para una serie de end-to-end runs.
    Devuelve un dict {endToEndRunId: [(lat, lon), (lat, lon), ...]} para una lista de 
    end_to_end_run_ids, ordenados por seq.
    """
    db = get_db()
    collection = db[SIMPLIFIED_RUN_POINTS_COLLECTION]


    cursor = collection.find(
        {"endToEndRunId": {"$in": end_to_end_run_ids}},
        { "location": 1 , "seq": 1, "endToEndRunId": 1, "_id": 0}
    ).sort([("endToEndRunId", 1), ("seq", 1)])  # Orden por ID y luego por seq

    docs = await cursor.to_list(length=None)

    result_dict = defaultdict(list)
    for doc in docs:
        run_id = doc["endToEndRunId"]
        lon, lat = doc["location"]
        result_dict[run_id].append((lat, lon))

    return dict(result_dict)

async def get_simplified_end_to_end_run(end_to_end_run_id: str):
    """
    Obtener el listado de puntos de rutas simplificados para un solo end-to-end run.
    Devuelve una lista de puntos [(lat, lon), (lat, lon), ...] para el end_to_end_run_id, 
    ordenados por seq.
    """
    db = get_db()
    collection = db[SIMPLIFIED_RUN_POINTS_COLLECTION]

    cursor = collection.find(
        {"endToEndRunId": end_to_end_run_id},
        { "location": 1 , "seq": 1, "endToEndRunId": 1, "_id": 0}
    ).sort([("seq", 1)])

    docs = await cursor.to_list(length=None)

    result = [(doc["location"][1], doc["location"][0]) for doc in docs]

    return result


async def get_scored_runs(route_id, end_to_end_run_ids, keys, min_score):

    # Convertir route_id a ObjectId
    route_id_obj = ObjectId(route_id)

    db = get_db()
    collection = db[RUN_SCORES_COLLECTION]
    match_conditions = [
        {"period.type": "day", "period.key": keys["day"]},
        {"period.type": "week", "period.key": keys["week"]},
        {"period.type": "month", "period.key": keys["month"]},
        {"period.type": "total"}
    ]

    pipeline = [
        { "$match": { 
            "routeId": route_id_obj,
            "endToEndRunId": {"$in": end_to_end_run_ids},
            "$or": match_conditions,
            "score": {"$gte": min_score}
        }},
        { "$sort": { "priority": 1, "score": -1, "updatedAt": -1 }},
        { "$group": {
            "_id": "$endToEndRunId",
            "endToEndRunId": { "$first": "$endToEndRunId" }
        }},
        { "$replaceRoot": { "newRoot": "$$ROOT" }}
    ]


    result = await collection.aggregate(pipeline, allowDiskUse=True).to_list(length=None)

    return [r["endToEndRunId"] for r in result]

