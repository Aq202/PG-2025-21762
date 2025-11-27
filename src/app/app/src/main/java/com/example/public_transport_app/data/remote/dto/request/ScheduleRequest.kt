package com.example.public_transport_app.data.remote.dto.request

data class ScheduleRequest(
    val serviceAvailable: Boolean,
    val day: Int,
    val open: String?,
    val close: String?
)