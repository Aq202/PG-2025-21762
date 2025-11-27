import numpy as np
from numba import njit
from .point_to_segment_geodesic_distance import point_to_segment_geodesic_distance
from app.consts import DTW_WINDOW_PERCENTAGE

@njit
def _smooth_cost(cost):
    """
    Suaviza el costo para distancias muy pequeñas al segmento, reduciendo
    la sensibilidad al ruido cuando el punto ya está muy cerca.
    """
    threshold = 5e-4
    if cost < threshold:
        # Hace que el costo crezca más lento que lineal cerca de 0
        cost = (cost * cost) / threshold
    return cost


@njit
def dtw_subsequence_with_band(route1_vec, route2_vec, window_size=None):
    """
    Calcula DTW subsequence entre dos secuencias de puntos A y B con una banda de Sakoe-Chiba opcional.
    @param route1_vec (ruta corta): np.ndarray. Array de shape (N, 3) con los puntos como vectores unitarios 3D.
    @param route2_vec (ruta larga): np.ndarray. Array de shape (M, 3) con los puntos como vectores unitarios 3D.
    """
    N = route1_vec.shape[0]
    M = route2_vec.shape[0]

    if window_size is None:
        window_size = max(N, M)  # sin banda

    prev = np.full(M, np.inf, np.float64)
    curr = np.full(M, np.inf, np.float64)

    # Primera fila
    max_m0 = min(M - 1, window_size)
    
    for m in range(max_m0):
        cost = point_to_segment_geodesic_distance(route1_vec[0], route2_vec[m], route2_vec[m + 1])
        cost = _smooth_cost(cost)
        prev[m] = cost

    # Última columna dentro de la banda inicial
    if max_m0 == M - 1:
        cost = point_to_segment_geodesic_distance(route1_vec[0], route2_vec[max_m0], route2_vec[max_m0])
        cost = _smooth_cost(cost)
        prev[max_m0] = cost
    else:
        cost = point_to_segment_geodesic_distance(route1_vec[0], route2_vec[max_m0], route2_vec[max_m0 + 1])
        cost = _smooth_cost(cost)
        prev[max_m0] = cost

    # Resto de filas
    for n in range(1, N):
        # limpiar
        curr[:] = np.inf

        # Calcular banda
        m_start = n - window_size
        m_end = n + window_size + 1

        # Recortar por límites
        if m_start < 0:
            m_start = 0
        if m_end > M:  # Si la banda supera el fin de la ruta base (eje: ruta observada larga)
            m_end = M 

        # Si la banda quedó totalmente fuera del rango válido,
        # fuerza alineación contra el último punto (M-1)
        if m_start >= M:
            m_start = M - 1
            m_end = M

        # Esquina izquierda (si está dentro de banda)
        if m_start == 0 and M > 1:
            cost = point_to_segment_geodesic_distance(route1_vec[n], route2_vec[0], route2_vec[1])
            cost = _smooth_cost(cost)
            curr[0] = prev[0] + cost

        # Celdas internas válidas
        m0 = max(1, m_start)
        m1 = min(M, m_end)
        for m in range(m0, m1):
            if m < M - 1:
                cost = point_to_segment_geodesic_distance(route1_vec[n], route2_vec[m], route2_vec[m + 1])
            else:
                cost = point_to_segment_geodesic_distance(route1_vec[n], route2_vec[m], route2_vec[m])
            cost = _smooth_cost(cost)

            curr[m] = cost + min(prev[m - 1], prev[m], curr[m - 1])

        # Última columna dentro de la banda
        last_col = M - 1
        if last_col >= m_start and last_col < m_end:
            cost = point_to_segment_geodesic_distance(route1_vec[n], route2_vec[last_col], route2_vec[last_col])
            cost = _smooth_cost(cost)

            if last_col == 0:  # caso M = 1
                curr[last_col] = prev[last_col] + cost
            else:
                curr[last_col] = cost + min(prev[last_col - 1],
                                            prev[last_col],
                                            curr[last_col - 1])

        # Avanzar fila
        prev, curr = curr, prev

    # Resultado final en la ultima fila
    finite = np.isfinite(prev)
    if np.any(finite):
        return np.min(prev[finite])
    else:
        return 9999.0  # sin alineación válida


@njit
def dtw_subsequence(route1, route2, window_size_percentage=DTW_WINDOW_PERCENTAGE):
    """
    Calcula DTW subsequence (NORMALIZADO) entre dos secuencias de puntos A y B con una banda de Sakoe-Chiba opcional.
    @param route1 (ruta corta): np.ndarray. Array de shape (N, 3) con los puntos como vectores unitarios 3D.
    @param route2 (ruta larga): np.ndarray. Array de shape (M, 3) con los puntos como vectores unitarios 3D.
    """
    N = route1.shape[0]
    if N == 0 or route2.shape[0] == 0:
        return 9999.0

    window_size = int(window_size_percentage * route2.shape[0])
    total_cost = dtw_subsequence_with_band(route1, route2, window_size)
    return total_cost / N

# Dummy para precompilar funciones
dummy_point = np.array([[0.0, 0.0, 0.0]], dtype=np.float64)
dtw_subsequence(dummy_point, dummy_point)