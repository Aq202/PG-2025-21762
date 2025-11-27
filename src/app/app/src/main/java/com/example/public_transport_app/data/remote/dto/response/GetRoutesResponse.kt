package com.example.public_transport_app.data.remote.dto.response

data class GetRoutesResponse(
    val message: String,
    val ok: Boolean,
    val routes: List<RouteResponse>
)