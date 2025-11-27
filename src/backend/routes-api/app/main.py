from fastapi import FastAPI
from app.routes.index import router as api_router
from app.middlewares.auth_middleware import JWTMiddleware
from contextlib import asynccontextmanager
from app.db.mongodb import connect_to_mongo, close_mongo_connection
import logging

logging.basicConfig(level=logging.INFO)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Código de startup
    await connect_to_mongo()
    yield
    # Código de shutdown
    await close_mongo_connection()

app = FastAPI(title="FastAPI JWT Validation", lifespan=lifespan)

app.add_middleware(JWTMiddleware)

# Todas las rutas protegidas por JWT
for route in api_router.routes:
    route.route_class = JWTMiddleware

app.include_router(api_router)
