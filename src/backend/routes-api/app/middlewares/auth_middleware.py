from starlette.middleware.base import BaseHTTPMiddleware
from fastapi import Request
from fastapi.responses import JSONResponse
from jose import jwt, JWTError
from app.config.settings import settings

class JWTMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        return await call_next(request)
        auth: str = request.headers.get("Authorization")
        if not auth:
            return JSONResponse(status_code=401, content={"detail": "No se proporcionó un token de autorización para el recurso."})


        try:
            payload = jwt.decode(
                auth,
                settings.jwt_key_route_service,
                algorithms=[settings.algorithm]
            )
            request.state.user = payload
        except JWTError:
            return JSONResponse(status_code=401, content={"detail": "Token de autorización del recurso inválido o expirado."})

        response = await call_next(request)
        return response
    
