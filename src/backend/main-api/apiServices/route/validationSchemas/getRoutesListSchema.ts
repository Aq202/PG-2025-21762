import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const getRoutesListSchema = yup.object({
    agencyId: yup
        .string()
        .required("El queryParam 'agencyId' es obligatorio.")
        .test("validate-id", "El queryParam 'agencyId' no es ID válido.", (value) =>
            validateId(value),
        )
        .typeError("El campo queryParam 'agencyId' debe ser una cadena."),
});
export default getRoutesListSchema;
