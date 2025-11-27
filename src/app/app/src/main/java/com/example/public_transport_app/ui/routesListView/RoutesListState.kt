package com.example.public_transport_app.ui.routesListView

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Route

sealed interface RoutesListState {

    data object LoadingAgencies: RoutesListState
    data class AgenciesLoaded(val agencies: List<Agency>): RoutesListState
    data object LoadingRoutes: RoutesListState
    data class RoutesLoaded(val routes: List<Route>): RoutesListState
    data class AgenciesError(val error: String):RoutesListState
    data class RoutesError(val error: String): RoutesListState
}