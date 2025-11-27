package com.example.public_transport_app.ui.stopsListView

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.ui.routesListView.RoutesListState

sealed class StopsListState {

    data object LoadingAgencies: StopsListState()
    data class AgenciesLoaded(val agencies: List<Agency>): StopsListState()
    data class AgenciesError(val error: String):StopsListState()
    object LoadingStops : StopsListState()
    data class StopsLoaded(val stops: List<Stop>) : StopsListState()
    data class Error(val error: String) : StopsListState()
}