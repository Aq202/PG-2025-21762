import numpy as np
from numba import njit

@njit
def latlon_to_unit_vector(lat, lon):
    """
    Convierte lat/lon en radianes a vector unitario 3D sobre la esfera.
    """
    c = np.cos(lat)
    return np.array([c * np.cos(lon), c * np.sin(lon), np.sin(lat)], dtype=np.float64)

def latlon_list_to_unit_vectors(coords):
    """
    Convierte una lista de coordenadas (lat, lon) en grados a un array de vectores unitarios 3D.
    """
    coords = np.radians(np.array(coords, dtype=np.float64)) # Grados a radiantes
    return np.array([latlon_to_unit_vector(lat, lon) for lat, lon in coords])