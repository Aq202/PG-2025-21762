import { Schema, model } from 'mongoose';

const sessionSchema = new Schema({
    userId: { type: Schema.Types.ObjectId, ref: 'user', required: true },
    token: { type: String, required: true },
    tokenType: { type: Number, required: true },
    date: { type: Date, required: true },
    linkedToken: { type: String },
});

const SessionSchema = model('session', sessionSchema);
export default SessionSchema;
