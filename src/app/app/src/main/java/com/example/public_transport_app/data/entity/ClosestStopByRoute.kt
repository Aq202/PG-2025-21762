package com.example.public_transport_app.data.entity


data class ClosestStopByRoute(
    val route: EmbeddedRoute,
    val stop: Stop,
    val distance: Float
)