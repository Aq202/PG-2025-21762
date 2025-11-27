import { Schema, model } from "mongoose";

const agencySchema = new Schema({
    name: { type: String, required: true, unique: true },
    adminsId: { type: [Schema.Types.ObjectId], ref: "user", default: [] },
    routesId: { type: [Schema.Types.ObjectId], ref: "route", default: [] },
    driversId: { type: [Schema.Types.ObjectId], ref: "user", default: [] },
});

const AgencySchema = model("agency", agencySchema);
export default AgencySchema;
