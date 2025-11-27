import parseObjectId from "../../utils/parseObjectId.js";
import { Session } from "../../types/db.js";
import RouteStopSchema from "../../db/mongodb/schemas/routeStop.schema.js";
import config from "config";
import mongoose from "mongoose";

const approveStopsAutomatically = config.get("approveStopsAutomatically") as boolean;

/**
 * Crea una nueva serie de paradas para una ruta.
 * @param routeId ID de la ruta.
 * @param stops Array de objetos con nombre y ubicación de las paradas.
 * @param session Objeto de sesión.
 * @returns Promise<RouteStop[]> Objeto que representa la parada creada.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const createRouteStop = async ({
    name,
    location,
    agency,
    session,
}: {
    name: string;
    location: LatLong
    agency: Agency;
    session?: Session;
}): Promise<RouteStop> => {
    const routeStop = new RouteStopSchema();

    routeStop.name = name;
    routeStop.location = [location.long, location.lat];
    routeStop.agency = {
        _id: parseObjectId(agency.id, "agencyId"),
        name: agency.name,
    }
    routeStop.approved = approveStopsAutomatically;

    const result = await routeStop.save({ session });
    return {
        id: result._id.toString(),
        name: result.name,
        location: {
            long: result.location[0],
            lat: result.location[1],
        },
        agency: {
            id: result.agency._id.toString(),
            name: result.agency.name,
        },
        routes: [],
    };
    
}

/**
 * Obtiene todas las paradas disponibles.
 * @param session Objeto de sesión.
 * @returns Promesa que resuelve con un array de paradas de ruta.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getAllStops = async ({
    agencyId,
    session = null,
}: {
    agencyId?: string;
    session?: Session | null;
}): Promise<RouteStop[]> => {

    const query: { approved: boolean; "agency._id"?: mongoose.Types.ObjectId } = { approved: true };
    if (agencyId) {
        query["agency._id"] = parseObjectId(agencyId, "agencyId");
    }

    const routeStops = await RouteStopSchema.find(query).session(session);
    return routeStops.map((stop) => ({
        id: stop._id.toString(),
        name: stop.name,
        location: {
            long: stop.location[0],
            lat: stop.location[1],
        },
        agency: {
            id: stop.agency._id.toString(),
            name: stop.agency.name,
        },
        routes: stop.routes.map((route) => ({
            id: route._id.toString(),
            name: route.name,
        })),
    }));
};

/**
 * Obtiene las paradas de una lista de IDs.
 * @param stopsList Lista de IDs de las paradas.
 * @param session Objeto de sesión.
 * @returns Promise<RouteStop[]> Lista de paradas correspondientes a los IDs proporcionados.
 * @throws {InvalidObjectIdError} Si alguno de los IDs proporcionados no es un ObjectId válido.
 */
const getStopsInListById = async ({
    stopsList,
    session = null,
}: {
    stopsList: string[];
    session?: Session | null;
}): Promise<RouteStop[]> => {
    const objectIdList = stopsList.map((id) => parseObjectId(id, "stopId"));
    const routeStops = await RouteStopSchema.find({
        _id: { $in: objectIdList },
        approved: true,
    }).session(session);

    return routeStops.map((stop) => ({
        id: stop._id.toString(),
        name: stop.name,
        location: {
            long: stop.location[0],
            lat: stop.location[1],
        },
        agency: {
            id: stop.agency._id.toString(),
            name: stop.agency.name,
        },
        routes: stop.routes.map((route) => ({
            id: route._id.toString(),
            name: route.name,
        })),
    }));
};

/**
 * 
 * Agrega rutas a las paradas especificadas.
 * @param pairs Array de objetos que contienen la ruta y el ID de la parada.
 * @param session Objeto de sesión.
 * @returns Promise<void>
 */
const addRoutesToStops = async ({
    pairs,
    session = null,
}: {
    pairs: {
        route: Route;
        stopId: string;
    }[];
    session?: Session | null;
}): Promise<void> => {
    const ops = pairs.map(({ route, stopId }) => {
        const routeObjectId = parseObjectId(route.id, "routeId");
        const stopObjectId = parseObjectId(stopId, "stopId");

        return {
            updateOne: { // Agregar solo si no existe el _id en el array
                filter: { _id: stopObjectId, "routes._id": { $ne: routeObjectId } },
                update: {
                    $push: { routes: { _id: routeObjectId, name: route.name } },
                },
            },
        };
    });

    await RouteStopSchema.bulkWrite(ops, { session: session ?? undefined });
};

