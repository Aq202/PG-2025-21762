import consts from "../../utils/consts.js";
import {
  validateSessionTokenService,
  deleteSessionTokenService,
  deleteSessionLinkedTokensService,
} from "../../apiServices/session/session.service.js";
import { UnauthorizedError } from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";

/**
 * Valida un refresh token y elimina tokens asociados si falla.
 *
 * @param token - Token recibido desde headers.
 * @returns Datos de sesión si el token es válido.
 * @throws UnauthorizedError si el token es inválido o ha expirado.
 */
export async function validateRefreshToken(token: string) {
  try {
    const userData = await validateSessionTokenService(token, consts.token.refresh);

    if (!userData) {
      throw new UnauthorizedError("El refresh token no es válido o ha expirado.");
    }

    return userData;
  } catch (error) {
    // Siempre eliminar el refresh token y los access tokens vinculados
    deleteSessionTokenService(token).catch(errorLogger);
    deleteSessionLinkedTokensService(token).catch(errorLogger);
    throw error;
  }
}
