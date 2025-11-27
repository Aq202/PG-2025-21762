package com.example.public_transport_app.data.remote.dto.request


data class AddRunPointRequest(
    val lat: Double,
    val long: Double,
    val speed: Float,
    val time: String,
    val accuracy: Float
)
