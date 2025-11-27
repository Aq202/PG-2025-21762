package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.EndToEndRunId
import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.data.entity.RoutePublicData
import com.example.public_transport_app.data.entity.Run
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.data.remote.API
import com.example.public_transport_app.data.remote.dto.request.AddRunPointRequest
import com.example.public_transport_app.data.remote.dto.request.LocationRequest
import com.example.public_transport_app.data.remote.dto.request.ScheduleRequest
import com.example.public_transport_app.data.remote.dto.request.StartRunRequest
import com.example.public_transport_app.data.remote.dto.request.UpdateRouteStopsRequest
import com.example.public_transport_app.data.remote.dto.response.toEntity
import com.example.public_transport_app.data.remote.dto.response.toLatlngList
import com.example.public_transport_app.utils.ErrorParser
import com.example.public_transport_app.utils.toCompactHourMinuteString
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

class RouteRepositoryImp(
    private val api: API,
    private val gson: Gson,
): RouteRepository {
    override suspend fun createRoute(
        agencyId: String,
        routeName: String,
        startLocation: LatLng,
        endLocation: LatLng,
        imageFiles: List<Pair<File, String>>,
        startSchedule: Map<DayOfWeek, LocalTime?>,
        endSchedule: Map<DayOfWeek, LocalTime?>,
        unitsId: Set<String>
    ): Resource<Route> {


        val startLocationObj = LocationRequest(startLocation.latitude, startLocation.longitude)
        val endLocationObj = LocationRequest(endLocation.latitude, endLocation.longitude)

        // Guardar horarios en el formato solicitado por la API
        val schedulesObj:MutableList<ScheduleRequest> = mutableListOf()

        val days = DayOfWeek.entries
        for(day in days){
            schedulesObj.add(
                ScheduleRequest(
                    serviceAvailable = startSchedule[day] != null && endSchedule[day] != null,
                    day = day.value,
                    open = startSchedule[day]?.toCompactHourMinuteString(),
                    close = endSchedule[day]?.toCompactHourMinuteString()
                )
            )
        }

        val agencyIdBody = agencyId.toRequestBody("text/plain".toMediaType())
        val routeNameBody = routeName.toRequestBody("text/plain".toMediaType())
        val startLocationBody = gson.toJson(startLocationObj).toRequestBody("application/json".toMediaType())
        val endLocationBody = gson.toJson(endLocationObj).toRequestBody("application/json".toMediaType())
        val unitsBody = gson.toJson(unitsId).toRequestBody("application/json".toMediaType())
        val schedulesBody = gson.toJson(schedulesObj).toRequestBody("application/json".toMediaType())

        // Crear MultipartBody.Part para las imágenes
        val imageParts = imageFiles.mapIndexed { _, (file, mimeType) ->
            val requestFile = file.asRequestBody(mimeType.toMediaType())
            MultipartBody.Part.createFormData("unitImages", file.name, requestFile)
        }

        try {
            val result = api.createRoute(
                agencyId = agencyIdBody,
                name = routeNameBody,
                startLocation = startLocationBody,
                endLocation = endLocationBody,
                units = unitsBody,
                schedules = schedulesBody,
                unitImages = imageParts
            )
            val body = result.body()
            if(result.isSuccessful && body != null){
                val route = body.route.toEntity()

                return Resource.Success(route)
            }else{
                val errorBody = result.errorBody()
                val error = ErrorParser.parseErrorMessage(errorBody)
                return Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en createRoute repository "+ex.message)
            return Resource.Error(ex.message)
        }



    }

    override suspend fun getRoutesList(agencyId: String): Resource<List<Route>> {
        return try {
            val response = api.getRoutesList(agencyId)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val routes = body.routes.map { routeDto ->
                    routeDto.toEntity()
                }

                Resource.Success(routes)
            } else if (response.isSuccessful && body != null && !body.ok){
              Resource.Error(body.message)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en getRoutesList repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    override suspend fun getRoute(routeId: String): Resource<Route> {
        return try {
            val response = api.getRoute(routeId)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val route = body.route.toEntity()
                Resource.Success(route)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en getRoute repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Crear un nuevo viaje e insertar el primer punto de la ruta.
     * @param routeId Id de la ruta
     * @param time Fecha y hora a la que se inició el viaje y se guardó la ubicación inicial.
     */
    override suspend fun startRun(
        routeId: String,
        time: Instant
    ): Resource<Run> {

        return try {
            val response = api.startRun(
                StartRunRequest(
                    time = time.toString(),
                    routeId = routeId,
                )
            )
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {

                val runResponse = body.run



                Resource.Success(Run(
                    agencyId = runResponse.agencyId,
                    routeId = runResponse.routeId,
                    id = runResponse.id,
                    time = runResponse.time
                ))
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en startRun repository: ${ex.message}")
            Resource.Error(ex.message)
        }

    }

    /**
     * Añadir una nueva ubicación en un viaje determinado.
     * @param runId Id del viaje
     * @param latLng Ubicación de la unidad actual (al inicio del viaje)
     * @param speed Velocidad de la unidad en esa ubicación.
     * @param time Fecha y hora a la que se inició el viaje y se guardó la ubicación inicial.
     */
    override suspend fun addRunPoint(
        runId: String,
        latLng: LatLng,
        speed: Float,
        time: Instant,
        accuracy: Float
    ): Resource<Boolean> {
        return try {

            val response = api.addRunPoint(
                runId,
                AddRunPointRequest(
                    lat = latLng.latitude,
                    long = latLng.longitude,
                    speed = speed,
                    time = time.toString(),
                    accuracy = accuracy
                )
            )
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {

                Resource.Success(true)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en addRoutePoint repository: ${ex.message}")
            Resource.Error(ex.message)
        }

    }

    override suspend fun finishRun(runId: String): Resource<Boolean> {
        return try {

            val response = api.finishRun(
                runId,
            )
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {

                Resource.Success(true)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en finishRun repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Obtener todos los recorridos activos que están en la ubicación dada.
     */
    override suspend fun getNearbyActiveRuns(location: LatLng): Resource<List<NearbyRun>> {
        return try {
            val response = api.getNearbyActiveRuns(location.latitude, location.longitude)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val nearbyRuns = body.runs.map {
                    it.toEntity()
                }
                Resource.Success(nearbyRuns)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en getNearbyActiveRuns repository: ${ex.message}")
            Resource.Error(ex.message)
        }


    }

    /**
     * Obtener puntos de un recorrido ajustado al mapa.
     * @param endToEndRunId Id del recorrido end-to-end.
     */
    override suspend fun getMatchedRunPoints(endToEndRunId: EndToEndRunId): Resource<List<LatLng>> {
        return try {
            val response = api.getMatchedPoints(endToEndRunId)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val points = body.points.toLatlngList()
                Resource.Success(points)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en getMatchedRunPoints repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Obtener datos públicos de la ruta.
     * @param routeId Id de la ruta
     */
    override suspend fun getRoutePublicData(routeId: String): Resource<RoutePublicData>{
        return try {
            val response = api.getRoutePublicData(routeId)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val routeData = body.route.toEntity()
                Resource.Success(routeData)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en getRoutePublicData repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Obtener las paradas de una ruta.
     * @param routeId Id de la ruta
     */
    override suspend fun getRouteStops(routeId: String): Resource<List<Stop>> {
        return try {
            val response = api.getRouteStops(routeId)
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
            println("Diego: Error en getRouteStops repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Actualizar las paradas de una ruta
     * @param routeId Id de la ruta
     * @param stops Lista de paradas
     */
    override suspend fun updateRouteStops(routeId: String, stops: List<Stop>): Resource<Boolean> {
        return try {
            val request = UpdateRouteStopsRequest(stops.map { it.id })
            val response = api.updateRouteStops(routeId, request)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                Resource.Success(true)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en updateStops repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }

    /**
     * Obtener el recorrido más relevante de una ruta.
     * @param routeId Id de la ruta
     */
    override suspend fun getBestRouteRun(routeId: String): Resource<List<LatLng>> {
        return try {
            val response = api.getBestRouteRun(routeId)
            val body = response.body()

            // Verificamos si la respuesta fue exitosa y contiene datos válidos
            if (response.isSuccessful && body != null && body.ok) {
                val bestRouteRun = body.points.toLatlngList()
                Resource.Success(bestRouteRun)
            } else {
                // Parseo de error personalizado
                val error = ErrorParser.parseErrorMessage(response.errorBody())
                Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Diego: Error en route.getBestRouteRun repository: ${ex.message}")
            Resource.Error(ex.message)
        }
    }
}