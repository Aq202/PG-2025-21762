import { AppRequest, AppResponse } from "../../types/express.js";
import { ControllerError } from "../../utils/customError.js";
import errorLogger from "../../utils/errorLogger.js";
import { createDeviceTokenService, loginService, logoutService, refreshSessionTokenService } from "./session.service.js";

const loginController = async (req:AppRequest, res: AppResponse) => {
    const { email, password } = req.body;

    try {
        const loginData = await loginService(email, password);

        if (!loginData) {
            throw new ControllerError("Email o contraseña incorrectos.", 401);
        }

        const response: LoginResponse = {
            ok: true,
            user: loginData.user,
            refreshToken: loginData.refreshToken,
            accessToken: loginData.accessToken,
        }

        res.status(200).json(response);
        
    } catch (error) {
        errorLogger(error);

        const response: ApiResponse = {ok: false, message: "Ocurrió un error al iniciar sesión."};
        let status = 500;
        
        if (error instanceof ControllerError){
            response.message = error.message;
            status = error.status;
        }
        res.status(status).json(response);        
    }
}

const refreshSessionTokenController = async (
    req: AppRequest,
    res: AppResponse,
) => {
    const authToken = req.headers.authorization as string;
    const sessionData = req.session as User;

    try {

        const accessToken = await refreshSessionTokenService(sessionData, authToken)

        const response: refreshSessionTokenResponse = {
            ok: true,
            accessToken,
        };

        res.status(200).json(response);
    } catch (error) {
        errorLogger(error);

        const response: ApiResponse = {
            ok: false,
            message: "Ocurrió un error al refrescar la sesión.",
        };
        const status = 500;

        res.status(status).json(response);
    }
};

const logoutController = async (req: AppRequest, res: AppResponse) => {
    try{
        const sessionUser = req.session?.id as string;
        await logoutService(sessionUser);

        const response: ApiResponse = {ok: true, message: "Sesión cerrada correctamente."};
        res.status(200).json(response);
    }catch(err){
        errorLogger(err);
        const response: ApiResponse = {ok: false, message: "Ocurrió un error al cerrar sesión."};
        res.status(500).json(response);
    }
}

const generateDeviceTokenController = async (req: AppRequest, res: AppResponse) => {
    try {
        const deviceToken = await createDeviceTokenService();
        const response: CreateDeviceTokenResponse = {ok: true, deviceToken};
        res.status(200).json(response);
    } catch (error) {
        errorLogger(error);
        const response: ApiResponse = {
            ok: false,
            message: "Ocurrió un error al generar el token del dispositivo.",
        };
        res.status(500).json(response);
    }
}


export {
    loginController,
    refreshSessionTokenController,
    logoutController,
    generateDeviceTokenController,
};