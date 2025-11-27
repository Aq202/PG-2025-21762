package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.EmbeddedRoute
import com.example.public_transport_app.data.entity.Stop

data class RouteEmbeddedResponse(
    val id: String,
    val name: String
)

fun RouteEmbeddedResponse.toEntity(): EmbeddedRoute {
    return EmbeddedRoute(this.id, this.name)
}

fun List<RouteEmbeddedResponse>.toEntity(): List<EmbeddedRoute> {
    return this.map { it.toEntity() }
}