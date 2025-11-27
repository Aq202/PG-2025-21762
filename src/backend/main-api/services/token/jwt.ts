import jwt, { JwtPayload } from "jsonwebtoken";
import config from "config";

const key = config.get("jwtKey") as string;
const keyRouteService = config.get("jwtKeyRouteService") as string;

/**
 * Firma un token JWT con los datos del usuario y el tipo de token.
 * @param param0
 * @returns Token firmado.
 */
const signToken = ({
    id,
    email,
    name,
    lastname,
    role,
    type,
    expiration,
}: User & { type: TokenType; expiration: number }) => {
    return jwt.sign(
        {
            id,
            email,
            name,
            lastname,
            role,
            type,
            exp: expiration,
        },
        key,
    );
};

/**
 * Valida la integridad y validez de un token JWT.
 * @param token
 * @returns Si el token es válido, devuelve un objeto con los datos del usuario y el tipo de token; de lo contrario, devuelve null.
 */
const validateToken = (token: string): (User & { type: TokenType }) | null => {
    try {
        const payload = jwt.verify(token, key) as JwtPayload;

        if (!payload) {
            return null;
        }

        const { id, name, lastname, email, role, type } = payload;
        return { id, name, lastname, email, role, type };
        
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
        return null;
    }
};

const signDeviceToken = (deviceId: DeviceId) => {
    return jwt.sign(
        { deviceId },
        key,
    );
};

const signRouteResourceToken = ({ userId } : { userId: string }) => {
    return jwt.sign(
        { userId },
        keyRouteService,
    );
};

export { 
    signToken,
    validateToken,
    signDeviceToken,
    signRouteResourceToken 
};
