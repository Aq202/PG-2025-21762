package com.example.public_transport_app.data.remote.dto.response

data class GetNearbyRunsResponse(
    val message: String,
    val ok: Boolean,
    val runs: List<NearbyRunResponse>
)
