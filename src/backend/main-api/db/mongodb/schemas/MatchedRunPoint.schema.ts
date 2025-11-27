import { Schema, model } from "mongoose";

const matchedRunPointSchema = new Schema({
    seq: { type: Number, required: true },
    routeId: { type: Schema.Types.ObjectId, ref: "routeId", required: true },
    runId: { type: Schema.Types.ObjectId, ref: "run", required: true },
    endToEndRunId: { type: String, required: true },
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
    onNetwork: { type: Boolean, required: true },
    geoIndex: { type: String, required: true },
});

const MatchedRunPointSchema = model("matchedRunPoint", matchedRunPointSchema);
export default MatchedRunPointSchema;
