package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Schedule
import java.time.DayOfWeek
import java.time.LocalTime

data class ScheduleResponse(
    val day: Int,
    val open: String?,
    val close: String?,
    val serviceAvailable: Boolean
)

fun ScheduleResponse.toEntity(): Schedule {
    return Schedule(
        open = open?.let { LocalTime.parse(it) },
        close = close?.let { LocalTime.parse(it) },
        serviceAvailable = serviceAvailable
    )
}

fun List<ScheduleResponse>.toEntity(): Map<DayOfWeek, Schedule> {
    val schedules = mutableMapOf<DayOfWeek, Schedule>()
    this.forEach { scheduleResponse ->
        schedules[DayOfWeek.of(scheduleResponse.day)] = scheduleResponse.toEntity()
    }
    return schedules
}