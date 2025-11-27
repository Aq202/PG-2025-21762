import config from "config";
import { PutObjectCommand } from "@aws-sdk/client-s3";
import { s3 } from "./s3.js";

const bucketName = config.get("aws.bucketName") as string;

const uploadFile = async (key: string, data: Buffer | Uint8Array | Blob, contentType: string) => {
  const command = new PutObjectCommand({
    Bucket: bucketName,
    Key: key,
    Body: data,
    ContentType: contentType,
  });
  await s3.send(command);
}

export default uploadFile;