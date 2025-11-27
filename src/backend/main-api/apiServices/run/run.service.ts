import { signRouteResourceToken } from "../../services/token/jwt.js";
import { Session } from "../../types/db.js";
import consts from "../../utils/consts.js";
import { InvalidObjectIdError, NotFoundError, UnauthorizedError } from "../../utils/customError.js";
import getDistanceBetweenGeoPoints from "../../utils/getDistanceBetweenGeoPoints.js";
import parseObjectId from "../../utils/parseObjectId.js";
import { withMongoTransaction } from "../../utils/withMongoTransaction.js";
import { getRouteUnauthorizedService, verifyDriverOrAgencyAdminAccessService } from "../route/route.service.js";
import cleanRunPoints from "./run-utils/cleanRunPoints.js";
import { mapMatchRunPoints } from "./run-utils/matchRunPoints.js";
import {
    addRunPoint,
    addRunPredictionHistory,
    clearMatchedRunPoints,
    clearSimplifiedRunPoints,
    getMatchedRunPoints,
    getNearbyRuns,
    getRouteRun,
    getRunPoints,
    insertMatchedRunPoints,
    insertSimplifiedRunPoints,
    startRouteRun,
    updateRunCurrentPoint,
    updateRunPrediction,
    updateRunScore,
    updateRunStatus,
    resetRunPrediction,
    getRunWithHigherScore,
    getEndToEndRunId,
    getMostRecentFinishedRunId,
} from "./run.repository.js";
import { v4 as uuidv4 } from 'uuid';
import { getRoutePrediction, simplifyRoutePoints } from "./routesApi.repository.js";
import { getH3Cell } from "../../utils/h3Index.js";
import getPeriodKeys from "../../utils/getPeriodKeys.js";
import getUTCDate from "../../utils/getUTCDate.js";


/**
 * Función privada para reusar la lógica de agregar un punto a un recorrido.
 * Calcula la distancia al inicio y al final de la ruta para determinar si el punto está
 * cerca del inicio o del final.
 * @param runId ID del recorrido al que se le agregará el punto.
 * @param route Objeto que representa la ruta a la que pertenece el recorrido.
 * @param lat Latitud del punto a agregar.
 * @param long Longitud del punto a agregar.
 * @param speed Velocidad en el punto a agregar.
 * @param time Fecha y hora en la que se registra el punto.
 * @param startPoint Indica si el punto es el inicio del recorrido.
 * @param endPoint Indica si el punto es el final del recorrido.
 * @returns
 */
const _addRunPoint = async ({
    runId,
    route,
    lat,
    long,
    speed,
    time,
    accuracy,
}: {
    runId: string;
    route: Route;
    lat: number;
    long: number;
    speed: number;
    time: Date;
    accuracy: number;
}): Promise<RunPoint> => {
    const distanceToStart = getDistanceBetweenGeoPoints({
        lat1: route.startLocation.lat,
        long1: route.startLocation.long,
        lat2: lat,
        long2: long,
    });

    const distanceToEnd = getDistanceBetweenGeoPoints({
        lat1: route.endLocation.lat,
        long1: route.endLocation.long,
        lat2: lat,
        long2: long,
    });

    const isCloserToStart =
        distanceToStart <= consts.proximityThreshold.closeToStart;
    const isCloserToEnd = distanceToEnd <= consts.proximityThreshold.closeToEnd;

    return await addRunPoint({
        runId,
        routeId: route.id,
        lat,
        long,
        time,
        speed,
        accuracy,
        closeToStartLocation: isCloserToStart,
        closeToEndLocation: isCloserToEnd,
    });
};

