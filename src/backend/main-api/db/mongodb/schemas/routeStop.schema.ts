import { Schema, model } from "mongoose";
import routeEmbeddedSchema from "./route.embedded.schema.js";
import agencyEmbeddedSchema from "./agency.embedded.schema.js";

const routeStopSchema = new Schema({
    agency: { type: agencyEmbeddedSchema, ref: "agency", required: true },
    name: { type: String, required: true },
    location: { // [Long, lat]
        type: [Number],
        required: true,
        validate: {
            validator: function(v: unknown) {
            return Array.isArray(v) && v.length === 2;
            },
            message: (props: { value: unknown; }) => `${props.value} debe tener exactamente 2 elementos [longitud, latitud]`
        }
    },
    approved: { type: Boolean, default: false },
    routes: { type: [routeEmbeddedSchema], ref: "route", default: [] },
});

const RouteStopSchema = model("routestop", routeStopSchema);
export default RouteStopSchema;
