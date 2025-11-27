import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    jwt_key_route_service: str
    algorithm: str
    mongo_db_connection_uri: str
    mongo_db_name: str
    class Config:
        env_file = os.getenv("ENV_FILE", ".env.dev")
        env_file_encoding = "utf-8"


settings = Settings()
