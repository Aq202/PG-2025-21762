import * as yup from "yup";
import validateId from "../../../utils/validateId.js";


const createRouteStopSchema = yup.object({
    location: yup
        .object({
            lat: yup
                .number()
                .required("El campo 'location.lat' es obligatorio.")
                .typeError("El campo 'location.lat' debe ser un número."),
            long: yup
                .number()
                .required("El campo 'location.long' es obligatorio.")
                .typeError("El campo 'location.long' debe ser un número."),
        })
        .nullable()
        .typeError("El campo 'location' debe ser un objeto de la forma {lat, long}.")
        .required("El campo 'location' es obligatorio."),
    name: yup
        .string()
        .required("El campo 'name' es obligatorio.")
        .min(5, "El campo 'name' debe tener al menos 5 caracteres.")
        .typeError("El campo 'name' debe ser una cadena."),
    agencyId: yup
        .string()
        .nullable()
        .required("El campo 'agencyId' es obligatorio.")
        .test(
            "valid-agency-id",
            "El campo 'agencyId' no es un ID válido.",
            (value) => validateId(value),
        )
        
    
});
export default createRouteStopSchema;
