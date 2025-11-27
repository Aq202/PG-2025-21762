import { Session } from "../../types/db.js";
import consts from "../../utils/consts.js";
import { NotFoundError, UnauthorizedError } from "../../utils/customError.js";
import { getAgencyByIdService } from "../agency/agency.service.js";
import { verifyUserPermissionService } from "../user/user.service.js";
import {
    addRoutesToStops,
    createRouteStop,
    getAllStops,
    getClosestStopByEachRoute,
    getStopsInListById,
    removeRoutesFromStops,
} from "./stop.repository.js";

/**
 * Verifica si el usuario tiene acceso a la ruta como conductor o administrador de agencia.
 * @param routeId ID de la ruta a verificar.
 * @param agencyId ID de la agencia a verificar.
 * @param userId ID del usuario a verificar.
 * @param session Objeto de sesión del usuario.
 * @returns Verdadero si el usuario tiene acceso, falso en caso contrario.
 */
const verifyDriverOrAgencyAdminAccessService = async ({
    routeId,
    agencyId,
    userId,
    session
}: {
    routeId: string;
    agencyId: string;
    userId: string;
    session?: Session;
}) => {
    // Validar permiso de acceso directo de conductor
    const hasDriverAccess = await verifyUserPermissionService({
        userId,
        id: routeId,
        role: consts.roles.driver,
        session,
    });

    if (!hasDriverAccess) {
        // Validar permiso de acceso de agencyAdmin
        const hasAgencyAdminAccess = await verifyUserPermissionService({
            userId,
            id: agencyId,
            role: consts.roles.agencyAdmin,
            session,
        });

        return hasAgencyAdminAccess;
    }

    return true;
}

/**
 * Crea una nueva parada en una ruta existente.
 * @param routeId ID de la ruta.
 * @param stops Array de objetos con nombre y ubicación de las paradas.
 * @param session Objeto de sesión.
 * @param userId ID del usuario que realiza la acción.
 * @returns Promise<RouteStop[]> Objeto que representa las paradas creadas.
 * @throws {NotFoundError} Si la agencia no existe.
 * @throws {UnauthorizedError} Si el usuario no tiene permiso para agregar una parada en la ruta.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const createRouteStopService = async ({
    agencyId,
    userId,
    name,
    location,
    session,
}: {
    agencyId: string;
    userId: string;
    name: string;
    location: LatLong;
    session?: Session;
}) => {

    // Validar permiso de acceso de agencyAdmin
    const hasAgencyAdminAccess = await verifyUserPermissionService({
        userId,
        id: agencyId,
        role: consts.roles.agencyAdmin,
        session,
    });

    if (!hasAgencyAdminAccess) {
        throw new UnauthorizedError(
            "No tienes permiso para agregar una parada en esta ruta.",
        );
    }

    const agency = await getAgencyByIdService({
        agencyId,
        session,
    });
    if (!agency) {
        throw new NotFoundError("La agencia especificada no existe.");
    }

    return await createRouteStop({
        agency,
        name,
        location,
        session,
    });
}

/**
 * Obtiene todas las paradas disponibles.
 * @param agencyId ID de la agencia. Parámetro de búsqueda opcional.
 * @param session Objeto de sesión.
 * @returns Promesa que resuelve con un array de paradas de ruta.
 */
const getAllStopsService = async ({
    agencyId,
    session,
}: {
    agencyId?: string;
    session?: Session;
} = {}) : Promise<RouteStop[]> => {
    
    return await getAllStops({
        agencyId,
        session,
    });
}


/**
 * Verificar si todas las paradas de una lista existen (paradas activas).
 * @param stopsList String[] Listado de id's de paradas
 * @param session
 * @return Promise<Boolean> Devuelve true si todas las paradas existen y están activas.
 */
const checkMissingStopsService = async ({
    stopsList,
    session
}: {
    stopsList: string[];
    session?: Session 
}) : Promise<boolean> => {
    
    const stops = await getStopsInListById({
        stopsList,
        session,
    });

    return stops.length !== stopsList.length;
}

/**
 * Obtener las paradas especificadas en la lista.
 * @param stopsList String[] Listado de id's de paradas
 * @param session
 * @return Promise<Boolean> Devuelve true si todas las paradas existen y están activas.
 */
const getStopsInListService = async ({
    stopsList,
    session
}: {
    stopsList: string[];
    session?: Session
}) : Promise<RouteStop[]> => {

    const stops = await getStopsInListById({
        stopsList,
        session,
    });

    return stops
}

/**
 * 
 * Agrega rutas a las paradas especificadas.
 * @param pairs Array de objetos que contienen la ruta y el ID de la parada.
 * @param session Objeto de sesión.
 * @returns Promise<void>
 * @throws {InvalidObjectIdError} Si alguno de los IDs proporcionados no es un ObjectId válido.
 * No utilizar expuesto sin control de permisos
 */
const addRoutesToStopsInternalService = async ({
    pairs,
    session = null,
}: {
    pairs: {
        route: Route;
        stopId: string;
    }[];
    session?: Session | null;
}): Promise<void> => {
    return await addRoutesToStops({
        pairs,
        session,
    });
}

/**
 * Elimina rutas de las paradas especificadas.
 * @param pairs Array de objetos que contienen la ruta y el ID de la parada.
 * @param session Objeto de sesión.
 * @returns Promise<void> 
 * @throws {InvalidObjectIdError} Si alguno de los IDs proporcionados no es un ObjectId válido.
 * No utilizar expuesto sin control de permisos
 */
const removeRoutesFromStopsInternalService = async ({
    pairs,
    session = null,
}: {
    pairs: {
        routeId: string;
        stopId: string;
    }[];
    session?: Session | null;
}): Promise<void> => {
    return await removeRoutesFromStops({
        pairs,
        session,
    });
}

/**
 * Obtiene las paradas más cercanas a una ubicación dada.
 * @param location Objeto con latitud y longitud del punto de referencia.
 * @param closestByRoute Booleano que indica si se debe obtener solo la parada más cercana por ruta.
 * @param session Objeto de sesión.
 * @returns Promise que resuelve con un array de paradas cercanas.
 */
const getNearbyStopsByRouteService = async ({
    location,
    session = null,
}: {
    location: LatLong;
    session?: Session | null;
}): Promise<NearbyStopByRoute[]> => {
    const maxRadiusInMeters = consts.maxRadiusForNearbyStops;

    return await getClosestStopByEachRoute({
        location,
        maxRadiusInMeters,
        session,
    });

}

export {
    verifyDriverOrAgencyAdminAccessService,
    createRouteStopService,
    getAllStopsService,
    checkMissingStopsService,
    getStopsInListService,
    addRoutesToStopsInternalService,
    removeRoutesFromStopsInternalService,
    getNearbyStopsByRouteService,
};