package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.User

data class LoginResponse(
    val accessToken: String,
    val ok: Boolean,
    val refreshToken: String,
    val user: User,
    val message: String
)