import UserSchema from "../../db/mongodb/schemas/user.schema.js";
import { Session } from "../../types/db.js";
import { NotFoundError, ValidationError } from "../../utils/customError.js";
import parseObjectId from "../../utils/parseObjectId.js";

const createUser = async ({
    name,
    lastname,
    email,
    role = 0,
    password,
    session,
}: {
    name: string;
    lastname: string;
    email: string;
    role: number;
    password: string;
    session: Session;
}): Promise<User> => {
    try {
        const user = new UserSchema();

        user.name = name?.trim();
        user.lastname = lastname?.trim();
        user.email = email?.trim().toLowerCase();
        user.role = role;
        user.password = password;

        await user.save({ session });
        return {
            id: user._id.toString(),
            name: user.name,
            lastname: user.lastname,
            email: user.email,
            role: user.role,
        };
    } catch (ex: unknown) {
        const err = ex as { code?: number; keyValue?: { email?: string } };
        if (err.code === 11000 && err.keyValue?.email !== undefined) {
            throw new ValidationError("El email ya se encuentra registrado.");
        }
        throw ex;
    }
};

/**
 *
 * @param userId String. ID del usuario a buscar.
 * @param session Session. Sesión de la base de datos.
 * @return Promise<User | null>. Devuelve el usuario encontrado o null si no existe.
 */
const getUserById = async ({
    userId,
    session,
}: {
    userId: string;
    session: Session;
}): Promise<User | null> => {
    const user = await UserSchema.findById(userId).session(session);
    if (!user) {
        return null;
    }
    return {
        id: user._id.toString(),
        name: user.name,
        lastname: user.lastname,
        email: user.email,
        role: user.role,
    };
};

/**
 *
 * @param email String. Email del usuario a buscar.
 * @param session Session. Sesión de la base de datos.
 * @returns Promise<User | null>. Devuelve el usuario encontrado o null si no existe.
 */
const getUserByEmail = async ({
    email,
    session,
}: {
    email: string;
    session: Session;
}): Promise<User | null> => {
    const user = await UserSchema.findOne({
        email: email.trim().toLowerCase(),
    }).session(session);
    if (!user) {
        return null;
    }
    return {
        id: user._id.toString(),
        name: user.name,
        lastname: user.lastname,
        email: user.email,
        role: user.role,
    };
};

/**
 * Modifica el rol de un usuario.
 * @param userId
 * @param role
 * @param session
 * @returns Promise<void>
 * @throws NotFoundError Si el usuario no existe.
 */
const modifyUserRole = async ({
    userId,
    role,
    session,
}: {
    userId: string;
    role: number;
    session: Session;
}): Promise<void> => {
    const user = await UserSchema.findById(userId).session(session);
    if (!user) {
        throw new NotFoundError("Usuario no encontrado.");
    }

    if (role === user.role) {
        return; // No hay cambios en el rol, no es necesario hacer nada.
    }

    user.role = role;
    await user.save({ session });
};

const addUserPermission = async ({
    userId,
    role,
    id,
    session,
}: {
    userId: string;
    role: number;
    id: string;
    session: Session;
}): Promise<void> => {
    const user = await UserSchema.findById(userId).session(session);
    if (!user) {
        throw new NotFoundError("Usuario no encontrado.");
    }

    const normalizedRole = Math.round(role);
    const objectId = parseObjectId(id);

    if (
        user.permissions.some(
            (perm) => perm.id.equals(objectId) && perm.role === normalizedRole,
        )
    ) {
        return; // El permiso ya existe, no es necesario agregarlo.
    }

    user.permissions.push({ role: normalizedRole, id: objectId });
    await user.save({ session });
};

/**
 * Verifica si un usuario tiene un permiso específico.
 * @param userId ID del usuario.
 * @param role Rol del permiso a verificar.
 * @param id ID del permiso a verificar. Consiste en el id de la ruta, agencia a la que pertenece el permiso.
 * @returns Boolean. Devuelve true si el usuario tiene el permiso, false en caso contrario.
 * @throws {InvalidObjectIdError} Si el id o userId proporcionados no son un ObjectId válido.
 */
const verifyUserPermission = async ({
    userId,
    role,
    id,
    session,
}: {
    userId: string;
    role: number;
    id: string;
    session: Session;
}): Promise<boolean> => {
    const parsedUserId = parseObjectId(userId, "userId");
    const parsedId = parseObjectId(id, "permissionId");

    const user = await UserSchema.findById(parsedUserId).session(session);
    if (!user) {
        throw new NotFoundError("Usuario no encontrado.");
    }

    const normalizedRole = Math.round(role);

    return user.permissions.some(
        (perm) => perm.id.equals(parsedId) && perm.role === normalizedRole,
    );
};

/**
 * Retornar la lista de permisos de un usuario.
 * @param userId ID del usuario.
 * @param session Objeto de sesión.
 * @returns Promise<Permission[]> Lista de permisos del usuario.
 * @throws {NotFoundError} Si el usuario no existe.
 */
const getUserPermissions = async ({
    userId,
    session,
}: {
    userId: string;
    session: Session;
}): Promise<Permission[]> => {
    const user = await UserSchema.findById(parseObjectId(userId)).session(
        session,
    );
    if (!user) {
        throw new NotFoundError("Usuario no encontrado.");
    }

    return user.permissions.map((perm) => ({
        role: perm.role,
        id: perm.id.toString(),
    }));
};

export {
    createUser,
    getUserById,
    modifyUserRole,
    getUserByEmail,
    addUserPermission,
    verifyUserPermission,
    getUserPermissions,
};
