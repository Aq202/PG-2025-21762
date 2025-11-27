import math
from app.consts import EARTH_RADIUS_KM

def get_distance_between_geo_points(lat1: float, long1: float, lat2: float, long2: float) -> float:
    """
    Calcula la distancia en metros entre dos puntos geográficos utilizando la fórmula del haversine.

    :param lat1: Latitud del primer punto.
    :param long1: Longitud del primer punto.
    :param lat2: Latitud del segundo punto.
    :param long2: Longitud del segundo punto.
    :return: Distancia en metros entre los dos puntos.
    """
    R = EARTH_RADIUS_KM * 1000  # Radio de la Tierra en metros

    d_lat = math.radians(lat2 - lat1)
    d_lon = math.radians(long2 - long1)

    a = (math.sin(d_lat / 2) ** 2 +
         math.cos(math.radians(lat1)) *
         math.cos(math.radians(lat2)) *
         math.sin(d_lon / 2) ** 2)

    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    distance = R * c

    return distance
