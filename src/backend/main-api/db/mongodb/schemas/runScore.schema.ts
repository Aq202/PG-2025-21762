import { Schema, model, Types } from "mongoose";

const runScoreSchema = new Schema({
  routeId: { type: Types.ObjectId, ref: "route", required: true },
  endToEndRunId: { type: String, required: true },
  period: {
    type: {
        type: String,
        enum: ["day", "week", "month", "total"],
        required: true
    },
    key: { type: String, required: true }, // 'YYYY-MM-DD', 'YYYY-WW', 'YYYY-MM', or ''
    priority: { type: Number, required: true }
  },
  score: { type: Number, required: true, default: 0 },
  updatedAt: { type: Date, required: true }
});

const RunScoreSchema = model("runScore", runScoreSchema);
export default RunScoreSchema;
