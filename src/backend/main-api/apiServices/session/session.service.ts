import consts from "../../utils/consts.js";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc.js";
import { signToken, validateToken } from "../../services/token/jwt.js";
import {
    deleteLinkedTokens,
    deleteToken,
    deleteUserSessionTokens,
    getUserToAuthenticate,
    getUserToken,
    storeSessionToken,
} from "./session.repository.js";
import { Session } from "../../types/db.js";
import { verifyPassword } from "../../services/hash/hashPassword.js";
import { withMongoTransaction } from "../../utils/withMongoTransaction.js";
import { signDeviceToken } from "../../services/token/jwt.js";
import crypto from "node:crypto";


dayjs.extend(utc); // Activar UTC plugin

/**
 * Generar un refresh token para el usuario.
 * @param user 
 * @param session 
 * @returns String. Refresh token generado.
 */
const generateRefreshToken = async (
    user: User,
    session: Session,
): Promise<string> => {
    const tokenType = consts.token.refresh;
    const expirationTime = dayjs()
        .utc()
        .add(consts.tokenExpiration.refreshExpirationTime, "second")
        .unix();

    const token = signToken({
        ...user,
        type: tokenType,
        expiration: expirationTime,
    });

    // Save token
    await storeSessionToken({ userId: user.id, token, tokenType, session });

    return token;
};

/**
 * Generar un access token para el usuario.
 * @param user User
 * @param refreshToken String. Refresh token del usuario. Este token se usará para vincular el
 * access token con el refresh token.
 * @param session 
 * @returns String. Access token generado.
 */
const generateAccessToken = async (
    user: User,
    refreshToken: string,
    session: Session = null,
): Promise<string> => {
    const tokenType = consts.token.access;
    const expirationTime = dayjs()
        .utc()
        .add(consts.tokenExpiration.accessExpirationTime, "second")
        .unix();

    const token = signToken({
        ...user,
        type: tokenType,
        expiration: expirationTime,
    });

    // Save token
    await storeSessionToken({ userId: user.id, token, tokenType, parentToken:refreshToken, session });

    return token;
};

/**
 * Permite autenticar un usuario con su email y contraseña.
 * @param email String. Email del usuario a autenticar.
 * @param plainPassword String. Contraseña sin encriptar del usuario.
 * @returns LoginDTO | null. Si el usuario es encontrado y la contraseña es correcta, se devuelve un objeto
 * con los datos del usuario, el refresh token y el access token. Si no se encuentra el usuario o la contraseña
 */
const loginService = async (
    email: string,
    plainPassword: string,
): Promise<LoginDTO | null> => {
    const authResult = await getUserToAuthenticate(email);

    if (!authResult) {
        return null;
    }

    // Verificar password hash
    const isPasswordValid = await verifyPassword(plainPassword, authResult.password);

    if (!isPasswordValid) {
        return null;
    }

    let refreshToken:string = '';
    let accessToken:string = '';

    // Generar tokens
    await withMongoTransaction(async (session: Session) => {
        refreshToken = await generateRefreshToken(authResult.user, session);
        accessToken = await generateAccessToken(authResult.user, refreshToken, session);
    });

    return {
        user: authResult.user,
        refreshToken,
        accessToken,
    }


};

/**
 * Valida un token de sesión y devuelve los datos del usuario si es válido.
 * Si el token no es válido o ha expirado, devuelve null.
 * @param token String. Token de sesión a validar.
 * @param expectedTokenType Number | undefined. Tipo de token esperado (opcional).
 * Si se proporciona, se valida que el tipo de token coincida.
 * @returns User | null. Datos del usuario si el token es válido, null en caso contrario.
 */
const validateSessionTokenService = async (token:string, expectedTokenType?:TokenType): Promise<User | null> => {

    // Validar integridad del token
    const decodedToken = validateToken(token);

    if (!decodedToken) {
        return null;
    }

    const { id: userId, type: tokenType } = decodedToken;

    // Validar tipo de token
    if (expectedTokenType && expectedTokenType !== tokenType) {
        return null;
    }

    // Verificar si el token existe en la base de datos
    const sessionToken = await getUserToken(userId, token, tokenType);

    if (!sessionToken) {
        return null;
    }

    // Si el token es válido, devolver los datos del usuario
    return decodedToken;
}

/**
 * Elimina un token de sesión de la base de datos.
 * @param token String. Token de sesión a eliminar.
 */
const deleteSessionTokenService = async (token: string): Promise<void> => {
    await deleteToken(token);
}

/**
 * Elimina los tokens vinculados a un token de sesión padre.
 * Esto incluye los tokens de acceso vinculados al refresh token.
 * @param parentToken String. Token de sesión padre (refresh token).
 * @param session 
 */
const deleteSessionLinkedTokensService = async (parentToken: string, session: Session = null): Promise<void> => {
    await deleteLinkedTokens(parentToken, session);
}

/**
 * Permite generar un nuevo access token para un usuario a partir de su refresh token.
 * @param user User. Usuario para el cual se generará el nuevo access token.
 * @param refreshToken String. Refresh token del usuario. Este token se usará para vincular el
 * nuevo access token con el refresh token.
 * @returns 
 */
const refreshSessionTokenService = async (user: User, refreshToken:string): Promise<string> => {
    
    let accessToken:string = '';
    await withMongoTransaction(async (session: Session) => {
        // Eliminar tokens vinculados (refresh token)
        await deleteSessionLinkedTokensService(refreshToken, session);

        // Generar nuevo access token
        accessToken = await generateAccessToken(user, refreshToken, session);

    });

    return accessToken;
}

const logoutService = async (userId: string, session: Session = null): Promise<void> => {
    // Eliminar todos los tokens del usuario
    await deleteUserSessionTokens(userId, session);
}

const createDeviceTokenService = async (): Promise<string> => {
    // Generar un ID de dispositivo único
    const deviceId = crypto.randomUUID();

    const deviceToken = signDeviceToken(deviceId);

    return deviceToken;
}

export {
    loginService,
    validateSessionTokenService,
    deleteSessionTokenService,
    deleteSessionLinkedTokensService,
    refreshSessionTokenService,
    logoutService,
    createDeviceTokenService,
};
