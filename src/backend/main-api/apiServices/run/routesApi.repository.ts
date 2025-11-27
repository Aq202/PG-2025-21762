import consts from "../../utils/consts.js";
import { ValidationError } from "../../utils/customError.js";

/**
 *  Obtiene la predicción de ruta desde el servicio externo.
 * * @param coords Coordenadas del camino (lat, long)
 * * @param destination Coordenadas del destino (lat, long)
 * * @param token Token de autorización para el servicio de rutas
 * * @returns Mejor ruta predicha (end_to_end_route_id)
 *  * @throws ValidationError si la respuesta del servicio es 400 o 422
 *  * @throws Error para otros errores del servicio
 */
const getRoutePrediction = async ({
	routeId,
	coords,
	start,
	destination,
	prevPrediction,
	token
}: {
	routeId: string;
	coords: LatLong[];
	start: LatLong;
	destination: LatLong;
	prevPrediction: string | null;
	token: string;
}): Promise<[string | null, number | null, {type:string, data:string}[]]> => {

  	// Formatear coords a lat,lon;lat,lon;...
    const formattedCoords = coords
        .map(point => `${point.lat},${point.long}`)
        .join(";");

	let params = new URLSearchParams({
		route_id: routeId,
		start: `${start.lat},${start.long}`,
		dest: `${destination.lat},${destination.long}`
	});

	if (prevPrediction) {
		params.append("prev_prediction", prevPrediction);
	}

	const url = `${consts.routeServiceHost}/predict/${formattedCoords}?${params.toString()}`;

	// Controlador de aborto del fetch
	const controller = new AbortController();
	const timeoutId = setTimeout(() => controller.abort(), consts.maxWaitForRoutePredictionMs);

	const response = await fetch(url, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": token,
		},
		signal: controller.signal, // Vincula el AbortController
	});

	clearTimeout(timeoutId);8

	const result = await response.json();

	if (!response.ok) {
		if ([400, 422].includes(response.status)) {
			throw new ValidationError(result.detail);
		}
		throw new Error("Error interno del servidor de rutas");
	}

	return [result.best_fit, result.metric, result.logs];
}

/**
 * Simplifica una serie de puntos de ruta usando el servicio externo.
 * 
 * @param coords Coordenadas del camino (lat, long)
 * @param token Token de autorización para el servicio de rutas
 * @returns LatLon[]. Ruta simplificada. 
 */
const simplifyRoutePoints = async ({
	coords,
	token,
}: {
	coords: LatLong[];
	token: string;
}) : Promise<LatLong[]> => {
	
	// Formatear coords a lat,lon;lat,lon;...
    const formattedCoords = coords
        .map(point => `${point.lat},${point.long}`)
        .join(";");

	const url = `${consts.routeServiceHost}/simplify/${formattedCoords}`;
	
	const response = await fetch(url, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": token,
		},
	});

	const result = await response.json();

	if (!response.ok) {
		if ([400, 422].includes(response.status)) {
			throw new ValidationError(result.detail);
		}
		throw new Error("Error interno del servidor de rutas");
	}

	const points =result.simplified_route; // Coordenadas en formato [[lat, long], ...]
	return points.map((pt: number[]) => ({ lat: pt[0], long: pt[1] }));
}

export {
	getRoutePrediction,
	simplifyRoutePoints,
}