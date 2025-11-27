import fs from 'fs';

const deleteFiles = (files:UploadedFile | UploadedFile[]) => {
  // @ts-expect-error global.dirname is defined in the global scope
  const dirname:string = global.dirname;
  if (Array.isArray(files)) {
    files.forEach((file) => {
      fs.unlink(`${dirname}/files/${file.fileName}`, () => { });
      return null;
    });
  } else {
    fs.unlink(`${dirname}/files/${files.fileName}`, () => { });
  }
};

export default deleteFiles;
