import { hashPassword } from "../../services/hash/hashPassword.js";
import consts from "../../utils/consts.js";
import { addUserPermission, createUser, getUserById, getUserPermissions, modifyUserRole, verifyUserPermission } from "./user.repository.js";
import { Session } from "../../types/db.js";
import { NotFoundError } from "../../utils/customError.js";

const createUserService = async ({
    name,
    lastname,
    email,
    password,
}: {
    name: string;
    lastname: string;
    email: string;
    role?: number;
    password: string;
}): Promise<User> => {
    const passwordHash = await hashPassword(password);

    const user = await createUser({
        name: name?.trim(),
        lastname: lastname?.trim(),
        email: email?.trim().toLowerCase(),
        role: consts.roles.default,
        password: passwordHash,
        session: null,
    });
    return user;
};

/**
 * Asigna el rol de administrador de compañía de transporte al usuario.
 * @param userId ID del usuario al que se le asignará el rol.
 * @param session Sesión de la base de datos.
 * @throws {NotFoundError} Si el usuario no existe.
 * @returns {Promise<void>}
 */
const assignAgencyAdminRoleToUserService = async ({
    userId,
    agencyId,
    session = null,
}: {
    userId: string;
    agencyId: string;
    session: Session;
}): Promise<void> => {
    const role = consts.roles.agencyAdmin;

    const user = await getUserById({ userId, session });
    if (!user) {
        throw new NotFoundError("El usuario no existe.");
    }

    // Cambiar el rol del usuario si es necesario (si no lo tiene o si ya posee un rol superior)
    if(user.role !== role && user.role !== consts.roles.admin) {
        await modifyUserRole({
            userId,
            role,
            session,
        });
    }

    // Asignar permiso como administrador de la compañía de transporte
    await addUserPermission({
        userId,
        role: role,
        id: agencyId,
        session,
    })

};

/**
 * Verifica si un usuario tiene un permiso específico.
 * @param userId ID del usuario.
 * @param role Rol del permiso a verificar.
 * @param id ID del permiso a verificar. Consiste en el id de la ruta, agencia a la que pertenece el permiso.
 * @returns Boolean. Devuelve true si el usuario tiene el permiso, false en caso contrario.
 * @throws {InvalidObjectIdError} Si el id o userId proporcionados no son un ObjectId válido.
 */
const verifyUserPermissionService = async ({
    userId,
    id,
    role,
    session = null,
}: {
    userId: string;
    id: string;
    role: number;
    session?: Session;
}): Promise<boolean> => {
    return verifyUserPermission({
        userId,
        role,
        id,
        session,
    })
}

/**
 * Retornar la lista de permisos de un usuario.
 * @param userId ID del usuario.
 * @param session Objeto de sesión.
 * @returns Promise<Permission[]> Lista de permisos del usuario.
 * @throws {NotFoundError} Si el usuario no existe.
 */
const getUserPermissionsService = async ({
    userId,
    session = null,
}: {
    userId: string;
    session?: Session;
}): Promise<Permission[]> => {
    return getUserPermissions({userId, session});
}

export { 
    createUserService,
    assignAgencyAdminRoleToUserService,
    verifyUserPermissionService,
    getUserPermissionsService,
};
