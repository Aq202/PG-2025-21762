import getSignedDownloadUrl from "../../services/cloudStorage/getSignedUrl.js";
import uploadFile from "../../services/cloudStorage/uploadFile.js";
import { Session } from "../../types/db.js";
import consts from "../../utils/consts.js";
import { NotFoundError, UnauthorizedError, ValidationError } from "../../utils/customError.js";
import { verifyUserPermissionService } from "../user/user.service.js";
import {
    createRoute,
    getAllAgencyRoutes,
    getDriverRoutes,
    getRoute,
    updateRouteStops,
} from "./route.repository.js";
import fs from "fs";
import path from "path";
import { promisify } from "util";
import { v4 as uuidv4 } from "uuid";
import { addRoutesToStopsInternalService, getStopsInListService, removeRoutesFromStopsInternalService } from "../stop/stop.service.js";
import { getAgencyByIdService } from "../agency/agency.service.js";
import { withMongoTransaction } from "../../utils/withMongoTransaction.js";
import { getMostRecentMatchedRunPointsInternalService, getRunPointsWithHigherScoreService } from "../run/run.service.js";
import { getH3Cell } from "../../utils/h3Index.js";

// Promisificar funciones de fs
const readFile = promisify(fs.readFile);
const unlink = promisify(fs.unlink);


export async function uploadUnitImages(unitImages: { fileName: string, type: string }[]) {
    const uploadedKeys: string[] = [];
    
    // @ts-expect-error Global variable to store the directory name of the current module
    const dirname = global.dirname
    const filesDir = path.join(dirname, "files");

    try {
        for (const image of unitImages) {
            const filePath = path.join(filesDir, image.fileName);

            // Leer archivo
            const data = await readFile(filePath);

            // Generar UUID como key y mantener extensión
            const key = uuidv4() + path.extname(image.fileName);

            // Subir a S3
            await uploadFile(key, data, image.type);
            uploadedKeys.push(key);

            console.log(`Archivo ${image.fileName} subido con key: ${key}`);

            // Borrar archivo temporal
            await unlink(filePath);
            console.log(`Archivo temporal ${image.fileName} eliminado`);
        }

        return uploadedKeys;
    } catch (error) {
        console.error("Error procesando archivos temporales:", error);
        throw error;
    }
}

/**
 * Crea un nuevo recorrido.
 * @param agencyId ID de la agencia de transporte.
 * @param startLocation {lat,long} Ubicación de inicio del recorrido.
 * @param endLocation {lat,long} Ubicación de fin del recorrido.
 * @param name Nombre del recorrido.
 * @param schedules [{
 *  day: día de la semana 0-6
 *  open: hora de apertura en formato HH:mm
 *  close: hora de cierre en formato HH:mm
 *  serviceAvailable: boolean
 * }] Horarios del recorrido.
 * @param units Array de identificadores de unidades asociadas al recorrido.
 * @param unitImages Array de URLs de imágenes de las unidades asociadas al recorrido.
 * @returns Promise<Route> Objeto que representa el recorrido creado.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 * @throws {UnauthorizedError} Si el usuario no tiene permiso para crear un recorrido en la agencia.
 * @throws {NotFoundError} Si la agencia no existe.
 */
const createRouteService = async ({
    agencyId,
    startLocation,
    endLocation,
    name,
    schedules,
    units,
    unitImages,
    userId,
    session,
}: {
    agencyId: string;
    startLocation: LatLong;
    endLocation: LatLong;
    name: string;
    schedules: Schedule[];
    units: string[];
    unitImages: { fileName: string, type: string }[];
    userId: string;
    session?: Session;
}): Promise<Route> => {
    // Validar permiso de acceso
    const hasAccess = verifyUserPermissionService({
        userId,
        id: agencyId,
        role: consts.roles.agencyAdmin,
        session,
    });

    if (!hasAccess) {
        throw new UnauthorizedError(
            "No tienes permiso para crear un recorrido en esta agencia.",
        );
    }

    // Obtener agencia
    const agency = await getAgencyByIdService({ agencyId, session });
    if (!agency) {
        throw new NotFoundError("Agencia no encontrada.");
    }

    const uploadedFilesKey = await uploadUnitImages(unitImages);

    const startLocationGeoIndex = getH3Cell({ location: startLocation, resolution: consts.h3GeoIndexResolution.routeEdgePoints });
    const endLocationGeoIndex = getH3Cell({ location: endLocation, resolution: consts.h3GeoIndexResolution.routeEdgePoints });

    return await createRoute({
        agency,
        startLocation,
        endLocation,
        name: name.trim(),
        schedules,
        units,
        unitImages: uploadedFilesKey,
        startLocationGeoIndex,
        endLocationGeoIndex,
        session,
    });
};

