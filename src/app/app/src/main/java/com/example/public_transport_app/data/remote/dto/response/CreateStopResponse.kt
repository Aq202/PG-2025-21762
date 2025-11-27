package com.example.public_transport_app.data.remote.dto.response

data class CreateStopResponse(
    val ok: Boolean,
    val message: String,
    val routeStop: RouteStopResponse
)