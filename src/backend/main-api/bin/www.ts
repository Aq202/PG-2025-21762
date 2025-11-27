import app from "../app.js";
import connectMongodb from "../db/mongodb/connection.js";
import { createRequire } from 'module';

const require = createRequire(import.meta.url);
const config = require('config');

const port = config.get("port");



connectMongodb()
    .then(() => {
        console.log("Conexión a la base de datos MongoDB exitosa.");

        app.listen(port, () => {
            console.log(`Servidor corriendo en puerto ${port}.`);
        });
        
    })
    .catch((error) => {
        console.error("Error al conectar a la base de datos.", error);
    });
