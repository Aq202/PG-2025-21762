from motor.motor_asyncio import AsyncIOMotorClient
from app.config.settings import settings
import logging

client: AsyncIOMotorClient = None
_db = None

async def connect_to_mongo():
    global client, _db
    client = AsyncIOMotorClient(settings.mongo_db_connection_uri)
    _db = client[settings.mongo_db_name]
    logging.info("Conexión a BD mongodb exitosa")

async def close_mongo_connection():
    global client
    client.close()
    logging.info("Conexión a BD mongodb cerrada")

def get_db():
    return _db