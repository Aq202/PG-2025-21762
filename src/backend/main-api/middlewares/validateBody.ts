import { NextFunction } from "express";
import { AppRequest, AppResponse } from "../types/express.js";
import { AnyObject, ObjectSchema, ValidationError } from "yup";
import errorLogger from "../utils/errorLogger.js";
import deleteFiles from "../utils/deleteFiles.js";

const validateBody =
    (...schemas: ObjectSchema<AnyObject>[]) =>
    async (req: AppRequest, res: AppResponse, next: NextFunction) => {
        try {
            await Promise.all(
                schemas?.map((schema) => schema.validate(req.body)),
            );
            return next();
        } catch (err: unknown) {
            if (req.uploadedFiles) deleteFiles(req.uploadedFiles); // Eliminar archivos temporales

            if (err instanceof ValidationError) {
                const response: ApiResponse = {
                    ok: false,
                    message: err.message,
                };
                res.status(400).json(response);
                return;
            }

            errorLogger(err);

            // No fue un error de validación
            const error = err as { message?: string };
            const response: ApiResponse = {
                ok: false,
                message:
                    error.message || "Ocurrió un error al validar los datos.",
            };
            res.status(500).json(response);
        }
    };

export default validateBody;
