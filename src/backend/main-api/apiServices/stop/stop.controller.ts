import { AppRequest, AppResponse } from "../../types/express.js";
import {
    InvalidObjectIdError,
    NotFoundError,
    UnauthorizedError,
} from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";
import { createRouteStopService, getAllStopsService, getNearbyStopsByRouteService, } from "./stop.service.js";


const createRouteStopController = async (req: AppRequest, res: AppResponse) => {
    try {
        const { agencyId, name, location } = req.body;
        const userId = req.session?.id as string;

        const routeStop = await createRouteStopService({
            agencyId,
            name,
            location,
            userId,
        });

        const response: CreateRouteStopResponse = {
            ok: true,
            message: "Parada creada correctamente.",
            routeStop,
        };
        res.status(201).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al crear la parada.";
        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = ex.message;
        } else if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message = ex.message;
        }

        const response: ApiResponse = {
            ok: false,
            message,
        };
        res.status(status).json(response);
    }
};

const getStopsController = async (req: AppRequest, res: AppResponse) => {
    try {

        const agencyId = req.query.agencyId as string | undefined;
        const routeStops = await getAllStopsService({ agencyId });

        const response: getAllStopsResponse = {
            ok: true,
            message: "Paradas obtenidas correctamente",
            routeStops,
        };
        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al obtener el listado de paradas disponibles.";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = ex.message;
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message = ex.message;
        }
        const response: ApiResponse = {
            ok: false,
            message,
        };
        res.status(status).json(response);
    }
};

const getNearbyStopsByRouteController = async (req: AppRequest, res: AppResponse) => {
    try {

        const { lat, long } = req.query;

        const nearbyStops = await getNearbyStopsByRouteService({
            location: {
                lat: parseFloat(lat as string),
                long: parseFloat(long as string),
            },
        });

        const response: getNearbyStopsByRouteResponse = {
            ok: true,
            message: "Paradas obtenidas correctamente",
            nearbyStops,
        };
        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        const status = 500;
        const message = "Error al obtener el listado de paradas disponibles.";

        const response: ApiResponse = {
            ok: false,
            message,
        };
        res.status(status).json(response);
    }
};

export {
    createRouteStopController,
    getStopsController,
    getNearbyStopsByRouteController,
};
