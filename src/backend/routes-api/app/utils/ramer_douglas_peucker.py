import numpy as np
from numba import njit
from numba.typed import List
from .point_to_segment_geodesic_distance import point_to_segment_geodesic_distance

@njit
def ramer_douglas_peucker(points_vec, epsilon_m):
    """
    Algoritmo Ramer-Douglas-Peucker para simplificación de grafos.

    Parameters
    ----------
    points : np.ndarray
        Array de shape (N, 3) con los puntos en vectores unitarios 3D.
    epsilon_m : float
        Distancia máxima permitida en metros.
    
    Returns
    -------
    np.ndarray
        Máscara booleana de shape (N,), donde True indica que el punto se conserva.
    """
    n = points_vec.shape[0]
    if n < 3:
        keep = np.ones(n, dtype=np.bool_)
        return keep
    
    # Convertir epsilon a radianes
    epsilon = epsilon_m

    # Lista booleana para marcar los puntos a conservar
    keep = np.zeros(n, dtype=np.bool_)
    keep[0] = True
    keep[-1] = True

    # Pila para intervalos [start, end]
    stack = List()
    stack.append((0, n - 1))

    while len(stack) > 0:
        start, end = stack.pop()

        max_dist = -1.0
        index = -1

        V = points_vec[start]
        W = points_vec[end]

        # Buscar el punto más lejano al segmento [V,W]
        for i in range(start + 1, end):
            d = point_to_segment_geodesic_distance(points_vec[i], V, W) * 1000.0  # Convertir a metros
            if d > max_dist:
                max_dist = d
                index = i

        # Si la distancia máxima es mayor que epsilon, dividir intervalo
        if max_dist > epsilon and index != -1:
            keep[index] = True
            stack.append((start, index))
            stack.append((index, end))

    return keep

# Dummy para precompilar funciones
dummy_point = np.array([[0.0, 0.0, 0.0]], dtype=np.float64)
ramer_douglas_peucker(dummy_point, 10)