/**
 * Inicia un recorrido en una ruta específica.
 * Verifica los permisos del usuario para iniciar el recorrido.
 * Agrega el primer punto al recorrido con la ubicación inicial del usuario y lo marca como el
 * inicio del recorrido.
 * @param routeId ID de la ruta en la que se iniciará el recorrido.
 * @param userId ID del usuario que inicia el recorrido.
 * @param time Fecha y hora en la que se inicia el recorrido.
 * @param lat Latitud de la ubicación inicial del recorrido.
 * @param long Longitud de la ubicación inicial del recorrido.
 * @param speed Velocidad del usuario al iniciar el recorrido.
 * @param session Objeto de sesión del usuario.
 * @throws {NotFoundError} Si la ruta especificada no existe.
 * @throws {UnauthorizedError} Si el usuario no tiene permiso para iniciar un recorrido en la ruta.
 * @throws {InvalidObjectIdError} Si el ID proporcionado para el usuario o la ruta no es un ObjectId válido.
 * @returns
 */
const startRouteRunService = async ({
    routeId,
    userId,
    time,
    session,
}: {
    routeId: string;
    userId: string;
    time: Date;
    session?: Session;
}): Promise<Run> => {

    const route = await  getRouteUnauthorizedService({ routeId });

    if (!route) {
        throw new NotFoundError("La ruta especificada no existe.");
    }

    const hasAccess = await verifyDriverOrAgencyAdminAccessService({
        routeId,
        agencyId: route.agency.id,
        userId,
        session,
    });

    if (!hasAccess) {
        throw new UnauthorizedError(
            "No tienes permiso para iniciar un recorrido en esta ruta.",
        );
    }

    // Crear el recorrido
    const run = await startRouteRun({
        routeId,
        agencyId: route.agency.id,
        userId,
        time,
        status: consts.runStatus.active,
        lastUpdated: new Date(),
        session,
    });

    return {
        id: run.id,
        routeId: run.routeId,
        agencyId: run.agencyId,
        time: run.time,
    };
};

const _getRunPrediction = async ({
    routeId,
    runId,
    start,
    destination,
    prevPrediction,
    userId
}: {
    routeId: string;
    runId: string;
    start: LatLong;
    destination: LatLong;
    prevPrediction: string | null;
    userId: string;
}) : Promise<[string | null, number | null, {type:string, data:string}[]]> => {

    const runPoints = await getRunPoints(runId);

    // Required edges = false para considerar que el trayecto inició tarde o no ha finalizado
    const cleanedRunPoints = cleanRunPoints({ runPoints, requiredEdges: false });
    if (!cleanedRunPoints.length) return [null, null, []];

    // Seleccionar solo último trayecto
    const lastSegment = cleanedRunPoints[cleanedRunPoints.length - 1];
    if (!lastSegment || lastSegment.length < consts.minNodesForPrediction) return [null, null, []];

    
    // Obtener token de autorización para api de rutas
    const token = signRouteResourceToken({ userId });
    
    return await getRoutePrediction({
        routeId,
        coords: lastSegment.map(point => point.location),
        start,
        destination,
        prevPrediction,
        token
    })
    
};


