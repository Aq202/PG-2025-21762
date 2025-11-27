import { ClientSession } from "mongoose";
import { connection } from "../db/mongodb/connection.js";

/**
 * Permite ejecutar una función dentro de una transacción de MongoDB. Si la función se ejecuta correctamente,
 * la transacción se confirma; si ocurre un error, la transacción se aborta.
 * @param callback Función que recibe una sesión de MongoDB y realiza operaciones dentro de una transacción.
 * @returns Promesa que se resuelve cuando la transacción se completa exitosamente o se rechaza si ocurre un error.
 * @throws Error Si ocurre un error durante la transacción, se aborta y se lanza el error.
 */
export async function withMongoTransaction(
    callback: (session: ClientSession) => Promise<void>,
): Promise<void> {
    const current_session = await connection.startSession();

    try {
        current_session.startTransaction();
        await callback(current_session); // Ejecuta la función proporcionada con la sesión actual
        await current_session.commitTransaction();
    } catch (error) {
        await current_session.abortTransaction();
        throw error; // Relanzar el error para que pueda ser manejado por el llamador
    } finally {
        await current_session.endSession();
    }
}
