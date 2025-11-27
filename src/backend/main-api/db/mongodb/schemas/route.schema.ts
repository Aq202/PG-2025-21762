import { Schema, model } from "mongoose";
import agencyEmbeddedSchema from "./agency.embedded.schema.js";

const routeSchema = new Schema({
    startLocation: { // [Long, lat]
        type: [Number],
        required: true,
        validate: {
            validator: function(v: unknown) {
            return Array.isArray(v) && v.length === 2;
            },
            message: (props: { value: unknown; }) => `${props.value} debe tener exactamente 2 elementos [longitud, latitud]`
        }
    },
    endLocation: { // [Long, lat]
        type: [Number],
        required: true,
        validate: {
            validator: function(v: unknown) {
                return Array.isArray(v) && v.length === 2;
            },
            message: (props: { value: unknown; }) => `${props.value} debe tener exactamente 2 elementos [longitud, latitud]`
        }
    },
    startLocationGeoIndex: { type: String, required: true },
    endLocationGeoIndex: { type: String, required: true },
    name: { type: String, required: true },
    schedules: {type: [{
        day: { type: Number, required: true, enum: [1, 2, 3, 4, 5, 6, 7] },
        serviceAvailable: { type: Boolean, required: true, default: true },
        open: {
                type: String,
                validate: {
                    validator: function (this:Schedule, v:string) {
                        // Si serviceAvailable es false, no validar open
                        if (this.serviceAvailable === false) return true;
                        return typeof v === 'string' && /^([01]\d|2[0-3]):[0-5]\d$/.test(v);
                    },
                    message: "El campo 'open' debe tener el formato HH:MM cuando 'serviceAvailable' es true.",
                },
                required: function (this:Schedule) {
                    return this.serviceAvailable === true;
                }
            },
        close: {
            type: String,
            validate: {
                validator: function (this:Schedule, v:string) {
                    if (this.serviceAvailable === false) return true;
                    return typeof v === 'string' && /^([01]\d|2[0-3]):[0-5]\d$/.test(v);
                },
                message: "El campo 'close' debe tener el formato HH:MM cuando 'serviceAvailable' es true.",
            },
            required: function (this:Schedule) {
                return this.serviceAvailable === true;
            }
        },
        _id: false,
    }], required: true },
    units: { type: [String], default: [] },
    driversId: { type: [Schema.Types.ObjectId], ref: "user", default: [] },
    unitImages: { type: [String], default: [] },
    stops: { type: [Schema.Types.ObjectId], ref: "routestop", default: [] },
    agency: { type: agencyEmbeddedSchema, required: true },
});

const RouteSchema = model("route", routeSchema);
export default RouteSchema;
