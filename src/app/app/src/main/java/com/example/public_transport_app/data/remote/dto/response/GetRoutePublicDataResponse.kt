package com.example.public_transport_app.data.remote.dto.response

data class GetRoutePublicDataResponse(
    val message: String,
    val ok: Boolean,
    val route: RoutePublicDataResponse
)