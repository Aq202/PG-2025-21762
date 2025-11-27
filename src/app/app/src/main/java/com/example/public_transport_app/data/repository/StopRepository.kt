package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.ClosestStopByRoute
import com.example.public_transport_app.data.entity.Stop
import com.google.android.gms.maps.model.LatLng

interface StopRepository {
    /**
     * Obtener lista de paradas disponibles
     */
    suspend fun getStops(agencyId: String): Resource<List<Stop>>

    /**
     * Crear una nueva ruta
     * @param agencyId Id de la agencia
     * @param stopName Nombre de la ruta
     * @param location Punto de inicio de la ruta
     */
    suspend fun createStop(agencyId: String, stopName: String, location: LatLng): Resource<Stop>

    /**
     * Obtener las paradas más cercanas de cada ruta en un radio definido.
     *
     */
    suspend fun getclosestStopByRoute(location: LatLng) : Resource<List<ClosestStopByRoute>>
}
