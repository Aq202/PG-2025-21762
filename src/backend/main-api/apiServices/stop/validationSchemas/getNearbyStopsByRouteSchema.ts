import * as yup from "yup";

const getNearbyStopsByRouteSchema = yup.object({
    long: yup
        .number()
        .nullable()
        .required("El parámetro en la url 'long' es obligatorio.")
        .typeError("El parámetro en la url 'long' debe ser un número."),
    lat: yup
        .number()
        .nullable()
        .required("El parámetro en la url 'lat' es obligatorio.")
        .typeError("El parámetro en la url 'lat' debe ser un número."),
    });

export default getNearbyStopsByRouteSchema;
