import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const optionalAgencyIdSchema = yup.object({
    agencyId: yup
        .string()
        .nullable()
        .notRequired()
        .test(
            "valid-agency-id",
            "El parámetro en la url 'agencyId' no es un ID válido.",
            (value) => {
                // Si no viene nada, no validar
                if (value === null || value === undefined || value === "") {
                    return true;
                }
                return validateId(value);
            },
        )
        .typeError("El parámetro en la url 'agencyId' debe ser una cadena."),
});

export default optionalAgencyIdSchema;
