import express from "express";
import validateBody from "../../middlewares/validateBody.js";
import loginSchema from "./validationSchemas/loginSchema.js";
import { generateDeviceTokenController, loginController, logoutController, refreshSessionTokenController } from "./session.controller.js";
import ensureRefreshTokenAuth from "../../middlewares/ensureRefreshTokenAuth.js";

const sessionRouter = express.Router();

sessionRouter.post("/login", validateBody(loginSchema), loginController);
sessionRouter.post("/refresh", ensureRefreshTokenAuth, refreshSessionTokenController);
sessionRouter.post("/logout", ensureRefreshTokenAuth, logoutController);
sessionRouter.post("/device", generateDeviceTokenController);

export default sessionRouter;
