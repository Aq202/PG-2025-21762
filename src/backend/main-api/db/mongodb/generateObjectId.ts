import mongoose from "mongoose";

const generateObjectId = () => {
  return new mongoose.Types.ObjectId();
};

export default generateObjectId;
