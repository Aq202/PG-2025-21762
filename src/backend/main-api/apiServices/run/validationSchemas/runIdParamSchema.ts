import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const runIdParamSchema = yup.object({
    runId: yup
        .string()
        .nullable()
        .required("El parámetro en la url 'runId' es obligatorio.")
        .test(
            "valid-route-id",
            "El parámetro en la url 'runId' no es un ID válido.",
            (value) => validateId(value),
        )
        .typeError("El parámetro en la url 'runId' debe ser una cadena."),
});

export default runIdParamSchema;
