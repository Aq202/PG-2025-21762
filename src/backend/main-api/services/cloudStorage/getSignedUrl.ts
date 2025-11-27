import config from "config";
import { GetObjectCommand } from "@aws-sdk/client-s3";
import { getSignedUrl } from "@aws-sdk/s3-request-presigner";
import { s3 } from "./s3.js";

const bucketName = config.get("aws.bucketName") as string;

const getSignedDownloadUrl = async (key: string, expiresIn = 3600): Promise<string> => {
  const command = new GetObjectCommand({
    Bucket: bucketName,
    Key: key,
  });

  const url = await getSignedUrl(s3, command, { expiresIn }); // expiración en segundos
  return url;
};

export default getSignedDownloadUrl;
