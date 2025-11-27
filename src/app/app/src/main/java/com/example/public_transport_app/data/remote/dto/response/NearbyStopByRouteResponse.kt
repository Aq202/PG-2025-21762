package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.ClosestStopByRoute

data class NearbyStopByRouteResponse(
    val route: RouteEmbeddedResponse,
    val stop: RouteStopResponse,
    val distance: Float
)

fun NearbyStopByRouteResponse.toEntity() : ClosestStopByRoute {
    return ClosestStopByRoute(
        this.route.toEntity(),
        this.stop.toEntity(),
        this.distance,
    )
}

fun List<NearbyStopByRouteResponse>.toEntity() : List<ClosestStopByRoute>{
    return this.map{ it.toEntity() }
}