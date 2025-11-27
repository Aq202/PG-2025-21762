import parseObjectId from "../../utils/parseObjectId.js";
import { Session, SessionStrict } from "../../types/db.js";
import RunSchema from "../../db/mongodb/schemas/run.schema.js";
import RunPointSchema from "../../db/mongodb/schemas/runPoint.schema.js";
import SimplifiedRunPointSchema from "../../db/mongodb/schemas/SimplifiedRunPoint.schema.js";
import MatchedRunPointSchema from "../../db/mongodb/schemas/MatchedRunPoint.schema.js";
import RunPrediction from "../../db/mongodb/schemas/runPrediction.schema.js";
import consts from "../../utils/consts.js";
import RunScoreSchema from "../../db/mongodb/schemas/runScore.schema.js";
import parseDateToUTC from "../../utils/parseDateToUTC.js";

/**
 * Inicia un nuevo recorrido para una ruta específica.
 * @param routeId ID de la ruta.
 * @param agencyId ID de la agencia de transporte.
 * @param time Hora de inicio del recorrido.
 * @param session Objeto de sesión.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId
 * @returns
 */
const startRouteRun = async ({
    routeId,
    agencyId,
    userId,
    time,
    status,
    lastUpdated,
    session,
}: {
    routeId: string;
    agencyId: string;
    userId: string;
    time: Date;
    status: number;
    lastUpdated: Date;
    session?: Session;
}): Promise<RunWithFullData> => {
    const run = new RunSchema();
    run.routeId = parseObjectId(routeId, "routeId");
    run.agencyId = parseObjectId(agencyId, "agencyId");
    run.userId = parseObjectId(userId, "userId");
    run.time = parseDateToUTC(time)!;
    run.status = status;
    run.lastUpdated = parseDateToUTC(lastUpdated)!;
    run.currentPoint = null;

    const result = await run.save({ session });

    return {
        id: result._id.toString(),
        routeId,
        agencyId,
        time,
        userId: result.userId.toString(),
        status: result.status,
        lastUpdated: result.lastUpdated,
        predictionInProgress: false,
    };
};

/**
 * Agregar un punto en el grafo de un recorrido.
 * @param runId ID del recorrido.
 * @param routeId ID de la ruta asociada al recorrido.
 * @param lat Latitud del punto.
 * @param long Longitud del punto.
 * @param time Hora en la que se registro esa ubicación.
 * @param speed Velocidad del punto.
 * @param closeToStartLocation Indica si el punto está cerca del inicio de la ruta.
 * @param closeToEndLocation Indica si el punto está cerca del final de la ruta.
 * @param StartPoint Indica si el punto es el inicio del recorrido.
 * @param EndPoint Indica si el punto es el final del recorrido.
 * @returns Promise<RunPoint> Objeto que representa el punto agregado al recorrido.
 */
const addRunPoint = async ({
    runId,
    routeId,
    lat,
    long,
    time,
    speed,
    accuracy,
    closeToStartLocation,
    closeToEndLocation,
    session,
}: {
    runId: string;
    routeId: string;
    lat: number;
    long: number;
    time: Date;
    speed: number;
    accuracy: number;
    closeToStartLocation: boolean;
    closeToEndLocation: boolean;
    session?: Session;
}): Promise<RunPoint> => {
    
    // Buscar último punto insertado
    const lastPoint = await RunPointSchema.findOne({ runId }).sort({seq: -1});

    let seq = 0;
    if (lastPoint){
        seq = lastPoint.seq + 1;
    }

    const newPoint = new RunPointSchema();

    newPoint.seq = seq;
    newPoint.routeId = parseObjectId(routeId, "routeId");
    newPoint.runId = parseObjectId(runId, "runId");
    newPoint.location = [long, lat];
    newPoint.time = parseDateToUTC(time)!;
    newPoint.speed = speed;
    newPoint.accuracy = accuracy;
    newPoint.closeToStartLocation = closeToStartLocation;
    newPoint.closeToEndLocation = closeToEndLocation;

    await newPoint.save({ session });

    return {
        id: newPoint._id.toString(),
        runId,
        routeId,
        location: { lat, long },
        time,
        speed,
        accuracy,
        closeToStartLocation,
        closeToEndLocation,
    }
};

/**
 * Obtiene la definición (en mongodb) de un recorrido por su ID.
 * @param runId ID del recorrido.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId
 * @returns Promise<Run | null> Objeto que representa el recorrido o null si no se encuentra.
 */
