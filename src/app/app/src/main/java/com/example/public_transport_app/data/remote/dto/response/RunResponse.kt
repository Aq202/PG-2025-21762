package com.example.public_transport_app.data.remote.dto.response

data class RunResponse(
    val agencyId: String,
    val id: String,
    val routeId: String,
    val time: String,
)