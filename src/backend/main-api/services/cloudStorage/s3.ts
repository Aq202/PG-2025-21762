import { S3Client } from "@aws-sdk/client-s3";
import config from "config";

const accessKeyId = config.get("aws.bucketAccessKey") as string;
const secretAccessKey = config.get("aws.bucketSecretKey") as string;

export const s3 = new S3Client({
	region: "us-west-1",
	credentials: {
		accessKeyId,
		secretAccessKey,
	},
});