const getRouteRun = async (runId: string): Promise<RunWithFullData | null> => {
    const run = await RunSchema.findById(parseObjectId(runId, "runId"));
    if (!run) return null;

    return {
        id: run._id.toString(),
        routeId: run.routeId.toString(),
        agencyId: run.agencyId.toString(),
        time: run.time!,
        userId: run.userId.toString(),
        status: run.status,
        currentPoint: run.currentPoint ? {
            lat: run.currentPoint[1],
            long: run.currentPoint[0],
        } : null,
        routePrediction: run.routePrediction ?? null,
        lastUpdated: run.lastUpdated ?? null,
        lastPrediction: run.lastPrediction ?? null,
        distanceFromLastPrediction: run.distanceFromLastPrediction ?? null,
        predictionInProgress: run.predictionInProgress,
    };
}

/**
 * Obtiene los puntos de un recorrido por su ID. Los puntos corresponden al recorrido original.
 * @param runId ID del recorrido.
 * @returns Promise<RunPoint[]> Lista de puntos del recorrido.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const getRunPoints = async (runId: string): Promise<RunPoint[]> => {
    const runPoints = await RunPointSchema.find({ runId: parseObjectId(runId, "runId") }).sort({ seq: 1 });

    return runPoints.map((point) => ({
        id: point._id.toString(),
        runId: point.runId.toString(),
        routeId: point.routeId.toString(),
        location: {
            lat: point.location[1],
            long: point.location[0],
        },
        time: point.time,
        speed: point.speed,
        accuracy: point.accuracy,
        closeToStartLocation: point.closeToStartLocation,
        closeToEndLocation: point.closeToEndLocation,
    }));
};

/**
 * Elimina todos los puntos simplificados de un recorrido para todos los grafos end-to-end.
 * @param runId ID del recorrido.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 */
const clearSimplifiedRunPoints = async ({runId, session}:{runId:string, session?: Session}) => {
    await SimplifiedRunPointSchema.deleteMany({
        runId: parseObjectId(runId, "runId"),
    },{
        session: session as SessionStrict,
    });
}

/**
 * Inserta puntos de ruta simplificados en la base de datos para todos los grafos end-to-end.
 * @param points Lista de puntos de ruta simplificados.
 * @param session Objeto de sesión de MongoDB.
 */
const insertSimplifiedRunPoints = async ({points, session}: {points: SimplifiedRunPoint[], session?: Session}) => {
    const pointsToInsert = points.map(point => ({
        runId: point.runId,
        routeId: point.routeId,
        location: [point.location.long, point.location.lat],
        seq: point.seq,
        endToEndRunId: point.endToEndRunId,
    }))
    await SimplifiedRunPointSchema.insertMany(pointsToInsert, { session: session as SessionStrict });
};

/**
 * Elimina los puntos emparejados (map matching) de un recorrido para todos los grafos end-to-end.
 * @param runId ID del recorrido.
 * @param session Objeto de sesión de MongoDB.
 */
const clearMatchedRunPoints = async ({runId, session}:{runId:string, session?: Session}) => {
    await MatchedRunPointSchema.deleteMany({
        runId: parseObjectId(runId, "runId"),
    },{
        session: session as SessionStrict,
    });
}

/**
 * Inserta puntos emparejados (map matching) en la base de datos para todos los grafos end-to-end.
 * @param points Lista de puntos emparejados.
 * @param session Objeto de sesión de MongoDB.
 */
const insertMatchedRunPoints = async ({points, session}:{points: MatchedRunPoint[], session?: Session}) => {
    const pointsToInsert = points.map(point => ({
        runId: point.runId,
        routeId: point.routeId,
        location: [point.location.long, point.location.lat],
        seq: point.seq,
        endToEndRunId: point.endToEndRunId,
        onNetwork: point.onNetwork,
        geoIndex: point.geoIndex,
    }))
    await MatchedRunPointSchema.insertMany(pointsToInsert, { session: session as SessionStrict });
}

/**
 * Actualiza el estado de un recorrido.
 * @param runId ID del recorrido a actualizar.
 * @param status Nuevo estado del recorrido.
 * @param lastUpdated Fecha de última actualización.
 */
const updateRunStatus = async({
    runId,
    status,
    lastUpdated,
    session,
}: {
    runId: string;
    status: number;
    lastUpdated: Date | null;
    session?: Session;
}) => {

    const updateData:{status:number, lastUpdated?: Date } = { status };

    if (lastUpdated) {
        updateData["lastUpdated"] = parseDateToUTC(lastUpdated)!;
    }

    await RunSchema.updateOne(
        { _id: parseObjectId(runId, "runId") },
        {
            $set: updateData,
        },
        { session: session as SessionStrict }
    );
}

/**
 * Actualiza el punto actual  de un recorrido.
 * @param runId ID del recorrido a actualizar.
 * @param currentPoint Nuevo punto actual del recorrido.
 * @param lastUpdated Fecha de última actualización.
 * @param increaseDistanceFromLastPrediction Incremento en la distancia desde la última predicción.
 * @param session Objeto de sesión de MongoDB.
 */
