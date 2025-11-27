import { Schema, model } from "mongoose";

const runPointSchema = new Schema({
    seq: { type: Number, required: true },
    routeId: { type: Schema.Types.ObjectId, ref: "routeId", required: true },
    runId: { type: Schema.Types.ObjectId, ref: "run", required: true },
    time: { type: Date, required: true },
    speed: { type: Number, required: true },
    accuracy: { type: Number, required: true },
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
    closeToStartLocation: { type: Boolean, required: true },
    closeToEndLocation: { type: Boolean, required: true }
});

const RunPointSchema = model("runPoint", runPointSchema);
export default RunPointSchema;
