
import consts from "../../../utils/consts.js";
import getDistanceBetweenGeoPoints from "../../../utils/getDistanceBetweenGeoPoints.js";

/**
 * Extrae los subcaminos (subgrafos lineales) de un grafo camino (Pn)
 * que cumplen la secuencia: CS ... CE.
 * - CS: nodo cercano al inicio (closeToStartLocation = true)
 * - CE: nodo cercano al final (closeToEndLocation = true)
 * Cada subcamino es contiguo dentro del grafo.
 * - La distancia acumulada CS → CE (sumando tramos intermedios con haversine)
 *   debe ser > minDistanceBetweenExtremes para que el CE sea válido.
 * - Si el CE no es válido, se trata como nodo normal intermedio (no cierra).
 *
 * @param runPoints Lista de nodos (Pn)
 * @returns Lista de subcaminos lineales CS...CE
 */
const cleanRunPoints = ({
	runPoints: runPointsParam,
	requiredEdges = true,
}:{
	runPoints: RunPoint[],
	requiredEdges?: boolean
}): RunPoint[][] => {

	const subpaths: RunPoint[][] = [];
	let currentPath: RunPoint[] | null = null;

	// Filtrar puntos con accuracy mayor a la permitida
	const runPoints = runPointsParam.filter((p) => p.accuracy <= consts.maxPointAccuracy);

	runPoints.forEach((node) => {
		if (node.closeToStartLocation) {
			// Si ya había un subcamino en construcción, reiniciar
			currentPath = [node];
		} else if (currentPath) {
			// Se está dentro de un camino, añadir nodos siguientes
			currentPath.push(node);

			if (node.closeToEndLocation) {
				// Calcular distancia acumulada
				let totalDist = 0;
				for (let i = 1; i < currentPath.length; i++) {
					const prev = currentPath[i - 1];
					const curr = currentPath[i];
					totalDist += getDistanceBetweenGeoPoints({
						lat1: prev.location.lat,
						long1: prev.location.long,
						lat2: curr.location.lat,
						long2: curr.location.long
					});

					if (totalDist > consts.minDistanceBetweenExtremes) {
						// CE válido, cerrar y guardar
						subpaths.push(currentPath);
						currentPath = null;
						break;
					}
				}

				
			}
		}
		// Si no hay subcamino activo, ignorar el nodo
	});

	if (!requiredEdges){

		// No se es tan estricto con los bordes

		if(currentPath && (currentPath as RunPoint[]).length > 0){
			// Si hay un subcamino en construcción, agregarlo como un subcamino válido
			subpaths.push(currentPath as RunPoint[]);
		}else if(subpaths.length == 0){
			// Si no hay subcaminos y no se encontró un CS
			// Verificar si la distancia acumulada es suficiente para considerar que el recorrido
			// fue iniciado tarde. Eje: El conductor lo inició lejos de la posición de inicio

			let totalDist = 0;
			const hasEnd = runPoints.some(p => p.closeToEndLocation);
			for (let i = 1; i < runPoints.length; i++) {
				const prev = runPoints[i - 1];
				const curr = runPoints[i];
				totalDist += getDistanceBetweenGeoPoints({
					lat1: prev.location.lat,
					long1: prev.location.long,
					lat2: curr.location.lat,
					long2: curr.location.long
				});

				if (totalDist > consts.minDistanceForLateStart) {
					// Si ya recorrió suficiente distancia, se considera que el recorrido fue iniciado tarde
					if(hasEnd){
						// Si tiene un extremo final, se corta hasta ese punto
						const partialPath = []
						for(const p of runPoints){
							partialPath.push(p);
							if(p.closeToEndLocation){
								subpaths.push(partialPath);
								break;
							}
						}
					}
					// Si no tiene extremo final, se toma todo el recorrido
					subpaths.push(runPoints);
					break;
				}
			}
		}
	}

	return subpaths;
};

export default cleanRunPoints;
