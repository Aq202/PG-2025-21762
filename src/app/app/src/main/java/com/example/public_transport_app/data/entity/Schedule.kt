package com.example.public_transport_app.data.entity

import java.time.LocalTime

data class Schedule(
    val open: LocalTime?,
    val close: LocalTime?,
    val serviceAvailable: Boolean
)
