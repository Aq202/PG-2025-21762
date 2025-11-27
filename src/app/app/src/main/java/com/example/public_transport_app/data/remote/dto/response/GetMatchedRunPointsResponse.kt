package com.example.public_transport_app.data.remote.dto.response

data class GetMatchedRunPointsResponse(
    val message: String,
    val ok: Boolean,
    val points: List<Location>
)