import * as yup from "yup";
import validateId from "../../../utils/validateId.js";

const scheduleSchema = yup
    .object({
        serviceAvailable: yup
            .boolean()
            .required("El campo 'schedules[n].serviceAvailable' es obligatorio.")
            .typeError("El campo 'schedules[n].serviceAvailable' debe ser un booleano."),

        open: yup
            .string()
            .matches(/^\d{1,2}:\d{1,2}$/, "El campo 'schedules[n].open' debe tener el formato HH:MM.")
            .typeError("El campo 'schedules[n].open' debe ser una cadena.")
            .when('serviceAvailable', {
                is: true,
                then: (schema) => schema.required("El campo 'schedules[n].open' es obligatorio cuando 'serviceAvailable' es true."),
                otherwise: (schema) => schema.notRequired().strip(),
            }),

        close: yup
            .string()
            .matches(/^\d{1,2}:\d{1,2}$/, "El campo 'schedules[n].close' debe tener el formato HH:MM.")
            .typeError("El campo 'schedules[n].close' debe ser una cadena.")
            .when('serviceAvailable', {
                is: true,
                then: (schema) => schema.required("El campo 'schedules[n].close' es obligatorio cuando 'serviceAvailable' es true."),
                otherwise: (schema) => schema.notRequired().strip(),
            }),

        day: yup
            .number()
            .oneOf([1, 2, 3, 4, 5, 6, 7], "El campo 'day' debe ser un número entre 1 y 7.")
            .required("El campo 'schedules[n].day' es obligatorio.")
            .typeError("El campo 'schedules[n].day' debe ser un número."),
    })
    .required("Cada elemento de 'schedules' es obligatorio.")
    .typeError("Cada elemento de 'schedules' debe ser un objeto de la forma { day, open, close, serviceAvailable }.");

const schema = yup.object({
    schedules: yup
        .array()
        .of(
            scheduleSchema.nonNullable(
                "Cada elemento de 'schedules' debe ser un objeto no null.",
            ),
        )
        .length(7, "Debe proporcionar exactamente 7 horarios en 'schedules'.")
        .test(
            "unique-and-all-days",
            "Los horarios deben contener los 7 días de la semana sin repetir (day = 0 al 6).",
            (value) => {
                if (!value || value.length !== 7) return false;

                // Si algún item no tiene 'day' válido, no marcar error aquí, lo hará scheduleSchema
                if (
                    value.some(
                        (s) =>
                            typeof s?.day !== "number" ||
                            s?.day < 0 ||
                            s?.day > 6,
                    )
                )
                    return true;

                const days = value.map((schedule) => schedule.day);
                const uniqueDays = new Set(days);
                if (uniqueDays.size !== 7) return false;
                for (let d = 0; d <= 6; d++) {
                    if (!uniqueDays.has(d)) return false;
                }
                return true;
            },
        )
        .required("El campo 'schedules' es obligatorio.")
        .nullable()
        .typeError("El campo 'schedules' debe ser una lista."),
    units: yup
        .array()
        .of(
            yup
                .string()
                .required("Cada 'unit' debe ser un string no vacío.")
                .typeError("Cada 'unit' debe ser una cadena."),
        )
        .required("El campo 'units' es obligatorio.")
        .typeError("El campo 'units' debe ser una lista."),
    endLocation: yup
        .object({
            lat: yup
                .number()
                .required("El campo 'endLocation.lat' es obligatorio.")
                .typeError("El campo 'endLocation.lat' debe ser un número."),
            long: yup
                .number()
                .required("El campo 'endLocation.long' es obligatorio.")
                .typeError("El campo 'endLocation.long' debe ser un número."),
        })
        .nullable()
        .typeError("El campo 'endLocation' debe ser un objeto.")
        .required("El campo 'endLocation' es obligatorio."),
   
    startLocation: yup
        .object({
            lat: yup
                .number()
                .required("El campo 'startLocation.lat' es obligatorio.")
                .typeError("El campo 'startLocation.lat' debe ser un número."),
            long: yup
                .number()
                .required("El campo 'startLocation.long' es obligatorio.")
                .typeError("El campo 'startLocation.long' debe ser un número."),
        })
        .nullable()
        .typeError("El campo 'startLocation' debe ser un objeto.")
        .required("El campo 'startLocation' es obligatorio."),
     name: yup
        .string()
        .required("El campo 'name' es obligatorio.")
        .min(1, "El campo 'name' debe tener al menos un carácter.")
        .typeError("El campo 'name' debe ser una cadena."),
    agencyId: yup
        .string()
        .required("El campo 'agencyId' es obligatorio.")
        .test("validate-id", "El 'agencyId' no es ID válido.", (value) =>
            validateId(value),
        )
        .typeError("El campo 'agencyId' debe ser una cadena."),
});
export default schema;
