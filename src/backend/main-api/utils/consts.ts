const consts = {
    apiPath: "/api",
    token: {
        refresh: 1 as TokenType.REFRESH_TOKEN,
        access: 2 as TokenType.ACCESS_TOKEN,
        device: 3 as TokenType.DEVICE_TOKEN,
        routeResource: 4 as TokenType.ROUTE_RESOURCE_TOKEN,
    },
    tokenExpiration: {
        refreshExpirationTime: 2592000, // 30 days in seconds
        accessExpirationTime: 900, // 15 minutes in seconds
    },
    roles: {
        default: 0,
        agencyAdmin: 1,
        driver: 2,
        admin: 3,
    },
    uploadImageSizeLimit: 5 * 1024 * 1024, // 5 MB in bytes
    maxUnitImages: 10,
    proximityThreshold: { // Metros
            closeToStart: 30,
            closeToEnd: 30,
    },
    routeServiceHost: "http://fastapi:8000",
    simplifyConfig:{
        tolerance: 0.00018, // aprox 20 m
        highQuality: true
    },
    minDistanceBetweenExtremes: 100, // m Distancia mínima entre extremos
    minDistanceForLateStart: 1000, // m Distancia mínima sin encontrar extremo de inicio para considerar que se dio un inicio tardío
    minNodesForPrediction: 50, // Número mínimo de nodos para comenzar a realizar predicciones
    predictionFrequency: 30, // Segundos entre predicciones
    maxWaitForRoutePredictionMs: 60000, // Milisegundos de espera máxima para obtener predicción de ruta
    minAccumDistanceToStartPrediction: 100, // Metros mínimos recorridos antes de iniciar predicciones
    osrmHost: "http://osrm:5000",
    runStatus: {
        active: 1,
        finished: 2,
        finishedIncomplete: 3,
    },
    unitLocationLifetime: 30, // Segundos
    maxRadiusForNearbyUnits: 30000,//5000, // Metros
    maxRadiusForNearbyStops: 30000,//10000, // Metros
    maxPointAccuracy: 100, // Metros
    h3GeoIndexResolution:{
        routeEdgePoints: 9, // Aprox radio 184.28 m
        matchedRunPoint: 12, // Aprox radio 9.95 m
    },

    scorePeriodPriority: {
        day: 1,
        week: 2,
        month: 3,
        general: 4,
    },

    minScoreToConsiderForBestRuns: 0, // Puntaje mínimo para considerar un recorrido al obtener los mejores recorridos cercanos
};

export default consts;
