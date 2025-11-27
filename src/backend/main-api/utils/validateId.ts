import parseObjectId from "./parseObjectId.js";

const validateId = (id: string): boolean => {
    if (id === null || id === undefined || id === '') {
        return false;
    }

    try{
        parseObjectId(id);
        return true;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    }catch (e) {
        return false;
    }
}

export default validateId;