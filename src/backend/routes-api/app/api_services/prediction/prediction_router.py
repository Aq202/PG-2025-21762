from fastapi import APIRouter, Request
from .prediction_controller import get_route_prediction_controller, get_route_similarity_metric_controller

router = APIRouter()

@router.get("/{coords}")
async def get_route_prediction(request: Request, coords: str, dest: str | None = None, start: str | None = None, route_id: str = None, prev_prediction: str | None = None):
    return await get_route_prediction_controller(request, coords, dest, start, route_id, prev_prediction)

@router.get("/compare")
async def get_route_comparison(request: Request, route1: str | None = None, route2: str | None = None):
    return await get_route_similarity_metric_controller(request, route1, route2)