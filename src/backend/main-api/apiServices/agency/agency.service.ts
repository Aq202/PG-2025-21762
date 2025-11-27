import { createAgency, assignAgencyAdmin, getAgenciesWhereUserIsAdminOrDriver, getAgencyById } from "./agency.repository.js";
import { Session } from "../../types/db.js";
import { withMongoTransaction } from "../../utils/withMongoTransaction.js";
import { assignAgencyAdminRoleToUserService, verifyUserPermissionService } from "../user/user.service.js";
import consts from "../../utils/consts.js";

/**
 * Crea una compañía de transporte.
 * @param name Nombre de la compañía.
 * @param session Sesión de la base de datos.
 * @throws {ValidationError} Si el nombre de la compañía ya está registrado.
 * @returns La compañía de transporte creada.
 */
const createAgencyService = async ({
    name,
    session = null,
}: {
    name: string;
    session: Session;
}): Promise<Agency> => {
    return await createAgency({ name: name.trim(), session });
};

/**
 * Asigna un usuario como administrador de una compañía de transporte.
 * @param agencyId ID de la compañía.
 * @param userId ID del usuario.
 * @param session Sesión de la base de datos.
 * @returns La compañía de transporte actualizada.
 * @throws {NotFoundError} Si el usuario o la compañía no existen.
 * @throws {ValidationError} Si el usuario ya es administrador de la compañía o si se asigna
 * el rol de administrador de comañia a un admin general.
 * @throws {InvalidObjectIdError} Si alguno de los IDs proporcionados no es un ObjectId válido.
 */
const assignAgencyAdminService = async ({
    agencyId,
    userId,
}: {
    agencyId: string;
    userId: string;
}) => {
    await withMongoTransaction(async (session: Session) => {
        // Asignar el rol de administrador de compañía de transporte al usuario
        await assignAgencyAdminRoleToUserService({
            userId,
            agencyId,
            session,
        });

        // Asignar el usuario como administrador de la compañía de transporte
        await assignAgencyAdmin({
            agencyId,
            userId,
            session,
        });
    });
};

/**
 * Retorna la lista de agencias a las que tiene acceso el usuario.
 * @param userId ID del usuario.
 * @param session Objeto de sesión.
 * @return Promise<Agency[]> Lista de agencias a las que tiene acceso el usuario.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getAgenciesWhereUserIsAdminOrDriverService = async ({
    userId,
    session = null,
}: {
    userId: string;
    session?: Session;
}): Promise<Agency[]> => {

    // Si es agencyAdmin o conductor, devolver las agencias a las que pertenece
    return getAgenciesWhereUserIsAdminOrDriver({userId, session});

}

/**
 * Verifica si un usuario es administrador de una agencia.
 * @param userId ID del usuario a verificar.
 * @param agencyId ID de la agencia a verificar.
 * @param session Objeto de sesión del usuario.
 * @returns Verdadero si el usuario es administrador de la agencia, falso en caso contrario.
 * @throws {InvalidObjectIdError} Si alguno de los IDs proporcionados no es un ObjectId válido.
 */
const verifyIfUserIsAgencyAdminService = async ({
    userId,
    agencyId,
    session,
}: {
    userId: string;
    agencyId: string;
    session?: Session;
}) => {
    return verifyUserPermissionService({
        userId,
        id: agencyId,
        role: consts.roles.agencyAdmin,
        session,
    });
};

/**
 * Obtiene una compañía de transporte por su ID.
 * @param agencyId ID de la compañía de transporte.
 * @param session Sesión de la base de datos.
 * @returns La compañía de transporte encontrada o null si no existe.
 */
const getAgencyByIdService = async ({
    agencyId,
    session = null,
}: {
    agencyId: string;
    session?: Session;
}): Promise<Agency | null> => {
    return getAgencyById({ agencyId, session });
};

export { 
    createAgencyService,
    assignAgencyAdminService,
    getAgenciesWhereUserIsAdminOrDriverService,
    verifyIfUserIsAgencyAdminService,
    getAgencyByIdService,
};
