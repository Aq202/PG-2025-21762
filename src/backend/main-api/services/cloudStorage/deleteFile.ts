import config from "config";
import { DeleteObjectCommand } from "@aws-sdk/client-s3";
import { s3 } from "./s3.js";

const bucketName = config.get("aws.bucketName") as string;

const deleteFile = async (key: string) => {
  const command = new DeleteObjectCommand({
    Bucket: bucketName,
    Key: key,
  });
  await s3.send(command);
};

export default deleteFile;
