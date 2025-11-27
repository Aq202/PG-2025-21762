import consts from "../../../utils/consts.js";
import getDistanceBetweenGeoPoints from "../../../utils/getDistanceBetweenGeoPoints.js";
import { OsrmMatchResponse } from "./osrm.types.js";

/**
 * Calcula el bearing (en grados) entre dos coordenadas geográficas.
 * @returns Ángulo en grados (0-360), donde 0 es el norte.
 */
function getBearing(lat1: number, lon1: number, lat2: number, lon2: number): number {
	const φ1 = (lat1 * Math.PI) / 180;
	const φ2 = (lat2 * Math.PI) / 180;
	const Δλ = ((lon2 - lon1) * Math.PI) / 180;

	const y = Math.sin(Δλ) * Math.cos(φ2);
	const x = Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ);
	const θ = Math.atan2(y, x);
	return (θ * 180) / Math.PI >= 0 ? (θ * 180) / Math.PI : (θ * 180) / Math.PI + 360;
}

/**
 * Hace una petición a OSRM match service para mapear puntos GPS a la red vial usando fetch nativo
 */
async function osrmMatchPoints(runPoints: RunPoint[]): Promise<OsrmMatchResponse> {
	const coordsStr = runPoints
		.map((p) => `${p.location.long},${p.location.lat}`)
		.join(";");
	const timestampsStr = runPoints
		.map((p) => Math.floor(p.time.getTime() / 1000))
		.join(";");
	const radiusesStr = runPoints
		.map((p) => Math.max(p.accuracy, 10))
		.join(";");

	// Calcular bearings con rango de ±45°
	const bearings: string[] = [];
	for (let i = 0; i < runPoints.length; i++) {
		if (i < runPoints.length - 1) {
			const b = getBearing(
				runPoints[i].location.lat,
				runPoints[i].location.long,
				runPoints[i + 1].location.lat,
				runPoints[i + 1].location.long
			);
			bearings.push(`${Math.round(b)},45`);
		} else {
			// Repetir el bearing del punto anterior para el último
			bearings.push(bearings[bearings.length - 1]);
		}
	}
	const bearingsStr = bearings.join(";");

	const url =
		`${consts.osrmHost}/match/v1/driving/${coordsStr}` +
		`?timestamps=${timestampsStr}` +
		`&radiuses=${radiusesStr}` +
		`&bearings=${bearingsStr}` +
		`&geometries=geojson` +
		`&steps=false` +
		`&overview=full` +
		`&tidy=true` +
		`&gaps=split`;

	const resp = await fetch(url);
	if (!resp.ok) {
		throw new Error(`Error OSRM match: ${resp.status} - ${resp.statusText}`);
	}

	const data: OsrmMatchResponse = await resp.json();
	return data;
}

/**
 * Busca si existe un vecino dentro de la red vial con una profundidad de búsqueda máxima.
 * @param currIndex Índice del punto actual en la lista de puntos emparejados.
 * @param maxDeepSearch Profundidad máxima de búsqueda para encontrar un vecino en la red.
 * @param gpsPoints Lista de puntos GPS originales.
 * @param mappedFlags Lista de banderas que indican si un punto está mapeado o no en la red vial.
 * @return Coordenadas del vecino más cercano en la red o null si no se encuentra.
 */
function getClosestOnNetworkNeighbor(
	currIndex: number,
	maxDeepSearch: number,
	gpsPoints: LatLong[],
	mappedFlags: boolean[]
): LatLong | null {
	for (let deep = 1; deep <= maxDeepSearch; deep++) {
		const leftNeighborIndex = currIndex - deep;
		const rightNeighborIndex = currIndex + deep;

		const leftNeighbor = leftNeighborIndex >= 0 ? gpsPoints[leftNeighborIndex] : null;
		const rightNeighbor = rightNeighborIndex < gpsPoints.length ? gpsPoints[rightNeighborIndex] : null;

		const isLeftOnNetwork = leftNeighbor !== null && mappedFlags[leftNeighborIndex];
		const isRightOnNetwork = rightNeighbor !== null && mappedFlags[rightNeighborIndex];

		if (isLeftOnNetwork && isRightOnNetwork) {
			// Devolver neighbor on_network con menor distancia haversine
			const currLocation = gpsPoints[currIndex];

			const distLeft = getDistanceBetweenGeoPoints({
				lat1: currLocation.lat,
				long1: currLocation.long,
				lat2: leftNeighbor!.lat,
				long2: leftNeighbor!.long,
			});
			const distRight = getDistanceBetweenGeoPoints({
				lat1: currLocation.lat,
				long1: currLocation.long,
				lat2: rightNeighbor!.lat,
				long2: rightNeighbor!.long,
			});

			return distLeft < distRight ? leftNeighbor! : rightNeighbor!;
		} else if (isLeftOnNetwork) {
			return leftNeighbor!;
		} else if (isRightOnNetwork) {
			return rightNeighbor!;
		}
	}
	return null;
}

