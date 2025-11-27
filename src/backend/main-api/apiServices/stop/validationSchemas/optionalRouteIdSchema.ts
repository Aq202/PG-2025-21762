import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const optionalRouteIdSchema = yup.object({
    routeId: yup
        .string()
        .nullable()
        .test(
            "valid-route-id",
            "El parámetro en la url 'routeId' no es un ID válido.",
            (value) => validateId(value ?? ""),
        )
        .typeError("El parámetro en la url 'routeId' debe ser una cadena."),
});

export default optionalRouteIdSchema;
