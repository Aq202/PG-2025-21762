import * as yup from "yup";
import validateId from "../../../utils/validateId.js";


const updateRouteStopsSchema = yup.object({
    stops: yup
        .array()
        .of(
            yup.string()
                .nullable()
                .required("Cada elemento del array 'stops' es obligatorio.")
                .test(
                    "valid-stop-id",
                    "Los elementos del array 'stops' deben ser IDs válidos.",
                    (value) => validateId(value),
                )
        )
        .nullable()
        .typeError("El campo 'stops' debe ser un array de IDs.")
        .required("El campo 'stops' es obligatorio."),
});
export default updateRouteStopsSchema;
