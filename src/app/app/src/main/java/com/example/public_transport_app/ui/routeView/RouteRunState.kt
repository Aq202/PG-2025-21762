package com.example.public_transport_app.ui.routeView

sealed class RouteRunState {
    data object Default : RouteRunState()
    data object StartingRun : RouteRunState()
    data class RunStarted(val runId: String) : RouteRunState()
    data class StartingRunError(val error: String): RouteRunState()
}
