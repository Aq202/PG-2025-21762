import express from "express";
import validateBody from "../../middlewares/validateBody.js";
import ensureAgencyAdminAuth from "../../middlewares/ensureAdminAuth.js";
import createAgencySchema from "./validationSchemas/createAgencySchema.js";
import {
    assignAgencyAdminController,
    createAgencyController,
    getAgenciesController,
    verifyIfUserIsAgencyAdminController,
} from "./agency.controller.js";
import assignAgencyAdminSchema from "./validationSchemas/assignAgencyAdminSchema.js";
import ensureDriverAuth from "../../middlewares/ensureDriverAuth.js";
import ensureAuth from "../../middlewares/ensureAuth.js";

const agencyRouter = express.Router();

agencyRouter.post(
    "/",
    ensureAgencyAdminAuth,
    validateBody(createAgencySchema),
    createAgencyController,
);
agencyRouter.post(
    "/admin",
    ensureAgencyAdminAuth,
    validateBody(assignAgencyAdminSchema),
    assignAgencyAdminController,
);

agencyRouter.get(
    "/",
    ensureDriverAuth,
    getAgenciesController
);

agencyRouter.get(
    "/:agencyId/is-admin",
    ensureAuth,
    verifyIfUserIsAgencyAdminController,
)

export default agencyRouter;
