from fastapi import APIRouter
from app.api_services.prediction import prediction_router
from app.api_services.simplification import simplification_router

router = APIRouter()
router.include_router(prediction_router.router, prefix="/predict", tags=["predict"])
router.include_router(simplification_router.router, prefix="/simplify", tags=["simplify"])