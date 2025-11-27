package com.example.public_transport_app.ui.homePage

import com.example.public_transport_app.data.entity.Stop

sealed class RouteStopsState {
    object NoStops: RouteStopsState()
    object LoadingStops: RouteStopsState()
    data class Success(val stops: List<Stop>): RouteStopsState()
    data class Error(val message: String): RouteStopsState()
}