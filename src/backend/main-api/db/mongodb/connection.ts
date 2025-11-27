import mongoose from "mongoose";
import config from "config";

const uri: string = config.get("db.mongodb.connectionUri");

const { connection, mongo } = mongoose;

const connect = () => {
    return new Promise<void>((resolve, reject) => {
        mongoose.connect(uri);
        connection.on("error", (err) => reject(err));

        connection.once("open", () => {
            resolve();
        });
    });
};

export default connect;
export { connection, mongo };
