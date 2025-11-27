import express from "express";
import validateBody from "../../middlewares/validateBody.js";
import createUserSchema from "./validationSchemas/createUserSchema.js";
import { createUserController } from "./user.controller.js";
import ensureAgencyAdminAuth from "../../middlewares/ensureAdminAuth.js";

const userRouter = express.Router();

userRouter.post(
    "/",
    ensureAgencyAdminAuth,
    validateBody(createUserSchema),
    createUserController,
);

export default userRouter;
