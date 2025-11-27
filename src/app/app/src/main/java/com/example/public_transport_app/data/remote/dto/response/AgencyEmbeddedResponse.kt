package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Agency

data class AgencyEmbeddedResponse(
    val id: String,
    val name: String,
)

fun AgencyEmbeddedResponse.toEntity(): Agency {
    return Agency(this.id, this.name)
}