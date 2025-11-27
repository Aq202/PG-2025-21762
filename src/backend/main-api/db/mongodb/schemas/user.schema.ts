import { Schema, model } from 'mongoose';

const userSchema = new Schema({
    email: { type: String, required: true, unique: true },
    name: { type: String, required: true },
    lastname: { type: String, required: true },
    role: { type: Number, required: true, default: 0, set: (v: number) => Math.round(v) },
    permissions: { type: [{
        _id: false,
        role: { type: Number, required: true, default: 0, set: (v: number) => Math.round(v) },
        id: { type: Schema.Types.ObjectId, required: true },
    }], default: [] },
    password: { type: String, required: true },
});

const UserSchema = model('user', userSchema);
export default UserSchema;
