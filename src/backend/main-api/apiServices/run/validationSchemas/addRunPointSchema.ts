import * as yup from "yup";

const addRunPointSchema = yup.object({
    accuracy: yup
        .number()
        .required("El campo 'accuracy' es obligatorio.")
        .nullable()
        .typeError("El campo 'accuracy' debe ser un número."),
    time: yup
        .date()
        .nullable()
        .required("El campo 'time' es obligatorio.")
        .typeError("El campo 'time' debe ser una fecha válida."),
    speed: yup
        .number()
        .nullable()
        .required("El campo 'speed' es obligatorio.")
        .min(0, "El campo 'speed' no puede ser negativo.")
        .typeError("El campo 'speed' debe ser un número."),
    long: yup
        .number()
        .required("El campo 'long' es obligatorio.")
        .nullable()
        .typeError("El campo 'long' debe ser un número."),
    lat: yup
        .number()
        .required("El campo 'lat' es obligatorio.")
        .nullable()
        .typeError("El campo 'lat' debe ser un número."),
});

export default addRunPointSchema;
