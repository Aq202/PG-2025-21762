package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.ClosestStopByRoute
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.data.remote.API
import com.example.public_transport_app.data.remote.dto.request.CreateStopRequest
import com.example.public_transport_app.data.remote.dto.request.LocationRequest
import com.example.public_transport_app.data.remote.dto.response.toEntity
import com.example.public_transport_app.utils.ErrorParser
import com.google.android.gms.maps.model.LatLng

class StopRepositoryImp(
    private val api: API,
): StopRepository {

    /**
     * Obtener lista de paradas disponibles
     */
    override suspend fun getStops(agencyId: String): Resource<List<Stop>> {
        return try {
            val response = api.getStops(agencyId)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val routeData = body.routeStops.toEntity()
                Resource.Success(routeData)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en stops.getStops repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Crear una nueva ruta
     * @param agencyId Id de la agencia
     * @param stopName Nombre de la ruta
     * @param location Punto de inicio de la ruta
     */
    override suspend fun createStop(
        agencyId: String,
        stopName: String,
        location: LatLng
    ): Resource<Stop> {
        return try {
            val request = CreateStopRequest(
                agencyId = agencyId,
                name = stopName,
                location = LocationRequest(
                    lat = location.latitude,
                    long = location.longitude
                )
            )
            val response = api.createStop(request)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val routeData = body.routeStop.toEntity()
                Resource.Success(routeData)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en stops.createStop repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Obtener las paradas más cercanas de cada ruta en un radio definido.
     *
     */
    override suspend fun getclosestStopByRoute(location: LatLng): Resource<List<ClosestStopByRoute>> {
        return try {
            val response = api.getNearbyStopsByRoute(
                lat = location.latitude,
                long = location.longitude
            )
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val nearbyStopsData = body.nearbyStops.toEntity()
                Resource.Success(nearbyStopsData)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en stops.getNearbyStopsByRoute repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }
}