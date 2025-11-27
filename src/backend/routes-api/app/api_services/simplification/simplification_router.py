from fastapi import APIRouter, Request
from .simplification_controller import get_route_simplification_controller

router = APIRouter()

@router.get("/{coords}")
async def get_route_simplification(request: Request, coords: str):
    return await get_route_simplification_controller(request, coords)