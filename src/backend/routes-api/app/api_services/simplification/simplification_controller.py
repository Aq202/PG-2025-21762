from fastapi import Request, HTTPException
from .simplification_service import  simplify_route_service

async def get_route_simplification_controller(request: Request, coords_param: str):
    # Validar que coords exista y tenga contenido suficiente
    if not coords_param or len(coords_param) < 3:
        raise HTTPException(
            status_code=400,
            detail="Las coordenadas son requeridas"
        )
    
    # Validar formato de coordenadas
    coord_pairs = coords_param.split(";")
    coords = []
    for pair in coord_pairs:
        try:
            lat, lon = pair.split(",")
            coords.append((float(lat), float(lon)))
        except Exception:
            raise HTTPException(
                status_code=422,
                detail="Formato de coordenadas inválido. Debe ser lat,long;lat,long;lat,long"
            )
            

    simplified_route = await simplify_route_service(coords)

    return { "simplified_route": simplified_route }
