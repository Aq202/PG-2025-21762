import express from "express";
import consts from "../utils/consts.js";
import userRouter from "../apiServices/user/user.route.js";
import sessionRouter from "../apiServices/session/session.route.js";
import agencyRouter from "../apiServices/agency/agency.route.js";
import routeRouter from "../apiServices/route/route.route.js";
import runRouter from "../apiServices/run/run.route.js";
import stopRouter from "../apiServices/stop/stop.route.js";

const router = express.Router();

const { apiPath } = consts;

router.use(`${apiPath}/user`, userRouter);
router.use(`${apiPath}/session`, sessionRouter);
router.use(`${apiPath}/agency`, agencyRouter);
router.use(`${apiPath}/route/run`, runRouter);
router.use(`${apiPath}/route`, routeRouter);
router.use(`${apiPath}/stop`, stopRouter)

router.get("/", (req, res) => {
  res.json({ message: "Welcome to subiteYa API" });
});

export default router;
