type User = {
    readonly id: string;
    email: string;
    name: string;
    lastname: string;
    role: number;
};

const enum TokenType {
    REFRESH_TOKEN = 1,
    ACCESS_TOKEN = 2,
    DEVICE_TOKEN = 3,
    ROUTE_RESOURCE_TOKEN = 4,
}

type ApiResponse = {
    ok: boolean;
    message?: string;
};

type UploadedFile = {
    fileName: string;
    type: string;
}

type session = {
    session?: User;
    uploadedFiles?: UploadedFile[];
};

type Agency = {
    id: string;
    name: string;
};

type LatLong = {
    lat: number;
    long: number;
}

type Schedule = {
    day: number;
    open: string | null;
    close: string | null;
    serviceAvailable: boolean;
}

type Route = {
    id: string;
    agency: Agency;
    startLocation: LatLong;
    endLocation: LatLong;
    startLocationGeoIndex: string;
    endLocationGeoIndex: string;
    name: string;
    schedules: Schedule[];
    units: string[];
    unitImages: string[];
    stops: string[];
}

type RoutePublicData = {
    id: string,
    name: string,
    unitImages: string[],
    schedules: Schedule[],
}

type Permission = {
    id: string;
    role: number;
}

type Run = {
    id: string;
    routeId: string;
    agencyId: string;
    time: Date;
}

type RunWithFullData = Run & {
    userId: string;
    status: number;
    currentPoint?: LatLong | null;
    routePrediction?: string | null;
    lastUpdated?: Date | null;
    lastPrediction?: Date | null;
    distanceFromLastPrediction?: number | null;
    predictionInProgress: boolean;
}

type RunPoint = {
    id: string;
    runId: string;
    routeId: string;
    location: LatLong;
    time: Date;
    speed: number;
    accuracy: number;
    closeToStartLocation?: boolean;
    closeToEndLocation?: boolean;
    startPoint?: boolean;
    endPoint?: boolean;
}

type RunPointLite = {
    runId: string;
    location: LatLong;
    routeId: string;
    seq: number;
}

type SimplifiedRunPoint = RunPointLite & {
    endToEndRunId: string;
}

type MatchedRunPoint = RunPointLite & {
    endToEndRunId: string;
    onNetwork: boolean;
    geoIndex: string;
}

type NearbyRun = {
    runId: string;
    routeId: string;
    location: LatLong;
    routePrediction: string | null;
}

type RouteEmbedded = {
    id: string;
    name: string;
}

type RouteStop = {
    id: string;
    name: string;
    location: LatLong;
    agency: Agency;
    routes: RouteEmbedded[];
}

type NearbyStopByRoute = {
    route: RouteEmbedded;
    stop: RouteStop;
    distance: number;
}

type DeviceId = readonly string;