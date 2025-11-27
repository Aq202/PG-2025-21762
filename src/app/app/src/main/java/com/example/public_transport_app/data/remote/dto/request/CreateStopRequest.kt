package com.example.public_transport_app.data.remote.dto.request

data class CreateStopRequest(
    val agencyId: String,
    val name: String,
    val location: LocationRequest
)
