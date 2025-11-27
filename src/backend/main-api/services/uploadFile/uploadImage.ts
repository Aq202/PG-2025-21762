import multer from "multer";
import storage from "./storage.js";
import consts from "../../utils/consts.js";
import { imageFileFilter } from "./fileFilter.js";

const limits = {
    fileSize: consts.uploadImageSizeLimit,
};

export default multer({ storage, limits, fileFilter: imageFileFilter });
