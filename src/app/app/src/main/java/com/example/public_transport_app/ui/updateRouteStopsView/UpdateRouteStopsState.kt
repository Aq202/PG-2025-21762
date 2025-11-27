package com.example.public_transport_app.ui.updateRouteStopsView

import com.example.public_transport_app.data.entity.Stop

sealed class UpdateRouteStopsState {
    object LoadingStops : UpdateRouteStopsState()
    data class StopsLoaded(val stops: List<Stop>) : UpdateRouteStopsState()
    data class Error(val error: String) : UpdateRouteStopsState()

    object UpdatingStops: UpdateRouteStopsState()

    object StopsUpdated: UpdateRouteStopsState()


}