/**
 * Retornar la lista de rutas a las que tiene acceso el usuario.
 * Si es admin, retorna todas las rutas de la agencia.
 * Si es agencyAdmin, verifica el permiso y retorna todas las rutas de la agencia.
 * Si es conductor, retorna las rutas asignadas al conductor.
 * Si no es conductor de ninguna ruta, retorna un array vacío.
 * @param userId ID del usuario.
 * @param role Rol del usuario (admin, agencyAdmin, driver).
 * @param agencyId ID de la agencia de transporte.
 * @param session Objeto de sesión.
 * @returns Promise<Route[]> Lista de rutas a las que tiene acceso el usuario.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getRoutesListService = async ({
    userId,
    role,
    agencyId,
}: {
    userId: string;
    role: number;
    agencyId: string;
}): Promise<Route[]> => {
    if (role === consts.roles.admin) {
        // Si es admin, retornar todas las rutas de la agencia
        return getAllAgencyRoutes(agencyId);
    }

    // Si es agencyAdmin, verificar permiso
    const isAgencyAdmin =
        role === consts.roles.agencyAdmin ||
        (await verifyUserPermissionService({
            userId,
            id: agencyId,
            role: consts.roles.agencyAdmin,
        }));

    return getDriverRoutes({
        userId,
        agencyId,
        isAgencyAdmin,
    });
};

/**
 * Obtiene una ruta sin verificar permisos. Esta función NO debe ser utilizada para acceder
 * directamente a rutas sin la debida autorización. Para uso interno únicamente.
 * @param routeId
 * @returns Route | null. 
 */
const getRouteUnauthorizedService = async ({
    routeId,
}: {
    routeId: string;
}) => {
    const route = await getRoute(routeId);

    return route;
};

/**
 * Obtiene los detalles de una ruta específica.
 * Verifica los permisos del usuario para acceder a la ruta.
 * @param routeId ID de la ruta a consultar.
 * @param userId ID del usuario que realiza la consulta.
 * @param session Objeto de sesión de express.
 * @returns Promise<Route|Null> Objeto que representa la ruta solicitada.
 */
const getRouteService = async ({
    routeId,
    userId,
    session,
}: {
    routeId: string;
    userId: string;
    session?: Session;
}): Promise<Route | null> => {
    const route = await getRoute(routeId);

    if (!route) {
        return null;
    }

    // Validar permiso de acceso directo de conductor
    const hasDriverAccess = verifyUserPermissionService({
        userId,
        id: routeId,
        role: consts.roles.driver,
        session,
    });

    if (!hasDriverAccess) {
        // Validar permiso de acceso de agencyAdmin
        const hasAgencyAdminAccess = verifyUserPermissionService({
            userId,
            id: route.agency.id,
            role: consts.roles.agencyAdmin,
            session,
        });

        if (!hasAgencyAdminAccess) {
            throw new UnauthorizedError(
                "No tienes permiso para consultar esta ruta.",
            );
        }
    }

    return route;
};

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

const getRoutePublicDataService = async ({ routeId }:{routeId: string}):Promise<RoutePublicData>=> {
    const route = await getRoute(routeId);

    if (!route) {
        throw new NotFoundError("Ruta no encontrada");
    }

    const imgAccessUrl = await Promise.all(
        route.unitImages.map(async (key) => getSignedDownloadUrl(key))
    );

    const publicData: RoutePublicData = {
        id: route.id,
        name: route.name,
        unitImages: imgAccessUrl,
        schedules: route.schedules,
    };

    return publicData;
};

/**
 * Obtiene las paradas de una ruta por su ID.
 * @param routeId ID de la ruta.
 * @param session Objeto de sesión.
 * @returns Promesa que resuelve con un array de paradas de ruta.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 * @throws {UnauthorizedError} Si el usuario no tiene permiso para consultar las paradas de la ruta.
 */
