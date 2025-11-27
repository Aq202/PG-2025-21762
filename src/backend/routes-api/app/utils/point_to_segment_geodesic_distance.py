import numpy as np
from numba import njit
from app.consts import EARTH_RADIUS_KM


EPS = 1e-12                  # Constante para estabilidad numérica

@njit
def clamp(x, lo, hi):
    """Limita x al rango [lo, hi] para estabilidad en arccos."""
    if x < lo: return lo
    if x > hi: return hi
    return x

@njit
def angle_between(u, v):
    """
    Calcula el ángulo entre vectores unitarios u y v (radianes)
    usando arccos del producto punto con clamp.
    """
    return np.arccos(clamp(np.dot(u, v), -1.0, 1.0))


@njit
def point_to_segment_geodesic_distance(P, V, W):
    """
    Calcula la distancia mínima de un punto P a un arco geodésico V-W
    Entradas: vectores unitarios 3D P, V, W
    Salida: distancia en km
    """

    # 4: normal ← normalize(v.cross(w))
    normal = np.cross(V, W)
    n_norm2 = np.dot(normal, normal)
    if n_norm2 < EPS:
        # Caso degenerado: V y W coinciden o son antipodales
        d1 = angle_between(P, V) * EARTH_RADIUS_KM
        d2 = angle_between(P, W) * EARTH_RADIUS_KM
        return min(d1, d2)
    normal /= np.sqrt(n_norm2)

    # 5: pDotNormal ← P · normal
    p_dot_normal = np.dot(P, normal)

    # 6: projection ← normalize(P - (pDotNormal * normal))
    projection = P - p_dot_normal * normal
    proj_norm2 = np.dot(projection, projection)
    if proj_norm2 < EPS:
        # Proyección degenerada → usar extremos
        d1 = angle_between(P, V) * EARTH_RADIUS_KM
        d2 = angle_between(P, W) * EARTH_RADIUS_KM
        return min(d1, d2)
    projection /= np.sqrt(proj_norm2)

    # 7-9: cross1 ← projection.cross(V), cross2 ← projection.cross(W)
    #      dotOfCrosses ← cross1 · cross2
    cross1 = np.cross(projection, V)
    cross2 = np.cross(projection, W)
    dot_of_crosses = np.dot(cross1, cross2)

    # 10-16: Condición sobre el arco menor
    if dot_of_crosses < 0:
        # Proyección dentro del arco menor: distancia = P - projection
        distance = angle_between(P, projection) * EARTH_RADIUS_KM
    else:
        # Proyección fuera del arco: distancia mínima a extremos
        d1 = angle_between(P, V) * EARTH_RADIUS_KM
        d2 = angle_between(P, W) * EARTH_RADIUS_KM
        distance = min(d1, d2)

    # 17: return distance * radiusOfTheEarth (ya multiplicado)
    return distance