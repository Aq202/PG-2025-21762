package com.example.public_transport_app.data.remote.dto.response

data class GetRouteResponse(
    val message: String,
    val ok: Boolean,
    val route: RouteResponse
)