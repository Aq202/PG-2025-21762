type StartRouteRunResponse = ApiResponse & {
    run: Run;
};

type AddRunPointResponse = ApiResponse;

type GetNearbyRunsResponse = ApiResponse & {
    runs: NearbyRun[];
};

type GetMatchedRunPointsResponse = ApiResponse & {
    points: LatLong[];
};

type RunScorePeriodType = "day" | "week" | "month" | "total";