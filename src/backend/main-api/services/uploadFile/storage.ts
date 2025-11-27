import multer from 'multer';
import { AppRequest} from "../../types/express.js";


const storage = multer.diskStorage({
  destination(req, file, callback) {
    // @ts-expect-error global.dirname is defined in the global scope
    callback(null, `${global.dirname}/files`);
  },

  filename(req:AppRequest, file, callback) {
    const newFilename = `${Date.now()}-${file.originalname}`;

    // guardar datos de archivo en req header
    const data:UploadedFile = { fileName: newFilename, type: file.mimetype.split('/')[1] };
    if (!Array.isArray(req.uploadedFiles)) {
      req.uploadedFiles = [data];
    } else req.uploadedFiles.push(data);

    callback(null, newFilename);
  },
});

export default storage;