const addRunPointService = async ({
    runId,
    userId,
    lat,
    long,
    speed,
    time,
    accuracy,
    session,
}: {
    runId: string;
    userId: string;
    lat: number;
    long: number;
    speed: number;
    time: Date;
    accuracy: number;
    session?: Session;
}): Promise<RunPoint> => {
    const run = await getRouteRun(runId);

    if (!run) {
        throw new NotFoundError("El recorrido especificado no existe.");
    }

    const route = await getRouteUnauthorizedService({ routeId: run.routeId });

    if (!route) {
        throw new NotFoundError("La ruta especificada no existe.");
    }

    // Validar permiso de acceso directo de conductor o admin agency
    const hasAccess = await verifyDriverOrAgencyAdminAccessService({
        routeId: run.routeId,
        agencyId: route.agency.id,
        userId,
        session,
    });

    if (!hasAccess) {
        throw new UnauthorizedError(
            "No tienes permiso para agregar ubicaciones en este viaje",
        );
    }
    
    // Verificar si el usuario fue el que inició el recorrido
    const isRunOwner = run.userId.toString() === userId;

    if (!isRunOwner) {
        throw new UnauthorizedError(
            "No tienes permiso para agregar un punto a un recorrido que no iniciaste.",
        );
    }

    // Agregar el punto al recorrido
    const runPoint = await _addRunPoint({
        runId,
        route,
        lat,
        long,
        speed,
        time,
        accuracy,
    });

    // Calcular la distancia desde el punto anterior al actual
    const distanceFromLastPoint = run.currentPoint != null ? getDistanceBetweenGeoPoints({
        lat1: run.currentPoint.lat,
        long1: run.currentPoint.long,
        lat2: lat,
        long2: long,
    }) : 0;

    // Actualizar punto actual en documento de ruta (previo a realizar predicción)
    await updateRunCurrentPoint({
        runId,
        currentPoint: runPoint.location,
        lastUpdated: time,
        increaseDistanceFromLastPrediction: distanceFromLastPoint,
        session,
    });

    let newRoutePrediction: string | null = null;
    let routePredictionMetric: number | null = null;
    let routePredictionLogs: {type:string, data:string}[] = [];

    // Se actualiza la predicción si:
    // - No existe una predicción previa
    // - Ha pasado el tiempo mínimo entre predicciones
    // - Se ha recorrido la distancia mínima acumulada desde la última predicción
    const updateRoutePrediction = (
        !run.lastPrediction 
        || (time.getTime() - run.lastPrediction.getTime()) > consts.predictionFrequency * 1000
        || (run.distanceFromLastPrediction
                && run.distanceFromLastPrediction + distanceFromLastPoint >= consts.minAccumDistanceToStartPrediction)
    ) && run.predictionInProgress === false;

    // Verificar si corresponde realizar una nueva predicción según frecuencia (o si no existe aún)
    if (updateRoutePrediction) {

        try{

            // Realizar nueva predicción
            [newRoutePrediction, routePredictionMetric, routePredictionLogs] = await _getRunPrediction({
                routeId: route.id,
                runId,
                start: route.startLocation,
                destination: route.endLocation,
                prevPrediction: run.routePrediction ?? null,
                userId,
            })


            await withMongoTransaction(async (internalSession: Session) => {

                // Actualizar predicción en documento de ruta
                await updateRunPrediction({
                    runId,
                    routePrediction: newRoutePrediction,
                    lastPredictionDate: time,
                    session: session || internalSession,
                });

                if (updateRoutePrediction) {
                    await addRunPredictionHistory({
                        runId,
                        runPointId: runPoint.id,
                        routePrediction: newRoutePrediction,
                        metric: routePredictionMetric,
                        logs: routePredictionLogs,
                        createdAt: runPoint.time,
                        session: session || internalSession,
                    });
                }
            });

        }catch(err){
            console.error("Error al obtener predicción de ruta:", err);

            await resetRunPrediction({
                runId,
                session,
            });
        }
        
    }
    return runPoint;
};

/**
 * Realiza el proceso de finalización de una ruta. Esto incluye limpiar el recorrido, obteniendo
 * subgrafos de inicio a fin, simplificar con RDP y hacer map matching de los subgrafos obtenidos.
 * @param runId ID del recorrido a finalizar.
 * @param userId ID del usuario que finaliza el recorrido.
 * @throws {NotFoundError} Si el recorrido no existe.
 * @throws {UnauthorizedError} Si el usuario no tiene permiso para finalizar el recorrido.
 * @throws {InvalidObjectIdError} Si el ID del recorrido o del usuario no es válido.
 * @returns Promise<boolean> Indica si el recorrido contiene rutas válidas end-to-end.
 */
