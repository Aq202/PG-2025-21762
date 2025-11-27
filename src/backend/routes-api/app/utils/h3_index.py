import h3
from collections import defaultdict

def get_h3_cell(location: tuple[float, float], resolution: int) -> str:
    """
    Retorna el índice H3 correspondiente a una ubicación dada.
    location: tupla (lat, long)
    resolution: nivel de resolución H3 (0-15)
    """
    lat, lon = location
    return h3.latlng_to_cell(lat, lon, resolution)


def get_h3_cell_with_neighbors(location: tuple[float, float], resolution: int, k_neighbors: int) -> list[str]:
    """
    Retorna el índice H3 central y sus vecinos en un radio k.
    """
    center_cell = get_h3_cell(location, resolution)
    neighbors = h3.grid_disk(center_cell, k_neighbors)
    return [center_cell, *neighbors]

def build_cell_to_points_map(coords, resolution, k_neighbors):
    """
    Construye un mapa {celda H3 o vecina -> set de puntos originales}
    @param coords: lista de tuplas (lat, lon)
    @param resolution: resolución H3 para las celdas base
    @param k_neighbors: número de vecinos a incluir por celda base
    @return: dict {geoíndice: set((lat, lon), ...)}
    """
    # Mapa base {celda base -> puntos dentro de esa celda}
    base_cell_to_points = defaultdict(list)
    for lat, lon in coords:
        base_cell = get_h3_cell(location=(lat, lon), resolution=resolution)
        base_cell_to_points[base_cell].append((lat, lon))

    # Generar vecinos solo una vez por celda base
    cell_to_points_map = defaultdict(set)
    for base_cell, points in base_cell_to_points.items():
        neighbors = h3.grid_disk(base_cell, k_neighbors)
        # Asociar los mismos puntos a la celda base y sus vecinos
        for neighbor in neighbors:
            cell_to_points_map[neighbor].update(points)

    return cell_to_points_map