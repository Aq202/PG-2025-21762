package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Stop

data class GetRouteStopsResponse(
    val message: String,
    val ok: Boolean,
    val routeStops: List<RouteStopResponse>
)