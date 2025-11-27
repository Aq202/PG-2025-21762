import consts from "../../utils/consts.js";
import { validateSessionTokenService, deleteSessionTokenService } from "../../apiServices/session/session.service.js";
import { ForbiddenError, UnauthorizedError } from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";

/**
 * Valida un access token y opcionalmente los roles. Elimina el token si no es válido.
 *  
 * @param {string} token - El token de acceso a validar.
 * @param {Array<number> | null} roles - Roles requeridos para el acceso. Si es null, no se valida.
 * @param {string} errorMessage - Mensaje de error a lanzar si el usuario no tiene los roles necesarios.
 * @return {Promise<Object>} - Datos del usuario si el token es válido y tiene los roles necesarios.
 * @throws UnauthorizedError si el token no es válido o ha expirado.
 * @throws ForbiddenError si el usuario no tiene los roles necesarios.
 * 
 */
export async function validateAccessToken(
  token: string,
  roles: Array<number> | null = null,
  errorMessage = "No se cuenta con los privilegios necesarios."
) {
  try {

    // Validar que el token sea válido y que exista en la BD
    const userData = await validateSessionTokenService(token, consts.token.access);

    if (!userData) {
      throw new UnauthorizedError("El token de autorización no es válido o ha expirado.");
    }

    // Si se especifican roles, verificar que el usuario tenga al menos uno de ellos
    if (
      roles &&
      Array.isArray(roles) &&
      roles.length > 0 &&
      !roles.includes(userData.role)
    ) {
      throw new ForbiddenError(errorMessage)
    }

    return userData;
  } catch (error) {
    // Token inválido o error en validación, se intenta eliminar el token
    deleteSessionTokenService(token).catch(errorLogger);
    throw error;
  }
}
