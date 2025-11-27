import express from "express";
import validateBody from "../../middlewares/validateBody.js";
import {
    addRunPointController,
    finishRouteRunController,
    getMatchedRunPointsController,
    getNearbyRunsController,
    postMapMatchingTestController,
    startRouteRunController,
} from "./run.controller.js";
import ensureDriverAuth from "../../middlewares/ensureDriverAuth.js";
import startRouteRunSchema from "./validationSchemas/startRouteRunSchema.js";
import validateParams from "../../middlewares/validateParams.js";
import runIdParamSchema from "./validationSchemas/runIdParamSchema.js";
import addRunPointSchema from "./validationSchemas/addRunPointSchema.js";
import validateQuery from "../../middlewares/validateQuery.js";
import getNearbyRunsSchema from "./validationSchemas/getNearbyRunsSchema.js";

const runRouter = express.Router();


runRouter.post(
    "/:runId/point",
    ensureDriverAuth,
    validateParams(runIdParamSchema),
    validateBody(addRunPointSchema),
    addRunPointController,
);

runRouter.post(
    "/",
    ensureDriverAuth,
    validateBody(startRouteRunSchema),
    startRouteRunController,
);

runRouter.post(
    "/:runId/finish",
    ensureDriverAuth,
    validateParams(runIdParamSchema),
    finishRouteRunController,
);

runRouter.get(
    "/nearby",
    validateQuery(getNearbyRunsSchema),
    getNearbyRunsController,
);

runRouter.get(
    "/matched-points/:endToEndRunId",
    getMatchedRunPointsController,
);

runRouter.get(
    "/:runId/map-matching-test",
    postMapMatchingTestController,
)

export default runRouter;
