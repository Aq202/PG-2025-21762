import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const startRouteRunSchema = yup.object({
    time: yup
        .date()
        .nullable()
        .required("El campo 'time' es obligatorio.")
        .typeError("El campo 'time' debe ser una fecha válida."),
    routeId: yup
        .string()
        .nullable()
        .required("El campo 'routeId' es obligatorio.")
        .test(
            "valid-route-id",
            "El campo 'routeId' no es un ID válido.",
            (value) => validateId(value),
        )
        .typeError("El campo 'routeId' debe ser una cadena."),
});

export default startRouteRunSchema;
