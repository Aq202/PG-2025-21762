import { Schema, model } from "mongoose";

const runSchema = new Schema({
    routeId: { type: Schema.Types.ObjectId, ref: "routeId", required: true },
    agencyId: { type: Schema.Types.ObjectId, ref: "agency", required: true },
    userId: { type: Schema.Types.ObjectId, ref: "user", required: true },
    time: { type: Date, required: true },
    status: { type: Number, required: true},
    currentPoint: {
        type: [Number],
        required: false,
        validate: {
            validator: function(v: unknown) {
            return !v || (Array.isArray(v) && v.length === 2);
            },
            message: (props: { value: unknown; }) => `${props.value} debe tener exactamente 2 elementos [longitud, latitud]`
        }
    },
    routePrediction: { type: String, required: false }, // End-to-end run id
    lastUpdated: { type: Date, required: false },
    lastPrediction: { type: Date, required: false },
    distanceFromLastPrediction: { type: Number, required: true, default: 0 },
    predictionInProgress: { type: Boolean, required: true, default: false },
});

const RunSchema = model("run", runSchema);
export default RunSchema;
