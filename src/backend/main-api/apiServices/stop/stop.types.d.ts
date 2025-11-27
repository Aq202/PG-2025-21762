type CreateRouteStopResponse = ApiResponse & {
    routeStop: RouteStop;
};

type getAllStopsResponse = ApiResponse & {
    routeStops: RouteStop[];
};

type getNearbyStopsByRouteResponse = ApiResponse & {
    nearbyStops: NearbyStop[];
}