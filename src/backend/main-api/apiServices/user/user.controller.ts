import { AppRequest, AppResponse } from "../../types/express.js";
import { ValidationError } from "../../utils/customError.js";
import { createUserService } from "./user.service.js";

const createUserController = async (req: AppRequest, res: AppResponse) => {
  const {
    name,
    lastname,
    email,
    password,
  }: {
    name: string;
    lastname: string;
    email: string;
    role: string;
    password: string;
  } = req.body;

  try {
    const user = await createUserService({
      name: name.toString(),
      lastname,
      email,
      password,
    });

    const response: CreateUserResponse = {
      ok: true,
      message: "Usuario creado correctamente.",
      ...user,
    };

    res.status(200).json(response);
  } catch (ex) {
    let response: ApiResponse = {
      ok: false,
      message: "Ocurrió un error al crear el usuario.",
    };

    if (ex instanceof ValidationError && ex.message) {
      response = {
        ok: false,
        message: ex.message,
      };
    }

    res.status(400).json(response);
  }
};

export { createUserController };
