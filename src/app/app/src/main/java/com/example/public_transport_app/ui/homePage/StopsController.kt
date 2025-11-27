package com.example.public_transport_app.ui.homePage

import com.example.public_transport_app.data.entity.Stop
import kotlinx.coroutines.flow.collectLatest

class StopsController(
    private val mapController: MapController,
    private val mapViewModel: MapViewModel,
){

    fun addStopsToMap(stops:List<Stop>){
        for (stop in stops){
            mapController.addStop(stop)
        }
    }

    suspend fun observeStops(){
        mapViewModel.routeStops.collectLatest { state ->
            when(state){
                is RouteStopsState.Error,
                RouteStopsState.LoadingStops,
                RouteStopsState.NoStops -> {
                    mapController.removeRouteStops()
                }
                is RouteStopsState.Success -> {
                    mapController.removeRouteStops()
                    addStopsToMap(state.stops)
                }
            }
        }
    }

}