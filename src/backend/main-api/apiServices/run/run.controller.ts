import { AppRequest, AppResponse } from "../../types/express.js";
import {
    ControllerError,
    InvalidObjectIdError,
    NotFoundError,
    UnauthorizedError,
    ValidationError,
} from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";
import { addRunPointService, finishRouteRunService,  getMatchedRunPointsService,  getNearbyRunsService,  startRouteRunService, testOSRMService } from "./run.service.js";

/**
 * Controller para iniciar un recorrido de una ruta.
 * @param req 
 * @param res 
 */
const startRouteRunController = async (req: AppRequest, res: AppResponse) => {
    try {

        const {
            time,
            routeId
        } = req.body;

        // Validación básica
        if (!routeId || !time) {
            throw new ControllerError("Faltan campos requeridos para iniciar el recorrido.", 400);
        }

        const userId = req.session?.id as string;

        const run = await startRouteRunService({
            routeId,
            userId,
            time: new Date(time),
        });

        const response: StartRouteRunResponse = {
            ok: true,
            message: "Recorrido iniciado correctamente",
            run,
        };

        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al iniciar un nuevo recorrido.";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = "Se proporcionó un ID inválido.";
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message = ex.message;
        } else if (ex instanceof NotFoundError) {
            status = 404;
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

const addRunPointController = async (req: AppRequest, res: AppResponse) => {
    try {
        const { runId } = req.params;
        const {
            lat,
            long,
            speed,
            time,
            accuracy,
        } = req.body;

        if (!runId || lat == null || long == null || speed == null || !time || accuracy == null) {
            throw new ControllerError("Faltan campos requeridos para agregar un punto al recorrido.", 400);
        }

        const userId = req.session?.id as string;

        await addRunPointService({
            runId,
            userId,
            lat: parseFloat(lat),
            long: parseFloat(long),
            speed: parseFloat(speed),
            time: new Date(time),
            accuracy: parseFloat(accuracy),
        });

        const response: AddRunPointResponse = {
            ok: true,
            message: "Punto agregado correctamente al recorrido",
        };

        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al agregar punto al recorrido.";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = "Se proporcionó un ID inválido.";
        } else if (ex instanceof UnauthorizedError) {
            status = 403;
            message = ex.message;
        } else if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        } else if (ex instanceof ControllerError) {
            status = ex.status;
            message = ex.message;
        } else if (ex instanceof ValidationError) {
            status = 400;
            message = ex.message;
        }

        res.status(status).json({ ok: false, message });
    }
};

const finishRouteRunController = async (req: AppRequest, res: AppResponse) => {
    try {

        const userId = req.session?.id as string;
        const { runId } = req.params;

        const hasEndToEndRoutes = await finishRouteRunService({
            runId,
            userId
        });

        const response = { ok: true, message: hasEndToEndRoutes ? "Ruta finalizada correctamente." : "Ruta finalizada, pero no contiene recorridos completos válidos." } as ApiResponse;
        res.status(200).json(response);

    }catch(ex){
        errorLogger(ex);
        let status = 500;
        let message = "Error al finalizar la ruta.";

        if (ex instanceof InvalidObjectIdError) {
            status = 400;
            message = "Se proporcionó un ID de ruta no válido.";
        }else if (ex instanceof ControllerError){
            status = ex.status;
            message = ex.message;
        }else if(ex instanceof UnauthorizedError){
            status = 403;
            message = ex.message;
        }else if(ex instanceof NotFoundError){
            status = 404;
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
};


const getNearbyRunsController = async (req: AppRequest, res: AppResponse) => {
    try {

        const { lat, long } = req.query;

        const nearbyRuns = await getNearbyRunsService({
            destination: {
                lat: parseFloat(lat as string),
                long: parseFloat(long as string),
            }
        })


        const response: GetNearbyRunsResponse = { 
            ok: true,
            message: "Recorridos cercanos obtenidos correctamente.",
            runs: nearbyRuns
        };
        res.status(200).json(response);

    }catch(ex){
        errorLogger(ex);
        const status = 500;
        const message = "Error al obtener recorridos cercanos.";


        const response: ApiResponse = {
            ok: false,
            message,
        };

        res.status(status).json(response);
    }
};

const getMatchedRunPointsController = async (req: AppRequest, res: AppResponse) => {
    try {
        const { endToEndRunId } = req.params;


        const matchedPoints = await getMatchedRunPointsService({ endToEndRunId });

        const response: GetMatchedRunPointsResponse = {
            ok: true,
            message: "Puntos obtenidos correctamente.",
            points: matchedPoints
        };

        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al obtener puntos del recorrido.";

        if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        }

        res.status(status).json({ ok: false, message });
    }
};

const postMapMatchingTestController = async (req: AppRequest, res: AppResponse) => {
    try {
        const { runId } = req.params;


        await testOSRMService({ runId });

        const response: ApiResponse = {
            ok: true,
            message: "Prueba de map matching exitosa.",
        };

        res.status(200).json(response);

    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        let message = "Error al realizar prueba de map matching.";

        if (ex instanceof NotFoundError) {
            status = 404;
            message = ex.message;
        }

        res.status(status).json({ ok: false, message });
    }
};

export { 
    startRouteRunController,
    addRunPointController,
    finishRouteRunController,
    getNearbyRunsController,
    getMatchedRunPointsController,
    postMapMatchingTestController,
};
