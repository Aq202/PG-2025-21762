package com.example.public_transport_app.utils

import java.time.LocalTime

fun LocalTime.toCompactHourMinuteString(): String {
    return "${this.hour}:${this.minute.toString().padStart(2, '0')}"
}
