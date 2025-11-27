package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Stop

data class RouteStopResponse(
    val id: String,
    val location: Location,
    val name: String,
    val agency: AgencyEmbeddedResponse,
    val routes: List<RouteEmbeddedResponse>,
)

fun RouteStopResponse.toEntity(): Stop{
    return Stop(
        name = this.name,
        location = this.location.toLatlng(),
        id = this.id,
        agency = this.agency.toEntity(),
        routes = this.routes.toEntity()
    )
}

fun List<RouteStopResponse>.toEntity(): List<Stop> {
    return this.map { it.toEntity() }
}