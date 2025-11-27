/**
 * Convierte una fecha a UTC.
 * @param {Date} date
 * @returns Date in UTC
 */
const getUTCDate = (date:Date|undefined|null) => {
  const localDate = date ? new Date(date) : new Date();
  const d = new Date(localDate.toISOString());
  return d;
};
export default getUTCDate;
