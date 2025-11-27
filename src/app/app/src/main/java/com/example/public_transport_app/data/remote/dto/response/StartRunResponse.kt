package com.example.public_transport_app.data.remote.dto.response


data class StartRunResponse(
    val message: String,
    val ok: Boolean,
    val run: RunResponse,
)
