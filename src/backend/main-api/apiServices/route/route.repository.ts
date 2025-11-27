import RouteSchema from "../../db/mongodb/schemas/route.schema.js";
import parseObjectId from "../../utils/parseObjectId.js";
import { Session } from "../../types/db.js";
import { NotFoundError } from "../../utils/customError.js";

/**
 * Crea un nuevo recorrido.
 * @param agencyId ID de la agencia de transporte.
 * @param startLocation {lat,long} Ubicación de inicio del recorrido.
 * @param endLocation {lat,long} Ubicación de fin del recorrido.
 * @param name Nombre del recorrido.
 * @param endName Nombre del punto de fin.
 * @param schedules [{
 *  day: día de la semana 0-6
 *  open: hora de apertura en formato HH:mm
 *  close: hora de cierre en formato HH:mm
 *  serviceAvailable: boolean
 * }] Horarios del recorrido.
 * @param units Array de identificadores de unidades asociadas al recorrido.
 * @param unitImages Array de URLs de imágenes de las unidades asociadas al recorrido.
 * @param startLocationGeoIndex H3 index de la ubicación de inicio.
 * @param endLocationGeoIndex H3 index de la ubicación de fin.
 * @param session Objeto de sesión.
 * @returns Promise<Route> Objeto que representa el recorrido creado.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const createRoute = async ({
    agency,
    startLocation,
    endLocation,
    name,
    schedules,
    units,
    unitImages,
    startLocationGeoIndex,
    endLocationGeoIndex,
    session,
}: {
    agency: Agency;
    startLocation: LatLong;
    endLocation: LatLong;
    name: string;
    schedules: Schedule[];
    units: string[];
    unitImages: string[];
    startLocationGeoIndex: string;
    endLocationGeoIndex: string;
    session?: Session;
}): Promise<Route> => {
    const route = new RouteSchema();
    route.agency = {
        _id: parseObjectId(agency.id, "agencyId"),
        name: agency.name,
    }
    route.startLocation = [startLocation.long, startLocation.lat];
    route.endLocation = [endLocation.long, endLocation.lat];
    route.name = name.trim();
    route.set("schedules", schedules);
    route.units = units;
    route.unitImages = unitImages;
    route.startLocationGeoIndex = startLocationGeoIndex;
    route.endLocationGeoIndex = endLocationGeoIndex;

    const result = await route.save({ session });

    return {
        id: result._id.toString(),
        agency: {
            id: route.agency._id.toString(),
            name: route.agency.name,
        },
        startLocation,
        endLocation,
        name,
        schedules,
        units,
        unitImages,
        stops: route.stops ? route.stops.map(id => id.toString()) : [],
        startLocationGeoIndex,
        endLocationGeoIndex,
    };
};

/**
 * Devuelve todas las rutas de una agencia de transporte.
 * @param agencyId ID de la agencia de transporte.
 * @param session Objeto de sesión.
 * @returns Promise<Route[]> Lista de rutas asociadas a la agencia.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getAllAgencyRoutes = async (
    agencyId: string,
    session: Session | null = null,
): Promise<Route[]> => {
    const routes = await RouteSchema.find({
        "agency._id": parseObjectId(agencyId, "agencyId"),
    }).session(session);
    
    return routes.map((route) => ({
        id: route._id.toString(),
        agency: {
            id: route.agency._id.toString(),
            name: route.agency.name,
        },
        startLocation: {
            long: route.startLocation[0],
            lat: route.startLocation[1],
        },
        endLocation: {
            long: route.endLocation[0],
            lat: route.endLocation[1],
        },
        name: route.name,
        schedules: route.schedules.map((schedule) => ({
            day: schedule.day,
            open: schedule.open || null,
            close: schedule.close || null,
            serviceAvailable: schedule.serviceAvailable,
        })),
        units: route.units,
        unitImages: route.unitImages,
        stops: route.stops ? route.stops.map(id => id.toString()) : [],
        startLocationGeoIndex: route.startLocationGeoIndex,
        endLocationGeoIndex: route.endLocationGeoIndex,
    }));
};

/**
 * Devuelve todas las rutas asociadas a un usuario.
 * Si el usuario es agencyAdmin, devuelve todas las rutas de la agencia a la que pertenece.
 * Si no, devuelve las rutas en las que el usuario es conductor.
 * @param userId ID del conductor.
 * @param agencyId ID de la agencia de transporte (si es agencyAdmin).
 * @param isAgencyAdmin Booleano que indica si el usuario es administrador de la agencia.
 * @param session Objeto de sesión.
 * @return Promise<Route[]> Lista de rutas asociadas al usuario.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId
 */
