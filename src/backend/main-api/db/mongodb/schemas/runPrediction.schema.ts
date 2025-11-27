import { Schema, model } from "mongoose";

const runPredictionSchema = new Schema({
	runId: { type: Schema.Types.ObjectId, ref: "run", required: true },
	runPointId: { type: Schema.Types.ObjectId, ref: "runPoint", required: true },
	routePrediction: { type: String },
	metric: { type: Number },
	logs: [{ type: { type: String }, data: { type: String } }],
	createdAt: { type: Date, default: Date.now }
});

const RunPrediction = model("runPrediction", runPredictionSchema);
export default RunPrediction;
