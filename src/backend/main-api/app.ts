import express from 'express';
import indexRoutes from './routes/index.js';
import getDirname from './utils/getDirname.js';
import fs from 'node:fs';

const app = express();

const dirname = getDirname(import.meta.url);
// @ts-expect-error Global variable to store the directory name of the current module
global.dirname = dirname;

// Crear directorio 'files' si no existe
const filesDir = `${dirname}/files`;
if (!fs.existsSync(filesDir)) {
  fs.mkdirSync(filesDir, { recursive: true });
}


app.use(express.urlencoded({ extended: true }));
app.use(express.json());

app.use('/', indexRoutes);


export default app;
