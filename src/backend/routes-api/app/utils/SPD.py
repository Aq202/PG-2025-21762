import numpy as np
from numba import njit, prange
from .latlon_to_unit_vector import latlon_to_unit_vector
from .point_to_segment_geodesic_distance import point_to_segment_geodesic_distance

@njit
def min_distance_point_segments(P, seg_start, seg_end):
	"""
	Calcula la distancia mínima de un punto P a un conjunto de segmentos definidos por sus extremos.
	"""
	best = 1e30
	for j in range(seg_start.shape[0]):
		d = point_to_segment_geodesic_distance(P, seg_start[j], seg_end[j])
		if d < best:
			best = d
			
	
	return best

@njit(parallel=True)
def min_distances_points_segments(points, seg_start, seg_end):
	"""
	Obtiene las distancias mínimas de un conjunto de puntos a un conjunto de segmentos.
	"""
	n_points = points.shape[0]
	out = np.empty(n_points, dtype=np.float64)
	for i in prange(n_points):
		out[i] = min_distance_point_segments(points[i], seg_start, seg_end)
	return out

# ------------------------------------------------------------
# SSPD exacto siguiendo Algorithm A1
# ------------------------------------------------------------
def spd(route1_vec, route2_vec):
    """
    Calcula SPD (segment-path-distance) entre dos rutas
    @param route1_vec: np.ndarray. Array de shape (N, 3) con los puntos como vectores unitarios 3D
        de la ruta 1. Se toma cada punto y se compara con la totalidad de la segunda ruta.
    @param route2_vec: np.ndarray. Array de shape (M, 3) con los puntos como vectores unitarios 3D
        de la ruta 2.
    """

    # 4: Construir segmentos consecutivos
    r2_start, r2_end = route2_vec[:-1], route2_vec[1:]
    
    # 5-6: Distancia de cada punto de una ruta a los segmentos de la otra
    d1 = min_distances_points_segments(route1_vec, r2_start, r2_end)

    return d1.mean()

# Dummy para precompilar funciones
dummy_point = np.array([[0.0, 0.0, 0.0]], dtype=np.float64)
spd(dummy_point, dummy_point)