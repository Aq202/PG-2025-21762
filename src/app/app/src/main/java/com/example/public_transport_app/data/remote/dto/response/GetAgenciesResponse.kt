package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Agency

data class GetAgenciesResponse(
    val agencies: List<Agency>,
    val message: String,
    val ok: Boolean
)