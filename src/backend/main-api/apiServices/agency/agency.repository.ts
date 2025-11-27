import AgencySchema from "../../db/mongodb/schemas/agency.schema.js";
import { Session } from "../../types/db.js";
import { ValidationError, NotFoundError } from "../../utils/customError.js";
import parseObjectId from "../../utils/parseObjectId.js";

/**
 * Crea una compañía de transporte.
 * @param name Nombre de la compañía de transporte.
 * @param session Sesión de la base de datos.
 * @throws {ValidationError} Si el nombre de la compañía ya está registrado.
 */
const createAgency = async ({
    name,
    session,
}: {
    name: string;
    session: Session;
}): Promise<Agency> => {
    try {
        const agency = new AgencySchema();

        agency.name = name;
        agency.adminsId = [];
        agency.routesId = [];

        await agency.save({ session });
        return {
            id: agency._id.toString(),
            name: agency.name,
        };
    } catch (ex: unknown) {
        const err = ex as { code?: number; keyValue?: { name?: string } };
        if (err.code === 11000 && err.keyValue?.name !== undefined) {
            throw new ValidationError(
                "El nombre de la compañía ya se encuentra registrado.",
            );
        }
        throw ex;
    }
};

/**
 * Permite asignar un usuario como administrador de una compañía de transporte.
 * Aclaración: No se encarga de asignar el rol de administrador al usuario, solo lo agrega a
 * la lista de administradores de la compañía.
 * @param agencyId ID de la compañía de transporte a la que se le asignará el administrador.
 * @param userId ID del usuario que se asignará como administrador.
 * @param session Sesión de la base de datos.
 * @throws {NotFoundError} Si la compañía de transporte no existe.
 * @throws {ValidationError} Si el usuario ya es administrador de la compañía de transporte.
 * @throws {InvalidObjectIdError} Si alguno de los IDs proporcionados no es un ObjectId válido.
 * @returns
 */
const assignAgencyAdmin = async ({
    agencyId,
    userId,
    session,
}: {
    agencyId: string;
    userId: string;
    session: Session;
}) => {
    const parsedAgencyId = parseObjectId(agencyId, "agencyId");
    const parsedUserId = parseObjectId(userId, "userId");

    const agency = await AgencySchema.findById(parsedAgencyId).session(session);
    if (!agency) {
        throw new NotFoundError("Compañía de transporte no encontrada.");
    }

    if (agency.adminsId.map(adminId => adminId.toString()).includes(parsedUserId.toString())) {
        throw new ValidationError(
            "El usuario ya es administrador de la compañía de transporte.",
        );
    }

    agency.adminsId.push(parsedUserId);
    await agency.save({ session });
};

/**
 * Obtener las agencias donde el usuario es administrador o conductor.
 * @param userId ID del usuario.
 * @param session Sesión de la base de datos.
 * @throws {InvalidObjectIdError} Si el userId proporcionado no es un Object
 * @returns Promise<Agency[]> Lista de agencias donde el usuario es administrador o conductor.
 */
const getAgenciesWhereUserIsAdminOrDriver = async ({
    userId,
    session,
}: {
    userId: string;
    session: Session;
}): Promise<Agency[]> => {
    const parsedUserId = parseObjectId(userId, "userId");
    const agencies = await AgencySchema.find({
        $or: [{ adminsId: parsedUserId }, { driversId: parsedUserId }],
    }).session(session);

    return agencies.map((agency) => ({
        id: agency._id.toString(),
        name: agency.name,
    }));
};

/**
 * Retorna todas las agencias de transporte.
 * @param session Sesión de la base de datos.
 * @returns Promise<Agency[]> Lista de todas las agencias de transporte.
 */
const getAllAgencies = async ({
    session,
}: {
    session: Session;
}): Promise<Agency[]> => {
    const agencies = await AgencySchema.find({}).session(session);
    return agencies.map((agency) => ({
        id: agency._id.toString(),
        name: agency.name,
    }));
};

/**
 * Obtiene una compañía de transporte por su ID.
 * @param agencyId ID de la compañía de transporte.
 * @param session Sesión de la base de datos.
 * @returns La compañía de transporte encontrada o null si no existe.
 * @throws {ObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getAgencyById = async ({
    agencyId,
    session,
}: {
    agencyId: string;
    session: Session;
}): Promise<Agency> => {
    const parsedAgencyId = parseObjectId(agencyId, "agencyId");
    const agency = await AgencySchema.findById(parsedAgencyId).session(session);
    if (!agency) {
        throw new NotFoundError("Compañía de transporte no encontrada.");
    }
    return {
        id: agency._id.toString(),
        name: agency.name,
    };
};

export {
    createAgency,
    assignAgencyAdmin,
    getAgenciesWhereUserIsAdminOrDriver,
    getAllAgencies,
    getAgencyById,
};
