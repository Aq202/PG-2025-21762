from typing import List, Tuple

def split_routes(route: List[Tuple[float, float]], subroute_size: int) -> List[List[Tuple[float, float]]]:
    """
    Divide una ruta en subrutas de tamaño fijo.

    La última subruta puede tener más de `subroute_size` puntos,
    pero siempre menos de el doble, ya que si sobran puntos (< subroute_size),
    se unen a la subruta anterior.

    Args:
        route (List[Tuple[float, float]]): Lista de coordenadas (lat, lon).
        subroute_size (int): Número máximo de puntos por subruta.

    Returns:
        List[List[Tuple[float, float]]]: Lista de subrutas (cada una es una lista de coordenadas).
    """
    num_points = len(route)
    if num_points == 0:
        return []

    num_subroutes = num_points // subroute_size
    remainder = num_points % subroute_size

    # Crear subrutas base
    subroutes = [
        route[i * subroute_size:(i + 1) * subroute_size]
        for i in range(num_subroutes)
    ]

    # Si sobran puntos, unirlos a la última subruta o crear una nueva
    if remainder > 0:
        if subroutes:
            subroutes[-1].extend(route[-remainder:])
        else:
            subroutes.append(route)

    return subroutes
