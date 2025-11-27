type CreateRouteResponse = ApiResponse & {
    route: Route;
};

type GetRoutesListResponse = ApiResponse & {
    routes: Route[];
};

type GetRouteResponse = ApiResponse & {
    route: Route | null;
};

type GetRoutePublicDataResponse = ApiResponse & {
    route: RoutePublicData;
};


type GetRouteStopsResponse = ApiResponse & {
    routeStops: RouteStop[];
};

type UpdateRouteStopsResponse = ApiResponse

type GetRouteBestRunResponse = ApiResponse & {
    points: LatLong[];
};