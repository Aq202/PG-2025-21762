import * as yup from "yup";

const getNearbyRunsSchema = yup.object({
    long: yup
        .number()
        .required("El queryParam 'long' es obligatorio.")
        .nullable()
        .typeError("El queryParam 'long' debe ser un número."),
    lat: yup
        .number()
        .required("El queryParam 'lat' es obligatorio.")
        .nullable()
        .typeError("El queryParam 'lat' debe ser un número."),
    
});

export default getNearbyRunsSchema;
