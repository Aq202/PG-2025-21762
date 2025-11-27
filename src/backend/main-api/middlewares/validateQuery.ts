import { AppNext, AppRequest, AppResponse } from "../types/express.js";

/* eslint-disable @typescript-eslint/no-explicit-any */
const validateQuery = (...schemas:any) => async (req:AppRequest, res:AppResponse, next:AppNext) => {
  try {
    await Promise.all(schemas?.map((schema:any) => schema.validate(req.query)));
    next();
  } catch (err:any) {
    res.statusMessage = err.message;
    res.status(400).send({ err: err.message, status: 400, ok: false });
  }
};

export default validateQuery;
