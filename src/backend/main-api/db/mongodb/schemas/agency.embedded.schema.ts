import { Schema } from 'mongoose';

const agencyEmbeddedSchema = new Schema({
  _id: { type: Schema.Types.ObjectId, ref: 'agency', required: true },
  name: { type: String, required: true },
});

export default agencyEmbeddedSchema;