const getRouteStopsService = async ({
    routeId,
    userId,
}: {    
    routeId: string;
    userId: string;
}) => {

    const route = await getRoute(routeId);
    if (!route) {
        throw new NotFoundError("Ruta no encontrada");
    }
    const agencyId = route.agency.id;

    // Validar permiso de acceso de agencyAdmin o driver
    const hasAccess = await verifyDriverOrAgencyAdminAccessService({
        routeId,
        agencyId,
        userId,
    })
    
    if (!hasAccess) {
        throw new UnauthorizedError(
            "No tienes permiso para consultar las paradas de esta ruta.",
        );
    }

    if (!route.stops || route.stops.length === 0) {
        return [];
    }

    // Obtener detalles de las paradas
    const stops = await getStopsInListService({
        stopsList: route.stops,
    })

    return stops;
}


/**
 * Actualiza el listado de paradas de una ruta. Reemplaza el listado completo.
 * @param routeId ID de la ruta a actualizar.
 * @param stopsList Nuevo listado de IDs de paradas.
 * @param userId ID del usuario que realiza la actualización.
 * @param session Objeto de sesión.
 * @returns Promise<Route> Objeto que representa la ruta actualizada.
 * @throws {NotFoundError} Si la ruta no existe.
 * @throws {UnauthorizedError} Si el usuario no tiene permiso para actualizar las paradas de la ruta.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const updateRouteStopsService = async ({
    routeId,
    stopsList,
    userId,
    session: parentSession =null,
}: {
    routeId: string;
    stopsList: string[];
    userId: string;
    session?: Session;
}) => {

    const route = await getRoute(routeId);
    if (!route) {
        throw new NotFoundError("Ruta no encontrada");
    }

    // Validar permiso de acceso de agencyAdmin
    const hasAgencyAdminAccess = await verifyUserPermissionService({
        userId,
        id: route.agency.id,
        role: consts.roles.agencyAdmin,
        session: parentSession,
    });

    if (!hasAgencyAdminAccess) {
        throw new UnauthorizedError(
            "No tienes permiso para actualizar las paradas de esta ruta.",
        );
    }

    // Obtener detalles de las paradas para validar agencyId y existencia
    const stopsDetails = await getStopsInListService({
        stopsList,
    });

    const missingStops = stopsDetails.length !== stopsList.length;

    if (missingStops) {
        throw new ValidationError("Una o más paradas no existen.");
    }

    // Validar que todas las paradas pertenezcan a la misma agencia
    for (const stop of stopsDetails) {
        if (stop.agency.id !== route.agency.id) {
            throw new ValidationError("Una o más paradas no pertenecen a la agencia de la ruta.");
        }
    }

    const stopsToAdd = stopsList.filter(stopId => !route.stops?.includes(stopId));
    const stopsToRemove = (route.stops || []).filter(stopId => !stopsList.includes(stopId));

    if (stopsToAdd.length === 0 && stopsToRemove.length === 0) {
        // No hay cambios en las paradas
        return;
    }

    await withMongoTransaction(async (currentSession) => {
        const session = parentSession || currentSession;

        // Actualizar paradas de la ruta
        await updateRouteStops({
            routeId,
            stopsList,
            session,
        });

        // Añadir o eliminar la ruta (doc embedded) en las paradas afectadas
        await addRoutesToStopsInternalService({
            pairs: stopsToAdd.map(stopId => ({ stopId, route })),
            session,
        });

        await removeRoutesFromStopsInternalService({
            pairs: stopsToRemove.map(stopId => ({ stopId, routeId }) ),
            session,
        })

    });

    
}

/**
 * Obtiene el recorrido más relevante para una ruta específica.
 * @param routeId ID de la ruta.
 * @param session Objeto de sesión de MongoDB. 
 * @returns 
 */
const getRouteBestRunService = async ({
    routeId,
    session = null,
}: {
    routeId: string;
    session?: Session;
}): Promise<LatLong[]> => {

    // Primero intenta obtener recorrido según prioridad de puntaje
    const runPointsWithHigherScore = await getRunPointsWithHigherScoreService({
        routeId,
        session,
    });

    if (runPointsWithHigherScore) {
        return runPointsWithHigherScore;
    }
    // Si no hay recorrido con puntaje, obtener el recorrido más reciente
    return await getMostRecentMatchedRunPointsInternalService({ routeId, session });
};

export {
    createRouteService,
    getRoutesListService,
    getRouteService,
    verifyDriverOrAgencyAdminAccessService,
    getRouteUnauthorizedService,
    getRoutePublicDataService,
    getRouteStopsService,
    updateRouteStopsService,
    getRouteBestRunService
};