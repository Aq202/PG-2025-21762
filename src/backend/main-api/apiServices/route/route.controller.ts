import { AppRequest, AppResponse } from "../../types/express.js";
import consts from "../../utils/consts.js";
import {
    ControllerError,
    InvalidObjectIdError,
    NotFoundError,
    UnauthorizedError,
    ValidationError,
} from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";
import normalizeScheduleTime from "../../utils/normalizeScheduleTime.js";
import { createRouteService, getRouteBestRunService as getRouteBestRunPointsService, getRoutePublicDataService, getRouteService, getRoutesListService, getRouteStopsService, updateRouteStopsService } from "./route.service.js";

const createRouteController = async (req: AppRequest, res: AppResponse) => {
    try {
        const {
            agencyId,
            startLocation,
            endLocation,
            name,
            schedules,
            units,
        } = req.body;
        
        const uploadedFiles = req.uploadedFiles || [];

        if (uploadedFiles.length === 0) {
            throw new ControllerError(
                "Debe subir al menos una imagen de las unidades en 'unitImages'.",
                400,
            );
        }

        if (uploadedFiles.length > consts.maxUnitImages) {
            throw new ControllerError(
                `No puedes subir más de ${consts.maxUnitImages} imágenes de las unidades.`,
                400,
            );
        }

        const normalizedSchedules = schedules.map((schedule:Schedule) => {
            if(!schedule.serviceAvailable) return schedule;
            return {
                ...schedule,
                open: schedule.open ? normalizeScheduleTime(schedule.open) : null,
                close: schedule.close ? normalizeScheduleTime(schedule.close) : null,
            };
        });

        const routeResult = await createRouteService({
            userId: req.session?.id as string,
            agencyId,
            startLocation,
            endLocation,
            name: name.trim(),
            schedules: normalizedSchedules,
            units,
            unitImages:uploadedFiles,
        });

        const response: CreateRouteResponse = {
            ok: true,
            message: "Recorrido creado correctamente",
            route: routeResult,
        };

        res.status(201).json(response);
    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al crear nueva ruta";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = "Se proporcionó un ID de agencia no válido.";
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message =
                "No tienes permiso para crear un recorrido en esta agencia.";
        } else if (ex instanceof ControllerError) {
            status = ex.status;
            message = ex.message;
        } else if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        }

        const response: ApiResponse = {
            ok: false,
            message: message,
        };
        res.status(status).json(response);
    }
};

const getRoutesListController = async (req: AppRequest, res: AppResponse) => {
    try {
        const userId = req.session?.id as string;
        const role = req.session?.role as number;
        const agencyId = req.query.agencyId as string;

        const routes = await getRoutesListService({
            userId,
            agencyId,
            role,
        });

        const response: GetRoutesListResponse = {
            ok: true,
            message: "Lista de rutas obtenida correctamente",
            routes,
        };

        res.status(200).json(response);    

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al obtener la lista de rutas";

        if(ex instanceof InvalidObjectIdError) {
            status = 400;
            message = ex.message;
        }

        res.status(status).json({ok: false, message});
    }
}

const getRouteController = async (req: AppRequest, res: AppResponse) => {
    try {
        const routeId = req.params.routeId;
        const userId = req.session?.id as string;

        if (!routeId) {
            throw new ControllerError("Debe proporcionar un ID de ruta válido.", 400);
        }

        const route = await getRouteService({
            routeId,
            userId,
        });

        if (!route) {
            throw new ControllerError("La ruta especificada no existe.", 404);
        }

        const response: GetRouteResponse = {
            ok: true,
            message: "Ruta obtenida correctamente",
            route,
        };

        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al obtener la ruta";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = "Se proporcionó un ID de ruta no válido.";
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message = ex.message;
        } else if (ex instanceof ControllerError) {
            status = ex.status;
            message = ex.message;
        }

        const response: ApiResponse = {
            ok: false,
            message,
        };

        res.status(status).json(response);
    }
};

const getRoutePublicDataController = async (req: AppRequest, res: AppResponse) => {
    try {
        const routeId = req.params.routeId;

        if (!routeId) {
            throw new ControllerError("Debe proporcionar un ID de ruta válido.", 400);
        }

        const route = await getRoutePublicDataService({ routeId });

        const response: GetRoutePublicDataResponse = {
            ok: true,
            message: "Ruta obtenida correctamente",
            route,
        };

        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al obtener datos públicos de la ruta";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = "Se proporcionó un ID de ruta no válido.";
        } else if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        }

        const response: ApiResponse = {
            ok: false,
            message,
        };

        res.status(status).json(response);
    }
};


const getRouteStopsController = async (req: AppRequest, res: AppResponse) => {
    try {
        const routeId = req.params.routeId;
        const userId = req.session?.id as string;

        if (!routeId) {
            throw new ControllerError("Debe proporcionar un ID de ruta válido.", 400);
        }

        const routeStops = await getRouteStopsService({
            routeId,
            userId,
        });

        const response: GetRouteStopsResponse = {
            ok: true,
            message: "Paradas de la ruta obtenidas correctamente",
            routeStops,
        };

        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al obtener las paradas de la ruta";

        if (ex instanceof UnauthorizedError){
            status = 403;
            message = ex.message;
        }

        const response: ApiResponse = {
            ok: false,
            message,
        };
        res.status(status).json(response);
    }
}

const updateRouteStopsController = async (req: AppRequest, res: AppResponse) => {
    try {

        const { routeId } = req.params;
        const { stops } = req.body;
        const userId = req.session?.id as string;

        await updateRouteStopsService({
            routeId,
            stopsList: stops,
            userId,
        });

        const response: UpdateRouteStopsResponse = {
            ok: true,
            message: "Paradas de la ruta actualizadas correctamente.",
        };
        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al actualizar las paradas de la ruta";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = ex.message;
        } else if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message = ex.message;
        } else if (ex instanceof ValidationError) {
            status = 400;
            message = ex.message;
        }

        const response: ApiResponse = {
            ok: false,
            message,
        };
        res.status(status).json(response);
    }
}

const getRouteBestRunController = async (req: AppRequest, res: AppResponse) => {
    try {

        const { routeId } = req.params;

        const bestRun = await getRouteBestRunPointsService({
            routeId,
        });

        const response: GetRouteBestRunResponse = {
            ok: true,
            message: "Recorrido más relevante obtenido correctamente.",
            points: bestRun,
        };
        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        const status = 500;
        const message = "Error al obtener el recorrido más relevante.";

        const response: ApiResponse = {
            ok: false,
            message,
        };
        res.status(status).json(response);
    }
}
    

export { 
    createRouteController,
    getRoutesListController,
    getRouteController,
    getRoutePublicDataController,
    getRouteStopsController,
    updateRouteStopsController,
    getRouteBestRunController,
};
