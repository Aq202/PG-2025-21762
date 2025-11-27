import SessionSchema from "../../db/mongodb/schemas/session.schema.js";
import UserSchema from "../../db/mongodb/schemas/user.schema.js";
import { Session } from "../../types/db.js";
import consts from "../../utils/consts.js";
import getUTCDate from "../../utils/getUTCDate.js";
import parseObjectId from "../../utils/parseObjectId.js";

const storeSessionToken = async ({
    userId,
    token,
    tokenType,
    parentToken,
    session = null,
}: {
    userId: string;
    token: string;
    tokenType: TokenType;
    parentToken?: string;
    session: Session;
}) => {
    const sessionObj = new SessionSchema();

    sessionObj.userId = parseObjectId(userId, "userId");
    sessionObj.token = token;
    sessionObj.tokenType = tokenType;
    sessionObj.date = getUTCDate(null);
    sessionObj.linkedToken = parentToken || null;

    return sessionObj.save({ session });
};

const getUserToAuthenticate = async (
    email: string,
    session: Session = null,
): Promise<UserWithPassword | null> => {
    const user = await UserSchema.findOne({ email }).session(session);

    if (!user) {
        return null;
    }

    return {
        user: {
            id: user._id.toString(),
            name: user.name,
            lastname: user.lastname,
            email: user.email,
            role: user.role,
        },
        password: user.password,
    };
};

const getUserToken = async (
    userId: string,
    token: string,
    type: number,
    session: Session = null,
): Promise<string | null> => {
    const sessionObj = await SessionSchema.findOne({
        userId: parseObjectId(userId, "userId"),
        token,
        tokenType: type,
    }).session(session);

    if (!sessionObj) {
        return null;
    }

    return sessionObj.token;
};

const deleteToken = async (
    token: string,
    session: Session = null,
): Promise<void> => {
    await SessionSchema.deleteMany({ token }).session(session);
};

const deleteLinkedTokens = async (
    parentToken: string,
    session: Session = null,
): Promise<void> => {
    await SessionSchema.deleteMany({ linkedToken: parentToken }).session(
        session,
    );
};

/**
 * Eliminar todos los tokens de sessión de un usuario.
 * Esto incluye tanto los tokens de acceso como los refresh tokens.
 * @param userId
 * @param session
 */
const deleteUserSessionTokens = async (
    userId: string,
    session: Session = null,
): Promise<void> => {
    await SessionSchema.deleteMany({
        userId: parseObjectId(userId, "userId"),
        tokenType: { $in: [consts.token.access, consts.token.refresh] },
    }).session(session);
};

export {
    storeSessionToken,
    getUserToAuthenticate,
    getUserToken,
    deleteToken,
    deleteLinkedTokens,
    deleteUserSessionTokens,
};
