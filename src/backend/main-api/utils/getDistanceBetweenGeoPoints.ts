/**
 * Calcula la distancia en metros entre dos puntos geográficos utilizando la fórmula del haversine.
 * @param lat1 Latitud del primer punto.
 * @param long1 Longitud del primer punto.
 * @param lat2 Latitud del segundo punto.
 * @param long2 Longitud del segundo punto.
 * @return Distancia en metros entre los dos puntos.
 */
const getDistanceBetweenGeoPoints = ({
    lat1,
    long1,
    lat2,
    long2,
}: {
    lat1: number;
    long1: number;
    lat2: number;
    long2: number;
}): number => {
    const R = 6371000; // Radio de la Tierra en metros

    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((long2 - long1) * Math.PI) / 180;

    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos((lat1 * Math.PI) / 180) *
            Math.cos((lat2 * Math.PI) / 180) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;

    return distance;
};

export default getDistanceBetweenGeoPoints;