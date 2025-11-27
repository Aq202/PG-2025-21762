import { AppRequest, AppResponse, AppNext } from "../types/express.js";
import { validateRefreshToken } from "../services/token/validateRefreshToken.js";
import { UnauthorizedError } from "../utils/customError.js";

/**
 * Middleware para validar refresh token y cargar sesión del usuario.
 */
const ensureRefreshTokenAuth = async (
  req: AppRequest,
  res: AppResponse,
  next: AppNext
) => {
  const authToken = req.headers?.authorization;

  if (!authToken) {
    res.status(401).json({
      ok: false,
      message: "No se ha especificado el refresh token.",
    });
    return;
  }

  try {
    const userData = await validateRefreshToken(authToken);
    req.session = userData;
    next();
  } catch (error: unknown) {
    let status = 500;
    let message = "Error interno del servidor.";

    if (error instanceof UnauthorizedError) {
      status = 401;
      message = error.message;
    } else if (error instanceof Error) {
      message = error.message;
    }

    const response: ApiResponse = {
      ok: false,
      message,
    };

    res.statusMessage = message;
    res.status(status).json(response);
  }
};

export default ensureRefreshTokenAuth;
