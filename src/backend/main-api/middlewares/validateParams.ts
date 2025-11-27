import { AppNext, AppRequest, AppResponse } from "../types/express.js";

const validateParams =
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (...schemas: any[]) =>
    async (req: AppRequest, res: AppResponse, next: AppNext) => {
        try {
            await Promise.all(
                schemas?.map((schema) => schema.validate(req.params)),
            );
            next();
        } catch (err) {
            const error = err as { message: string };
            res.statusMessage = error.message;
            res.status(400).send({
                err: error.message,
                status: 400,
                ok: false,
            });
        }
    };

export default validateParams;
