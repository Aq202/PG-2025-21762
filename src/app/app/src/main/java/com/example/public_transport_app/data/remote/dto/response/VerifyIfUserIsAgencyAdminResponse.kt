package com.example.public_transport_app.data.remote.dto.response

data class VerifyIfUserIsAgencyAdminResponse(
    val isAdmin: Boolean,
    val message: String,
    val ok: Boolean
)