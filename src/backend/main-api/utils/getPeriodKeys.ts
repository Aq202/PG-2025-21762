/**
 * Genera claves de período para día, semana y mes basadas en la fecha proporcionada.
 * @param {Date} d - Fecha para la cual generar las claves. Por defecto es la fecha actual. 
 * @returns {Object} Objeto con las claves generadas. { dayKey, weekKey, monthKey }
 */
const getPeriodKeys = (d = new Date()) => {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');

    // YYYY-MM-DD y YYYY-MM
    const dayKey = `${year}-${month}-${day}`;
    const monthKey = `${year}-${month}`;

    // Semana ISO (YYYY-WW)
    const temp = new Date(Date.UTC(year, d.getMonth(), d.getDate()));
    temp.setUTCDate(temp.getUTCDate() + 4 - (temp.getUTCDay() || 7));
    const week = Math.ceil((((temp.getTime() - Date.UTC(temp.getUTCFullYear(), 0, 1)) / 86400000) + 1) / 7);
    const weekKey = `${temp.getUTCFullYear()}-W${String(week).padStart(2, '0')}`;

    return { dayKey, weekKey, monthKey };
};

export default getPeriodKeys;