const updateRunCurrentPoint = async ({
    runId,
    currentPoint,
    lastUpdated,
    increaseDistanceFromLastPrediction,
    session
}: {
    runId: string;
    currentPoint: LatLong;
    lastUpdated: Date;
    increaseDistanceFromLastPrediction: number;
    session?: Session;
}) => {
    const updateFields = {
        currentPoint: [currentPoint.long, currentPoint.lat],
        lastUpdated: parseDateToUTC(lastUpdated),
    };

    await RunSchema.updateOne(
        { _id: parseObjectId(runId, "runId") },
        { 
            $set: updateFields,
            $inc: {
                distanceFromLastPrediction: increaseDistanceFromLastPrediction ?? 0
            }
        },
        { session: session as SessionStrict }
    );
};

/**
 * Actualiza la predicción de ruta de un recorrido.
 * @param runId ID del recorrido a actualizar.
 * @param routePrediction Nueva predicción de ruta.
 * @param lastPredictionDate Fecha de la última predicción.
 * @param session Objeto de sesión de MongoDB.
 */
const updateRunPrediction = async ({
    runId,
    routePrediction,
    lastPredictionDate,
    session
}: {
    runId: string;
    routePrediction: string | null;
    lastPredictionDate: Date | null;
    session?: Session;
}) => {
    const updateFields: Record<string, unknown> = {
        routePrediction,
        lastPrediction: parseDateToUTC(lastPredictionDate),
        distanceFromLastPrediction: 0,
        predictionInProgress: false,
    };

    await RunSchema.updateOne(
        { _id: parseObjectId(runId, "runId") },
        { $set: updateFields },
        { session: session as SessionStrict }
    );
};

/**
 * Elimina la predicción de ruta de un recorrido.
 * @param runId ID del recorrido a actualizar.
 * @param session Objeto de sesión de MongoDB.
 */
const resetRunPrediction = async ({
    runId,
    session
}: {
    runId: string;
    session?: Session;
}) => {
    const updateFields: Record<string, unknown> = {
        routePrediction: null,
        lastPrediction: null,
        predictionInProgress: false,
    };

    await RunSchema.updateOne(
        { _id: parseObjectId(runId, "runId") },
        { $set: updateFields },
        { session: session as SessionStrict }
    );
};

/**
 * Agrega un nuevo registro al historial de predicciones de un recorrido.
 * @param runId ID del recorrido.
 * @param runPointId ID del punto asociado al recorrido.
 * @param routePrediction Predicción de la ruta.
 * @param session Sesión opcional de la base de datos.
 */
const addRunPredictionHistory = async ({
    runId,
    runPointId,
    routePrediction,
    metric,
    logs,
    createdAt,
    session
}: {
    runId: string;
    runPointId: string;
    routePrediction: string | null;
    metric: number | null;
    logs: {type:string, data:string}[];
    createdAt: Date;
    session?: Session;
}) => {
    const prediction = new RunPrediction({
        runId,
        runPointId,
        routePrediction,
        metric,
        logs,
        createdAt: parseDateToUTC(createdAt),
    });

    await prediction.save({ session });
    return prediction;
};

/**
 * Busca los recorridos activos que actualmente se encuentran cerca de una ubicación destino.
 * Debe estar dentro del radio máximo definido en las constantes, encontrarse en estado activo y
 * haber reportado su última ubicación dentro del tiempo de vida definido en las constantes.
 * @param destination Ubicación destino con latitud y longitud.
 * @returns Promise<NearbyRun[]> Lista de recorridos cercanos.
 */
const getNearbyRuns = async ({ destination }: { destination: LatLong }) : Promise<NearbyRun[]> => {
    const nearbyRunResult = await RunSchema.find({
        currentPoint: {
            $near: {
                $geometry: {
                    type: "Point",
                    coordinates: [destination.long, destination.lat]
                },
                $maxDistance: consts.maxRadiusForNearbyUnits
            }
        },
        lastUpdated: { 
            $gte: new Date(Date.now() - consts.unitLocationLifetime * 1000).toISOString()
        },
        status: consts.runStatus.active
    }, { _id: 1, routeId: 1, currentPoint: 1, routePrediction: 1 });

    return nearbyRunResult.map(run => ({
        runId: run._id.toString(),
        routeId: run.routeId.toString(),
        location: {
            lat: run.currentPoint![1],
            long: run.currentPoint![0],
        },
        routePrediction: run.routePrediction ?? null,
    }))
}

/**
 * Obtener únicamente las ubicaciones emparejadas (map-matching) de un recorrido para un grafo end-to-end específico.
 * @param endToEndRunId ID del grafo end-to-end.
 * @param session Objeto de sesión de MongoDB.
 * @returns Promise<LatLong[]> Lista de ubicaciones emparejadas.
 */
