import { ControllerError } from "../../utils/customError.js";
import { Request } from "express";

// eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
const imageFileFilter = (req: Request, file: unknown, callback: Function) => {
    // Check if the file type is an image
    if (
        file &&
        typeof file === "object" &&
        "mimetype" in file &&
        (file.mimetype === "image/jpeg" ||
            file.mimetype === "image/jpg" ||
            file.mimetype === "image/png")
    ) {
        callback(null, true); // Accept the file
    } else {
        callback(
            new ControllerError(
                "Solo se permiten formatos de imagen JPEG, JPG, Y PNG.",
                400,
            ),
        ); // Reject the file
    }
};

export { imageFileFilter };
