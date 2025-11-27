import { AppRequest, AppResponse, AppNext } from "../types/express.js";
import { validateAccessToken } from "../services/token/validateAccessToken.js";
import { UnauthorizedError, ForbiddenError } from "../utils/customError.js";

/**
 * Middleware para validar tokens de acceso y opcionalmente roles.
 *
 * @param roles Roles requeridos o null para omitir validación.
 * @param errorMessage Mensaje si el usuario no tiene permisos.
 */
const ensureAccessTokenAuth =
  (roles: Array<number> | null = null, errorMessage = "") =>
  async (req: AppRequest, res: AppResponse, next: AppNext) => {
    const authHeader = req.headers?.authorization;

    if (!authHeader) {
      res.status(401).json({
        ok: false,
        message: "No se ha especificado el token de autorización.",
      });
      return;
    }

    try {
      const userData = await validateAccessToken(authHeader, roles, errorMessage);
      req.session = userData;
      next();
    } catch (error: unknown) {
      let status = 500;
      let message = "Ocurrió un error al validar access token.";

      if (error instanceof UnauthorizedError) {
        status = 401;
        message = error.message;
      } else if (error instanceof ForbiddenError) {
        status = 403;
        message = error.message;
      } else if (error instanceof Error) {
        // Fallback para errores no tipados personalizados
        message = error.message;
      }

      const response = {
        ok: false,
        message,
      };

      res.statusMessage = message;
      res.status(status).json(response);
    }
  };

export default ensureAccessTokenAuth;
