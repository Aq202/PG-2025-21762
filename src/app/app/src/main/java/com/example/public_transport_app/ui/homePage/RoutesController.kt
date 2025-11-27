package com.example.public_transport_app.ui.homePage

import android.content.Context
import android.widget.Toast
import com.example.public_transport_app.data.entity.RouteAndStop
import kotlinx.coroutines.flow.collectLatest

class RoutesController(
    private val context: Context,
    private val mapViewModel: MapViewModel,
    private val mapController: MapController,
    private val bottomSheetController: BottomSheetController,
    private val centerLocationButtonController: CenterLocationButtonController,
) {
    fun showRouteInfo(routeAndStop: RouteAndStop){
        val route = routeAndStop.route
        val stop = routeAndStop.stop

        // Obtener paradas de la ruta
        mapViewModel.getRouteStops(route.id)

        // Obtener recorrido de la ruta más relevante
        mapViewModel.getRouteBestRunPoints(route.id)

        // Borrar predicciones previas
        mapController.removeRouteLine()

        // Centrar en la parada más cercana al usuario
        mapController.animateMoveCamera(
            stop.location,
            14f
        )

        // Dejar de tener un objetivo en la camara
        mapController.resetCameraObjective()
        centerLocationButtonController.setIsCameraFollowingUser(false)

        // Navegar hacia ventana de detalles de la ruta
        bottomSheetController.navigateToRouteDetails(route.id)
        bottomSheetController.expandHalf()
    }

    suspend fun observeRouteBestRunPoints(){
        mapViewModel.routeBestRun.collectLatest { pointsState ->
            when(pointsState){
                RouteBestRunState.Default,
                RouteBestRunState.Loading -> { }
                is RouteBestRunState.Error -> {
                    Toast.makeText(context, pointsState.error, Toast.LENGTH_SHORT).show()
                }
                is RouteBestRunState.Success -> {
                    mapController.drawRouteLine(pointsState.runPoints)
                }
            }
        }
    }
}