const getMatchedRunPoints = async ({
    endToEndRunId,
    session
}: {
    endToEndRunId: string;
    session?: Session
}): Promise<LatLong[]> => {
    const matchedPoints = await MatchedRunPointSchema.find({ endToEndRunId }).sort({ seq: 1 }).session(session as SessionStrict);
    return matchedPoints.map(point => ({
        lat: point.location[1],
        long: point.location[0],
    }));
}

/**
 * Obtiene el id del recorrido más reciente finalizado para una ruta específica.
 * @param routeId ID de la ruta.
 * @param session Objeto de sesión de MongoDB.
 * @returns Promise<string | null> ID del recorrido más reciente o null si no se encuentra ninguno.
 */
const getMostRecentFinishedRunId = async ({
    routeId,
    session
}: {
    routeId: string;
    session?: Session;
}): Promise<string | null> => {
    // Buscar el recorrido más reciente para la ruta dada
    const mostRecentRun = await RunSchema.findOne({
        routeId: parseObjectId(routeId, "routeId"),
        status: consts.runStatus.finished,
    })
    .sort({ time: -1 })
    .session(session as SessionStrict);
    if (!mostRecentRun) {
        return null;
    }

    return mostRecentRun._id.toString();
}

/**
 * Obtiene los IDs de los grafos end-to-end asociados a un recorrido.
 * @param runId ID del recorrido.
 * @param session Objeto de sesión de MongoDB.
 * @returns Promise<string[]> Lista de IDs de grafos end-to-end.
 */
const getEndToEndRunId = async ({
    runId,
    session
}: {
    runId: string;
    session?: Session;
}): Promise<string[]> => {
    const endToEndRunIds = await SimplifiedRunPointSchema.distinct("endToEndRunId", {
        runId: parseObjectId(runId, "runId"),
    }).session(session as SessionStrict);

    return endToEndRunIds.map(id => id.toString()).sort();
}

const updateRunScore = async ({
    routeId,
    endToEndRunId,
    periodType,
    periodKey,
    periodPriority,
    updatedAt
}: {
    routeId: string;
    endToEndRunId: string;
    periodType: RunScorePeriodType;
    periodKey: string;
    periodPriority: number;
    updatedAt: Date;
}) => {
    const routeIdObj = parseObjectId(routeId, "routeId");
    await RunScoreSchema.updateOne({
        routeId: routeIdObj,
        endToEndRunId,
        "period.type": periodType,
        "period.key": periodKey,
    }, {
        $inc: { score: 1 },
        $setOnInsert: {
            routeId: routeIdObj,
            updatedAt: parseDateToUTC(updatedAt),
            period: {
                type: periodType,
                key: periodKey,
                priority: periodPriority
            }
        }
    }, {
        upsert: true
    })

}

/**
 * 
 * Obtiene endToEndRunId de un recorrido con el puntaje más alto para una ruta específica.
 * @param routeId ID de la ruta.
 * @param keys Claves de periodo para día, semana y mes.
 * @param minScore Puntaje mínimo requerido (si un periodo es menor, se descarta y se toma periodo superior).
 * @param session Objeto de sesión de MongoDB.
 * @returns Promise<string | null> endToEndRunId del recorrido con el puntaje más alto o null si no se encuentra ninguno.
 */
const getRunWithHigherScore = async ({
    routeId,
    keys,
    minScore,
    session,
}: {
    routeId: string;
    keys: { dayKey: string; weekKey: string; monthKey: string };
    minScore: number;
    session?: Session;
}): Promise<string | null> => {

    const keyConditions = [
        { "period.type": "day", key: keys.dayKey },
        { "period.type": "week", key: keys.weekKey },
        { "period.type": "month", key: keys.monthKey },
        { "period.type": "total" }
    ];

    const doc = await RunScoreSchema
        .findOne(
            {
                routeId: parseObjectId(routeId, "routeId"),
                $or: keyConditions,
                score: { $gte: minScore }
            },
            { endToEndRunId: 1, _id: 0 }
        )
        .sort({
            priority: 1,
            score: -1,
            updatedAt: -1
        })
        .session(session || null)
        .lean();

    return doc?.endToEndRunId || null;
};

export {
    startRouteRun,
    addRunPoint,
    getRouteRun,
    getRunPoints,
    clearSimplifiedRunPoints,
    insertSimplifiedRunPoints,
    clearMatchedRunPoints,
    insertMatchedRunPoints,
    updateRunStatus,
    addRunPredictionHistory,
    getNearbyRuns,
    getMatchedRunPoints,
    getMostRecentFinishedRunId,
    getEndToEndRunId,
    updateRunScore,
    updateRunCurrentPoint,
    updateRunPrediction,
    resetRunPrediction,
    getRunWithHigherScore,
};