/* eslint-disable @typescript-eslint/no-explicit-any */
import { AppNext, AppRequest, AppResponse } from "../types/express.js";
import consts from "../utils/consts.js";
import errorLogger from "../utils/errorLogger.js";

export default (
        multerInstance: any,
        fileSizeLimit = consts.uploadImageSizeLimit,
    ) =>
    (req: AppRequest, res: AppResponse, next: AppNext) => {
        multerInstance(req, res, (err: any) => {
            if (!err) next();
            else {
                errorLogger(err);
                let error = err?.message ?? "Ocurrió un error al subir imagen.";
                let status = err?.status ?? 500;

                if (err?.code === "LIMIT_FILE_SIZE") {
                    error = `El tamaño del archivo es demasiado grande. El tamaño máximo es de ${
                        Math.trunc(fileSizeLimit / 1000000)
                    } MB.`;
                    status = 413;
                }

                const responseObj: ApiResponse = {ok: false, message: error};
                console.log(responseObj);
                res.status(status).send(responseObj);
            }
        });
    };
