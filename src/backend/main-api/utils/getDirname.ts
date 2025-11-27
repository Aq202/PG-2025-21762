import { fileURLToPath } from 'node:url';
import path from 'node:path';

const getDirname = (metaUrl:string) => path.dirname(fileURLToPath(metaUrl));
export default getDirname;
