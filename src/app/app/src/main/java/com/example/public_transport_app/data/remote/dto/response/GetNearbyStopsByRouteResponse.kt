package com.example.public_transport_app.data.remote.dto.response

data class GetNearbyStopsByRouteResponse(
    val ok: Boolean,
    val message: String,
    val nearbyStops: List<NearbyStopByRouteResponse>

)
