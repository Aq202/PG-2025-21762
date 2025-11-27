from app.utils.latlon_to_unit_vector import latlon_list_to_unit_vectors
from app.utils.ramer_douglas_peucker import ramer_douglas_peucker
from app.consts import RDP_EPSILON_M

async def simplify_route_service(coords: list[tuple[float, float]], epsilon: float = RDP_EPSILON_M):
    """
    Servicio para simplificar una ruta usando el algoritmo Ramer-Douglas-Peucker.
    @param coords: Lista de coordenadas (latitud, longitud) que representan la ruta.
    @return: Lista de coordenadas simplificadas (latitud, longitud).
    """
    
    # Convertir coords a vectores unitarios 3D
    coords_vec = latlon_list_to_unit_vectors(coords)
    
    # Obtener máscara booleana de puntos a conservar
    mask = ramer_douglas_peucker(coords_vec, epsilon)
    
    # Aplicar máscara a las coordenadas originales
    simplified_coords = [coords[i] for i in range(len(coords)) if mask[i]]
    
    return simplified_coords