const finishRouteRunService = async ({
    runId,
    userId,
}: {
    runId: string;
    userId: string;
}) : Promise<boolean> => {

    // Obtener definición del viaje
    const run = await getRouteRun(runId);

    if (!run) {
        throw new NotFoundError("El recorrido especificado no existe.");
    }

    // Verificar que quien finaliza es el mismo que inició el recorrido
    if (parseObjectId(run.userId, "runUserId").toString() !== parseObjectId(userId, "sessionUserId").toString()) {
        throw new UnauthorizedError("No tienes permiso para finalizar este recorrido.");
    }

    // Obtener puntos del recorrido
    const originalRunPoints = await getRunPoints(runId);

    if (originalRunPoints.length === 0) {
        throw new NotFoundError("El recorrido no tiene puntos registrados.");
    }

    const cleanedRunPoints = cleanRunPoints({runPoints: originalRunPoints});

    const hasEndToEndRuns = cleanedRunPoints.length > 0;

    // Actualizar estado de viaje a finalizado
    await updateRunStatus({
        runId,
        status: hasEndToEndRuns ? consts.runStatus.finished : consts.runStatus.finishedIncomplete,
        lastUpdated: null,
    });

    // Sumar al score del recorrido seleccionado como predicción
    if(run.routePrediction !== null && run.routePrediction !== undefined){
        const periodKeys = getPeriodKeys(new Date());
        const runTypes: { type: RunScorePeriodType; priority: number; key: string }[] = [
            { type: "day", priority: consts.scorePeriodPriority.day, key: periodKeys.dayKey },
            { type: "week", priority: consts.scorePeriodPriority.week, key: periodKeys.weekKey },
            { type: "month", priority: consts.scorePeriodPriority.month, key: periodKeys.monthKey },
            { type: "total", priority: consts.scorePeriodPriority.general, key: "" }
        ];

        await Promise.all(
            runTypes.map((period) =>  
                updateRunScore({
                    routeId: run.routeId,
                    endToEndRunId: run.routePrediction as string,
                    periodType: period.type,
                    periodKey: period.key,
                    periodPriority: period.priority,
                    updatedAt: new Date(),
                })
            )
        );
    }

    if (!hasEndToEndRuns) {
        return false;
    }

    await withMongoTransaction(async (session: Session) => {

        await Promise.all( 
            cleanedRunPoints.map(async (endToEndRunPoints) => {

                // Asignar un ID único para el subgrafo del viaje
                const endToEndRunId = uuidv4();

                // Hacer map matching
                const [matchedGPSPoints, mappedFlags] = await mapMatchRunPoints(endToEndRunPoints);

                const matchedRunPoints:MatchedRunPoint[] = matchedGPSPoints.map((point, index) =>{

                    const h3Index = getH3Cell({
                        location: point,
                        resolution: consts.h3GeoIndexResolution.matchedRunPoint,
                    })

                    return {
                        runId,
                        routeId: run.routeId,
                        location: point,
                        seq: index,
                        endToEndRunId,
                        onNetwork: mappedFlags[index],
                        geoIndex: h3Index,
                    };
                });

                // Limpiar puntos mapeados previos (conservar solo los nuevos)
                await clearMatchedRunPoints({ runId, session });

                // Insertar puntos mapeados
                await insertMatchedRunPoints({ points: matchedRunPoints, session });


                 // Simplificar ruta mapeada con algoritmo ramer-douglas-peucker

                // Obtener token de autorización para api de rutas
                const token = signRouteResourceToken({ userId });

                const simplifiedRun = await simplifyRoutePoints({
                    coords: matchedGPSPoints,
                    token
                })

                const simplifiedRunPoints= simplifiedRun.map((location, index) => ({
                    runId,
                    routeId: run.routeId,
                    location,
                    seq: index,
                    endToEndRunId,
                }));


                // Limpiar rutas guardadas previas
                await clearSimplifiedRunPoints({runId, session});

                // Guardar puntos de ruta simplificados
                await insertSimplifiedRunPoints({ points: simplifiedRunPoints, session });

            })
        );
    });

    return true;
};

/**
 * Busca los recorridos activos que actualmente se encuentran cerca de una ubicación destino.
 * Debe estar dentro del radio máximo definido en las constantes, encontrarse en estado activo y
 * haber reportado su última ubicación dentro del tiempo de vida definido en las constantes.
 * @param destination Ubicación destino con latitud y longitud.
 * @returns Promise<NearbyRun[]> Lista de recorridos cercanos.
 */
