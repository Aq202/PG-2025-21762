import express from "express";
import validateBody from "../../middlewares/validateBody.js";
import ensureAgencyAdminAuth from "../../middlewares/ensureAdminAuth.js";
import {
    createRouteStopController,
    getNearbyStopsByRouteController,
    getStopsController,
} from "./stop.controller.js";
import createRouteStopSchema from "./validationSchemas/createRouteStopSchema.js";
import validateQuery from "../../middlewares/validateQuery.js";
import optionalAgencyIdSchema from "./validationSchemas/optionalAgencyIdSchema.js";
import getNearbyStopsByRouteSchema from "./validationSchemas/getNearbyStopsByRouteSchema.js";

const stopRouter = express.Router();

stopRouter.get(
    "/",
    validateQuery(optionalAgencyIdSchema),
    getStopsController
);


stopRouter.post(
    "/",
    ensureAgencyAdminAuth,
    validateBody(createRouteStopSchema),
    createRouteStopController,
);

stopRouter.get(
    "/nearby/by-route",
    validateQuery(getNearbyStopsByRouteSchema),
    getNearbyStopsByRouteController,
)

export default stopRouter;
