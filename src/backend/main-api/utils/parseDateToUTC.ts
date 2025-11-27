const parseDateToUTC = (date: Date | null): Date | null => {
    if (!date) return null;
    const d = new Date(date.toISOString());
    return d;
}

export default parseDateToUTC;