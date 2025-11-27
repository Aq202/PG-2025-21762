import mongoose from 'mongoose';
import { InvalidObjectIdError } from './customError.js';

/**
 * Parsea un string a un ObjectId de Mongoose.
 * @param id 
 * @param fieldName String. Nombre del campo para el cual se está parseando el ObjectId, usado en los mensajes de error.
 * @throws {InvalidObjectIdError} Si el ID proporcionado no es un ObjectId válido.
 * @returns {mongoose.Types.ObjectId} El ObjectId parseado.
 */
const parseObjectId = (id: string, fieldName = 'id'): mongoose.Types.ObjectId => {
  if (!mongoose.Types.ObjectId.isValid(id)) {
    throw new InvalidObjectIdError(`Invalid ObjectId provided for ${fieldName}: ${id}`, fieldName);
  }
  return new mongoose.Types.ObjectId(id);
};

export default parseObjectId;
