// Coordenadas en formato [lon, lat] (OSRM usa lon primero)
export type OsrmCoordinate = [number, number];

export interface OsrmGeometry {
    coordinates: OsrmCoordinate[];
    type: "LineString";
}

export interface OsrmLeg {
    steps: unknown[];
    distance: number;
    duration: number;
    summary: string;
    weight: number;
}

export interface OsrmMatching {
    confidence: number;
    geometry: OsrmGeometry;
    legs: OsrmLeg[];
    distance: number;
    duration: number;
    weight_name: string;
    weight: number;
}

export interface OsrmTracepoint {
    alternatives_count: number;
    waypoint_index: number;
    matchings_index: number;
    location: OsrmCoordinate;
    name: string;
    distance: number;
    hint: string;
}

export interface OsrmMatchResponse {
    code: string;
    matchings: OsrmMatching[];
    tracepoints: (OsrmTracepoint | null)[];
}