const getDriverRoutes = async ({
    userId,
    agencyId,
    isAgencyAdmin,
    session = null,
}: {
    userId: string;
    agencyId: string;
    isAgencyAdmin: boolean;
    session?: Session | null;
}): Promise<Route[]> => {

    const agencyIdObj = parseObjectId(agencyId, "agencyId");

    // Buscar por defecto las rutas en las que el usuario es conductor
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const orConditions: any[] = [
        { driversId: parseObjectId(userId, "userId"), "agency._id": agencyIdObj },
    ];

    if (isAgencyAdmin) {
        // Si es agencyAdmin, agregar rutas de la agencia en la que es administrador
        orConditions.push({ "agency._id": agencyIdObj });
    }

    const query = { $or: orConditions };

    const routes = await RouteSchema.find(query).session(session);

    return routes.map((route) => ({
        id: route._id.toString(),
        agency: {
            id: route.agency._id.toString(),
            name: route.agency.name,
        },
        startLocation: {
            long: route.startLocation[0],
            lat: route.startLocation[1],
        },
        endLocation: {
            long: route.endLocation[0],
            lat: route.endLocation[1],
        },
        name: route.name,
        schedules: route.schedules.map((schedule) => ({
            day: schedule.day,
            open: schedule.open || null,
            close: schedule.close || null,
            serviceAvailable: schedule.serviceAvailable,
        })),
        units: route.units,
        unitImages: route.unitImages,
        stops: route.stops ? route.stops.map(id => id.toString()) : [],
        startLocationGeoIndex: route.startLocationGeoIndex,
        endLocationGeoIndex: route.endLocationGeoIndex,
    }));
};

/**
 * Obtiene una ruta por su ID.
 * @param routeId ID de la ruta.
 * @returns Promise<Route | null> Objeto que representa la ruta o null si no se encuentra.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getRoute = async (routeId: string): Promise<Route | null> => {
    const route = await RouteSchema.findById(parseObjectId(routeId, "routeId"));
    if (!route) return null;

    return {
        id: route._id.toString(),
        agency: {
            id: route.agency._id.toString(),
            name: route.agency.name,
        },
        startLocation: {
            long: route.startLocation[0],
            lat: route.startLocation[1],
        },
        endLocation: {
            long: route.endLocation[0],
            lat: route.endLocation[1],
        },
        name: route.name,
        schedules: route.schedules.map((schedule) => ({
            day: schedule.day,
            open: schedule.open || null,
            close: schedule.close || null,
            serviceAvailable: schedule.serviceAvailable,
        })),
        units: route.units,
        unitImages: route.unitImages,
        stops: route.stops ? route.stops.map(id => id.toString()) : [],
        startLocationGeoIndex: route.startLocationGeoIndex,
        endLocationGeoIndex: route.endLocationGeoIndex,
    }
}

/**
 * Actualizar las paradas asociadas a una ruta.
 * @param routeId ID de la ruta.
 * @param stopsList Lista de IDs de las paradas a asociar a la ruta.
 * @param session Objeto de sesión.
 * @return Promise<void>
 * @throws {NotFoundError} Si la ruta no existe.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const updateRouteStops = async ({
    routeId,
    stopsList,
    session = null,
}: {
    routeId: string;
    stopsList: string[];
    session?: Session | null;
}): Promise<void> => {
    const uniqueIds = [...new Set(stopsList)].map((id) =>
        parseObjectId(id, "stopId")
    );

    const { matchedCount } = await RouteSchema.updateOne(
        { _id: parseObjectId(routeId, "routeId") },
        { $set: { stops: uniqueIds } }
    ).session(session);

    if (matchedCount === 0) {
        throw new NotFoundError("Ruta no encontrada");
    }
};


export {
    createRoute,
    getAllAgencyRoutes,
    getDriverRoutes,
    getRoute,
    updateRouteStops,
};
