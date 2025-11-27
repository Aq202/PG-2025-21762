from typing import List, Tuple
from app.utils.get_distance_between_geo_points import get_distance_between_geo_points

def get_route_last_segment(
    route_points: List[Tuple[float, float]],
    max_distance_m: float
) -> List[Tuple[float, float]]:
    """
    Obtiene los puntos del tramo final de una ruta que cubren aproximadamente los últimos 'max_distance_m' metros.

    Args:
        route_points: Lista de tuplas (lat, lon) que representan la ruta en orden.
        max_distance_m: Distancia máxima en metros desde el final hacia atrás.

    Returns:
        Lista de puntos (lat, lon) dentro del rango indicado o None si la distancia total es menor que 'max_distance_m'.
    """
    if len(route_points) < 2:
        return route_points

    selected_points = [route_points[-1]]  # Siempre incluir el último punto
    accumulated_distance = 0.0

    # Recorrer desde el final hacia el inicio
    for i in range(len(route_points) - 1, 0, -1):
        lat1, lon1 = route_points[i]
        lat2, lon2 = route_points[i - 1]
        segment_distance = get_distance_between_geo_points(lat1, lon1, lat2, lon2)
        accumulated_distance += segment_distance
        selected_points.append(route_points[i - 1])
        if accumulated_distance >= max_distance_m:
            break

    # Invertir para mantener el orden original (inicio a fin)
    selected_points.reverse()
    return selected_points if accumulated_distance >= max_distance_m else None
