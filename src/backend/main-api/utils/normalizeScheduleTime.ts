/**
 * Normaliza una cadena de tiempo en formato HH:mm.
 * @param timeStr 
 * @returns string Cadena de tiempo normalizada en formato HH:mm.
 */
const normalizeScheduleTime = (timeStr:string) => {
    const [h, m] = timeStr.split(":");
    return `${h.padStart(2, "0")}:${m.padStart(2, "0")}`;
};

export default normalizeScheduleTime;