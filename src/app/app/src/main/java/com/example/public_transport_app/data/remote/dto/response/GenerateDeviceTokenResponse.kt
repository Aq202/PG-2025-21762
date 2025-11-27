package com.example.public_transport_app.data.remote.dto.response

data class GenerateDeviceTokenResponse(
    val deviceToken: String,
    val ok: Boolean,
    val message: String?
)