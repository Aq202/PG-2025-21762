import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const routeIdParamSchema = yup.object({
    routeId: yup
        .string()
        .nullable()
        .required("El parámetro en la url 'routeId' es obligatorio.")
        .test(
            "valid-route-id",
            "El parámetro en la url 'routeId' no es un ID válido.",
            (value) => validateId(value),
        )
        .typeError("El parámetro en la url 'routeId' debe ser una cadena."),
});

export default routeIdParamSchema;
