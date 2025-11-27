import express from "express";
import validateBody from "../../middlewares/validateBody.js";
import ensureAgencyAdminAuth from "../../middlewares/ensureAdminAuth.js";
import multerMiddleware from "../../middlewares/multer.middleware.js";
import uploadFile from "../../services/uploadFile/uploadImage.js";
import createRouteSchema from "./validationSchemas/createRouteSchema.js";
import {
    createRouteController,
    getRouteBestRunController,
    getRouteController,
    getRoutePublicDataController,
    getRoutesListController,
    getRouteStopsController,
    updateRouteStopsController,
} from "./route.controller.js";
import { parseFormDataToJson } from "../../middlewares/parseFormDataToJson.js";
import consts from "../../utils/consts.js";
import ensureDriverAuth from "../../middlewares/ensureDriverAuth.js";
import validateQuery from "../../middlewares/validateQuery.js";
import getRoutesListSchema from "./validationSchemas/getRoutesListSchema.js";
import validateParams from "../../middlewares/validateParams.js";
import routeIdParamSchema from "./validationSchemas/routeIdParamSchema.js";
import updateRouteStopsSchema from "./validationSchemas/updateRouteStopsSchema.js";

const routeRouter = express.Router();

routeRouter.post(
    "/",
    ensureAgencyAdminAuth,
    multerMiddleware(uploadFile.array("unitImages"), consts.uploadImageSizeLimit),
    parseFormDataToJson(["startLocation", "endLocation", "schedules", "units"]),
    validateBody(createRouteSchema),
    createRouteController,
);

routeRouter.get(
    "/",
    ensureDriverAuth,
    validateQuery(getRoutesListSchema),
    getRoutesListController,
);

routeRouter.get(
    "/:routeId",
    ensureDriverAuth,
    validateParams(routeIdParamSchema),
    getRouteController,
);

routeRouter.get(
    "/:routeId/public",
    validateParams(routeIdParamSchema),
    getRoutePublicDataController,
);

routeRouter.get(
    "/:routeId/stop",
    ensureDriverAuth,
    validateParams(routeIdParamSchema),
    getRouteStopsController,
);

routeRouter.put(
    "/:routeId/stop",
    ensureAgencyAdminAuth,
    validateParams(routeIdParamSchema),
    validateBody(updateRouteStopsSchema),
    updateRouteStopsController,
)

routeRouter.get(
    "/:routeId/run/best",
    validateParams(routeIdParamSchema),
    getRouteBestRunController,
)

export default routeRouter;
