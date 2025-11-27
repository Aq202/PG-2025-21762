package com.example.public_transport_app.ui.selectRouteStopsView

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.ui.stopsListView.StopsListState

sealed class SelectRouteStopsState {
    object LoadingStops : SelectRouteStopsState()
    data class StopsLoaded(val stops: List<Stop>) : SelectRouteStopsState()
    data class Error(val error: String) : SelectRouteStopsState()
}