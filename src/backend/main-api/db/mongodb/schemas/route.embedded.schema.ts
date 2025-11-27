import { Schema } from 'mongoose';

const routeEmbeddedSchema = new Schema({
  _id: { type: Schema.Types.ObjectId, ref: 'route', required: true },
  name: { type: String, required: true },
});

export default routeEmbeddedSchema;