/**
 * Elimina rutas de las paradas especificadas.
 * @param pairs Array de objetos que contienen la ruta y el ID de la parada.
 * @param session Objeto de sesión.
 * @returns Promise<void> 
 */
const removeRoutesFromStops = async ({
    pairs,
    session = null,
}: {
    pairs: {
        routeId: string;
        stopId: string;
    }[];
    session?: Session | null;
}): Promise<void> => {
    const ops = pairs.map(({ routeId, stopId }) => {
        const routeObjectId = parseObjectId(routeId, "routeId");
        const stopObjectId = parseObjectId(stopId, "stopId");

        return {
            updateOne: {
                filter: { _id: stopObjectId },
                update: { $pull: { routes: { _id: routeObjectId } } },
            },
        };
    });

    await RouteStopSchema.bulkWrite(ops, { session: session ?? undefined });
};

/**
 * 
 * Obtiene la parada más cercana para cada ruta dentro de un radio especificado.
 * @param location Objeto con latitud y longitud del punto de referencia.
 * @param maxRadiusInMeters Radio máximo en metros para buscar paradas cercanas.
 * @param session Objeto de sesión.
 * @returns Promise que resuelve con un array de tuplas que contienen la ruta, la parada y la distancia en metros.
 */
const getClosestStopByEachRoute = async ({
    location,
    maxRadiusInMeters,
    session = null,
}: {
    location: LatLong;
    maxRadiusInMeters: number;
    session: Session;
}): Promise<NearbyStopByRoute[]> => {

    const nearbyStopsResult = await RouteStopSchema.aggregate([
        {
            // Encuentra los stops cercanos dentro del radio máximo
            // usando índice geoespacial.
            $geoNear: {
                near: {
                    type: "Point",
                    coordinates: [location.long, location.lat],
                },
                distanceField: "distance",   // el campo resultante será la distancia calculada
                maxDistance: maxRadiusInMeters,
                spherical: true,
                query: { approved: true },
            },
        },
        {
            // Guardar el array original de rutas en un nuevo campo
            // para no perderlo al hacer el unwind.
            $addFields: {
                originalRoutes: "$routes"
            }
        },
        {
            // Explorar el array de rutas, generando un documento por cada ruta.
            $unwind: "$routes"
        },
        {
            // Agrupar por ruta y obtener solo stop más cercano
            $group: {
                _id: "$routes._id",
                distance: { $first: "$distance" },
                stopId: { $first: "$_id" },
                stopName: { $first: "$name" },
                stopLocation: { $first: "$location" },
                stopAgency: { $first: "$agency" },
                routeName: { $first: "$routes.name" },
                routesArray: { $first: "$originalRoutes" },
            },
        },
        {
            $sort: { distance: 1 }
        },
        {
            $project: {
                _id: 0,
                route: {
                    id: "$_id",
                    name: "$routeName",
                },
                stop: {
                    id: "$stopId",
                    name: "$stopName",
                    location: "$stopLocation",
                    agency: "$stopAgency",
                    routes: "$routesArray",
                },
                distance: 1,
            },
        },
    ]).session(session);

    return nearbyStopsResult.map((item) => ({
        route: {
            id: item.route.id.toString(),
            name: item.route.name,
        },
        stop: {
            id: item.stop.id.toString(),
            name: item.stop.name,
            location: {
                long: item.stop.location[0],
                lat: item.stop.location[1],
            },
            agency: {
                id: item.stop.agency._id,
                name: item.stop.agency.name,
            },
            routes: item.stop.routes?.map((route: { _id: string; name: string }) => ({
                id: route._id.toString(),
                name: route.name,
            })) || [],
        },
        distance: item.distance,
    }));
};



export {
    createRouteStop,
    getAllStops,
    getStopsInListById,
    removeRoutesFromStops,
    addRoutesToStops,
    getClosestStopByEachRoute,
};
