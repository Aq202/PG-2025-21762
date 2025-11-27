package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.EndToEndRunId
import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.data.entity.RoutePublicData
import com.example.public_transport_app.data.entity.Run
import com.example.public_transport_app.data.entity.Stop
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

interface RouteRepository {

    suspend fun createRoute(
        agencyId: String,
        routeName: String,
        startLocation: LatLng,
        endLocation: LatLng,
        imageFiles: List<Pair<File, String>>,
        startSchedule: Map<DayOfWeek, LocalTime?>,
        endSchedule: Map<DayOfWeek, LocalTime?>,
        unitsId: Set<String>
    ):Resource<Route>

    suspend fun getRoutesList(agencyId: String): Resource<List<Route>>

    suspend fun getRoute(routeId: String): Resource<Route>

    /**
     * Crear un nuevo viaje e insertar el primer punto de la ruta.
     * @param routeId Id de la ruta
     * @param time Fecha y hora a la que se inició el viaje y se guardó la ubicación inicial.
     */
    suspend fun startRun(
        routeId: String,
        time: Instant
    ): Resource<Run>

    /**
     * Añadir una nueva ubicación en un viaje determinado.
     * @param runId Id del viaje
     * @param latLng Ubicación de la unidad actual (al inicio del viaje)
     * @param speed Velocidad de la unidad en esa ubicación.
     * @param time Fecha y hora a la que se inició el viaje y se guardó la ubicación inicial.
     */
    suspend fun addRunPoint(
        runId: String,
        latLng: LatLng,
        speed: Float,
        time: Instant,
        accuracy: Float
    ): Resource<Boolean>

    /**
     * Finalizar un viaje. En este proceso se guardan los subgrafos end-to-end a los cuales se les
     * aplicó map-matching y simplificación.
     */
    suspend fun finishRun(
        runId: String
    ): Resource<Boolean>

    /**
     * Obtener todos los recorridos activos que están en la ubicación dada.
     */
    suspend fun getNearbyActiveRuns(location: LatLng) : Resource<List<NearbyRun>>

    /**
     * Obtener puntos de un recorrido ajustado al mapa.
     * @param endToEndRunId Id del recorrido end-to-end.
     */
    suspend fun getMatchedRunPoints(endToEndRunId: EndToEndRunId) : Resource<List<LatLng>>

    /**
     * Obtener datos públicos de la ruta.
     * @param routeId Id de la ruta
     */
    suspend fun getRoutePublicData(routeId: String): Resource<RoutePublicData>

    /**
     * Obtener las paradas de una ruta.
     * @param routeId Id de la ruta
     */
    suspend fun getRouteStops(routeId: String): Resource<List<Stop>>

    /**
     * Actualizar las paradas de una ruta
     * @param routeId Id de la ruta
     * @param stops Lista de paradas
     */
    suspend fun updateRouteStops(routeId: String, stops: List<Stop>): Resource<Boolean>

    /**
     * Obtener el recorrido más relevante de una ruta.
     * @param routeId Id de la ruta
     */
    suspend fun getBestRouteRun(routeId: String): Resource<List<LatLng>>
}
