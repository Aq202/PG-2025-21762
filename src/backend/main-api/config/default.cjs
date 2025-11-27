/* eslint-disable no-undef */
// eslint-disable-next-line @typescript-eslint/no-require-imports
const dotenv = require("dotenv");

dotenv.config();

module.exports = {
	port: 3000,
	avoidCors: true,
    jwtKey: process.env.JWT_KEY,
    jwtKeyRouteService: process.env.JWT_KEY_ROUTE_SERVICE,
    db:{
        mongodb: {
            connectionUri: process.env.DEV_MONGO_DB_CONNECTION_URI,
        },
    },
    aws:{
        bucketAccessKey: process.env.DEV_AWS_BUCKET_ACCESS_KEY,
        bucketSecretKey: process.env.DEV_AWS_BUCKET_SECRET_KEY,
        bucketName: process.env.DEV_AWS_BUCKET_NAME,
    },
    approveStopsAutomatically: true,
};
