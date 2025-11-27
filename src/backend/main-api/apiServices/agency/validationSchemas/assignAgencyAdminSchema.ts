import yup from "yup";
import validateId from "../../../utils/validateId.js";

export default yup.object().shape({
    agencyId: yup
        .string()
        .required("El campo 'agencyId' es obligatorio.")
        .test("validate-id", "El campo 'agencyId' no es un id válido.", (id) =>
            validateId(id),
        ),

    userId: yup
        .string()
        .required("El campo 'userId' es obligatorio.")
        .test("validate-id", "El campo 'userId' no es un id válido.", (id) =>
            validateId(id),
        ),
});