/**
 * Función para realizar el mapeo de puntos GPS a la red vial, considerando puntos fuera de la red.
 * runPoints: lista de RunPoint[]
 * @return: tupla [LatLong[], mappedFlags], en donde:
 *      mappedFlags[i] = true si el punto está dentro de la red, false si fue off-network
 */
export async function mapMatchRunPoints(
	runPoints: RunPoint[]
): Promise<[LatLong[], boolean[]]> {
	if (runPoints.length === 0) return [[], []];

	// Hacer un map matching con splits
	const matchResult = await osrmMatchPoints(runPoints);

	// Construir full_route con indicador de si el punto fue mapeado
	const fullRoute: LatLong[] = [];
	const mappedFlags: boolean[] = [];
	let currentMatchingIndex: number | null = null;

	for (let i = 0; i < runPoints.length; i++) {
		const originalPoint = runPoints[i];
		const tp = matchResult.tracepoints[i];

		if (tp) {
			if (tp.matchings_index !== currentMatchingIndex) {
				// Si el punto pertenece a un segmento diferente, actualizar current_matching_index
				currentMatchingIndex = tp.matchings_index;

				// Agregar todos los puntos mapeados del segmento
				const segment: [number, number][] = matchResult.matchings[currentMatchingIndex].geometry.coordinates;
				for (const [long, lat] of segment) {
					fullRoute.push({ long, lat });
					mappedFlags.push(true);
				}
			}
		} else {

			// El punto no fue mapeado por ser off_network o redundante
			const { lat, long } = originalPoint.location;
			let addNotMatchedPoint = false;

			if (currentMatchingIndex === null) {
				// Puntos iniciales no mapeados
				addNotMatchedPoint = true;
			} else if (i === runPoints.length - 1) {
				// Si es el último punto, añadir
				addNotMatchedPoint = true;
			} else {
				addNotMatchedPoint = true; //  Default si no se encuentra otro segmento

				//  Encontrar siguiente punto mapeado
				for (let j = i + 1; j < runPoints.length; j++) {
					if (matchResult.tracepoints[j]) {

						// Si el siguiente punto mapeado pertenece al mismo segmento, el punto fue
						// omitido por redundancia, se puede descartar
						// Si el siguiente no pertenece al mismo segmento, este punto fue de los
						// causantes que se tuviera que partir el recorrido, mantener
						if (matchResult.tracepoints[j]?.matchings_index === currentMatchingIndex) {
							addNotMatchedPoint = false;
						}
						break;
					}
				}
			}

			if (addNotMatchedPoint) {
				fullRoute.push({ long, lat });
				mappedFlags.push(false);
			}
		}
	}

	// Para cada punto fuera de la red, evaluar si hay puntos dentro de la red cercanos
	// Si los hay, eliminar punto
	// Verificar si tiene un vecino (antes o despues) en la ruta a menos de 50m
	let index = 0;
	while (index < fullRoute.length) {
		if (mappedFlags[index]) {
			// Ignorar puntos dentro de la red
			index++;
			continue;
		}

		const point = fullRoute[index];
		const closestOnNetworkNeighbor = getClosestOnNetworkNeighbor(
			index,
			10,
			fullRoute,
			mappedFlags
		);

		if (
			closestOnNetworkNeighbor &&
			getDistanceBetweenGeoPoints({
				lat1: point.lat,
				long1: point.long,
				lat2: closestOnNetworkNeighbor.lat,
				long2: closestOnNetworkNeighbor.long,
			}) < 50
		) {
			//Si hay un vecino cerca dentro de la red, eliminar punto
			fullRoute.splice(index, 1);
			mappedFlags.splice(index, 1);

			// No aumentar index dado que se eliminó un elemento
		} else {
			index++;
		}
	}

	return [fullRoute, mappedFlags];
}
