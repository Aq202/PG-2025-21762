import { AppRequest, AppResponse } from "../../types/express.js";
import { ValidationError, NotFoundError } from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";
import {
    createAgencyService,
    assignAgencyAdminService,
    getAgenciesWhereUserIsAdminOrDriverService,
    verifyIfUserIsAgencyAdminService,
} from "./agency.service.js";

const createAgencyController = async (req: AppRequest, res: AppResponse) => {
    const { name }: { name: string } = req.body;

    try {
        const agency = await createAgencyService({
            name: name.toString(),
            session: null,
        });

        const response: createAgencyResponse = {
            ok: true,
            message: "Compañía de transporte creada correctamente.",
            agency,
        };

        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);
        let status = 500;
        const response: ApiResponse = {
            ok: false,
            message: "Ocurrió un error al crear la compañía de transporte.",
        };

        if (ex instanceof ValidationError) {
            response.message = ex.message;
            status = 400;
        }

        res.status(status).json(response);
    }
};

const assignAgencyAdminController = async (
    req: AppRequest,
    res: AppResponse,
) => {
    const {
        agencyId,
        userId,
    }: {
        agencyId: string;
        userId: string;
    } = req.body;

    try {
        await assignAgencyAdminService({
            agencyId,
            userId,
        });

        const response: assignAgencyAdminResponse = {
            ok: true,
            message:
                "Administrador asignado correctamente a la compañía de transporte.",
        };

        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);

        let status = 500;
        const response: assignAgencyAdminResponse = {
            ok: false,
            message:
                "Ocurrió un error al asignar el administrador de la comañia de transporte.",
        };

        if (ex instanceof ValidationError) {
            response.message = ex.message;
            status = 400;
        } else if (ex instanceof NotFoundError) {
            response.message = ex.message;
            status = 404;
        }

        res.status(status).json(response);
    }
};

const getAgenciesController = async (
    req: AppRequest,
    res: AppResponse,
) => {
    const userId = req.session?.id as string;

    try {
        const agencies = await getAgenciesWhereUserIsAdminOrDriverService({
            userId,
            session: null,
        });

        const response: GetAgenciesResponse = {
            ok: true,
            message: "Agencias obtenidas correctamente.",
            agencies,
        };

        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);
        const status = 500;
        const response:ApiResponse = {
            ok: false, 
            message: "Error al obtener las agencias",
        };
        res.status(status).json(response);
    }
}

const verifyIfUserIsAgencyAdminController = async (req: AppRequest, res: AppResponse) => {
    const { agencyId } = req.params;
    const userId = req.session?.id as string;

    try {
        const isAdmin = await verifyIfUserIsAgencyAdminService({

            userId,
            agencyId,
            session: null,
        });
        const response: VerifyIfUserIsAgencyAdminResponse = {
            ok: true,
            message: "Verificación realizada correctamente.",
            isAdmin,
        };
        res.status(200).json(response);
    } catch (ex) {
        errorLogger(ex);
        const status = 500;
        const response: ApiResponse = {
            ok: false,
            message: "Error al verificar si el usuario es administrador de la agencia.",
        };
        res.status(status).json(response);
    }
};

export { 
    createAgencyController, 
    assignAgencyAdminController,
    getAgenciesController,
    verifyIfUserIsAgencyAdminController,
};
