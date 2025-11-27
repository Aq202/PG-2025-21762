package com.example.public_transport_app.ui.homePage

import android.content.Context
import android.widget.Toast
import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.utils.updateNearbyActiveRunsInterval
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime
import java.time.ZoneId

class NearbyRunsController(
    val context: Context,
    val mapViewModel: MapViewModel,
    val mapController: MapController,
    val bottomSheetController: BottomSheetController,
) {

    private var lastNearbyRunsUpdate: LocalDateTime? = null


    /**
     * Realiza la petición de buses cercanos si ya se cumplió el intervalo mínimo para actualizarlos.
     * @param lastNearbyRunsUpdate tiempo de última actualización de buses cercanos
     * @return tiempo de última actualización de buses cercanos (actualizado)
     */
    fun requestToUpdateNearbyRunsIfNeeded(location: LatLng){
        // Obtener tiempo transcurrido desde última actualización de buses cercanos
        val nowMillis = System.currentTimeMillis()
        val lastUpdateMillis = lastNearbyRunsUpdate
            ?.atZone(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli() ?: 0L

        val diffMillis = nowMillis - lastUpdateMillis

        // Verificar si ya se cumplió el intervalo mínimo para actualizar buses cercanos
        if (lastNearbyRunsUpdate == null || diffMillis >= updateNearbyActiveRunsInterval) {
            // Actualizar buses cercanos
            mapViewModel.getNearbyActiveRuns(location)
            lastNearbyRunsUpdate = LocalDateTime.now()
        }

    }

    fun updateNearbyActiveRuns(runs: Map<String, NearbyRun>){

        val nearbyRuns = runs.values.toList()

        // Eliminar recorridos que ya no aparecen
        mapController.removeBusesNotInList(nearbyRuns.map { it.runId })

        // Agregar (o actualizar) recorridos
        nearbyRuns.forEach { nearbyRun ->
            if (mapController.hasBus(nearbyRun.runId)){
                updateExistingBus(nearbyRun)
            }else{
                addNewBus(nearbyRun)
            }
        }

        // Verificar si el recorrido seleccionado desapareció
        handleFocusedRunDisappearance(runs)
    }

    private fun updateExistingBus(nearbyRun: NearbyRun){

        mapController.updateBusLocation(
            nearbyRun,
            nearbyRun.location
        )

        // Verificar si el recorrido está seleccionado
        if(nearbyRun.runId == mapViewModel.getFocusedNearbyRun()?.runId){
            val routePrediction = nearbyRun.routePrediction

            // Aplicar cambios en predicción
            if(routePrediction != null){
                // Actualizar predicción solo si cambió
                if(routePrediction != mapViewModel.getCurrentPredictionEndToEndRunId()){
                    mapViewModel.getRoutePrediction(routePrediction)
                }
            }else{
                mapViewModel.clearRoutePrediction()
            }

            // Si la cámara está centrado en la unidad, actualizar ubicación
            mapController.followBusIfNeeded(nearbyRun.location)
        }
    }

    private fun addNewBus(nearbyRun: NearbyRun) {
        mapController.addBus(
            nearbyRun,
            LatLng(nearbyRun.location.latitude, nearbyRun.location.longitude)
        )
    }

    private fun handleFocusedRunDisappearance(runs: Map<String, NearbyRun>){
        // Verificar si el recorrido seleccionado desapareció
        val focusedNearbyRunDisappeared = !runs.containsKey(mapViewModel.getFocusedNearbyRun()?.runId)
        if(focusedNearbyRunDisappeared){
            mapViewModel.clearRoutePrediction()
            mapViewModel.blurNearbyRun()

            // Si la cámara estaba siguiendo al bus, resetear
            mapController.apply{
                if (this.isCameraFollowingBus()){
                    this.resetCameraObjective()
                }
            }
        }
    }

    fun focusNearbyRunInMap(nearbyRun: NearbyRun){

        // Borrar lineas previas si la predicción cambió
        if(mapViewModel.getFocusedNearbyRun()?.runId != nearbyRun.runId){
            mapController.removeRouteLine()
        }

        // Si el recorrido del bus tiene predicción, obtenerla para dibujarla
        if(nearbyRun.routePrediction != null){
            mapViewModel.getRoutePrediction(nearbyRun.routePrediction)
        }

        mapViewModel.getRouteStops(nearbyRun.routeId)

        mapViewModel.focusNearbyRun(nearbyRun)

        mapController.centerCameraInBusLocation(nearbyRun.location)

        // Navegar hacia ventana de detalles de la ruta
        bottomSheetController.navigateToRouteDetails(nearbyRun.routeId)
        bottomSheetController.expandHalf()
    }

    fun blurNearbyRunInMap(){
        mapViewModel.blurNearbyRun()
        mapController.removeRouteLine()
        mapController.removeRouteStops()

        // Dejar de seguir recorrido con la cámara
        mapController.resetCameraObjective()


        // Cerrar detalles de la parada
        bottomSheetController.collapse()
        bottomSheetController.navigateToMainView()
    }

    fun removeAllBuses(){
        mapController.removeAllBuses()
    }

    suspend fun observeNearbyActiveRuns(){
        mapViewModel.nearbyActiveRuns.collectLatest { nearbyActiveRuns ->
            when(nearbyActiveRuns){
                NearbyRunsState.Loading -> {
                    // Al estar cargando, ignorar
                }
                is NearbyRunsState.Error -> {
                    // Eliminar todos los marcadores si hay error
                    removeAllBuses()
                }
                is NearbyRunsState.Success -> {
                    updateNearbyActiveRuns(nearbyActiveRuns.runs)
                }
            }
        }
    }
}