const getNearbyRunsService = async ({ destination }: { destination: LatLong }) : Promise<NearbyRun[]> => {
    const nearbyRuns = await getNearbyRuns({ destination });
    return nearbyRuns;
};

/**
 * Obtener únicamente las ubicaciones emparejadas (map-matching) de un recorrido para un grafo end-to-end específico.
 * @param endToEndRunId ID del grafo end-to-end.
 * @returns Promise<LatLong[]> Lista de ubicaciones emparejadas.
 * @throws {NotFoundError} Si no se encuentran puntos mapeados para el ID de recorrido proporcionado.
 */
const getMatchedRunPointsService = async ({ endToEndRunId }:{ endToEndRunId:string }): Promise<LatLong[]> => {
    const matchedPoints = await getMatchedRunPoints({ endToEndRunId });

    if(!matchedPoints || matchedPoints.length === 0){
        throw new NotFoundError("No se encontraron puntos mapeados para el ID de recorrido proporcionado.");
    }
    return matchedPoints;
}
/**
 * Obtiene los puntos emparejados (map-matching) del recorrido más reciente para una ruta específica.
 * @param routeId ID de la ruta.
 * @param session Objeto de sesión de MongoDB.
 * @returns Promise<MatchedRunPoint[]> Lista de puntos emparejados del recorrido más reciente.
 */
const getMostRecentMatchedRunPointsInternalService = async ({
    routeId,
    session
}: {
    routeId: string;
    session?: Session;
}): Promise<LatLong[]> => {
    
    // Obtener runID más reciente
    const mostRecentRunID =  await getMostRecentFinishedRunId({
        routeId,
        session
    });

    if(!mostRecentRunID) return [];

    // Obtener puntos end-to-end del recorrido más reciente
    const endToEndRunIds = await getEndToEndRunId({
        runId: mostRecentRunID,
        session
    });

    if (endToEndRunIds.length === 0) return [];

    return getMatchedRunPoints({
        endToEndRunId: endToEndRunIds[0],
        session
    })
}

/**
 * Obtener los puntos emparejados (map-matching) de un recorrido con el puntaje más alto para una ruta específica.
 * @param routeId ID de la ruta.
 * @param session Objeto de sesión de MongoDB.
 * @throws {InvalidObjectIdError} Si el ID de la ruta no es un ObjectId válido.
 * @returns Promise<LatLong[] | null> Lista de puntos emparejados del recorrido con el puntaje más
 * alto, o null si no se encuentra ninguno.
 */
const getRunPointsWithHigherScoreService = async ({
    routeId,
    session,
}:{
    routeId: string;
    session?: Session;
}) => {

    const keys = getPeriodKeys(getUTCDate(null));

    const runWithHigherScore = await getRunWithHigherScore({
        routeId,
        keys,
        minScore: consts.minScoreToConsiderForBestRuns,
        session,
    })

    if(!runWithHigherScore) return null;

    // Si existe un recorrido con puntaje alto, obtener sus puntos mapeados
    const matchedPoints = await getMatchedRunPoints({ 
        endToEndRunId: runWithHigherScore,
        session
    });

    return matchedPoints;
}

const testOSRMService = async ({
    runId,
}: {
    runId: string;
}) : Promise<boolean> => {

    // Obtener puntos del recorrido
    const originalRunPoints = await getRunPoints(runId);

    if (originalRunPoints.length === 0) {
        throw new NotFoundError("El recorrido no tiene puntos registrados.");
    }

    const cleanedRunPoints = cleanRunPoints({runPoints: originalRunPoints});

    if (cleanedRunPoints.length === 0) {
        return false;
    }

     // Hacer map matching
    //const mapMatching = await mapMatchRunPoints(cleanedRunPoints[0]);

    return true;
};

export {
    startRouteRunService,
    addRunPointService,
    finishRouteRunService,
    getNearbyRunsService,
    getMatchedRunPointsService,
    getMostRecentMatchedRunPointsInternalService,
    testOSRMService,
    getRunPointsWithHigherScoreService,
};