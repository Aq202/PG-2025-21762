package com.example.public_transport_app.ui.routeView

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Route

sealed interface RouteState {

    data object LoadingRoute: RouteState
    data class RouteLoaded(val route: Route): RouteState
    data class RouteError(val error: String): RouteState
}