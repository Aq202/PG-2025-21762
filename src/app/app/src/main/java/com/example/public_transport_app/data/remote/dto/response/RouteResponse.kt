package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.data.entity.Schedule
import com.google.android.gms.maps.model.LatLng
import java.time.DayOfWeek
import java.time.LocalTime

data class LocationResponse(
    val lat: Double,
    val long: Double
)

data class RouteResponse(
    val agency: AgencyEmbeddedResponse,
    val id: String,
    val name: String,
    val schedules: List<ScheduleResponse>,
    val startLocation: LocationResponse,
    val endLocation: LocationResponse,
    val unitImages: List<String>,
    val units: List<String>
)

// Extension function para convertir Route response a Entity
fun RouteResponse.toEntity(): Route {

    // Convertimos la lista de horarios a un Map<DayOfWeek, Schedule>
    val scheduleMap = mutableMapOf<DayOfWeek, Schedule>()
    for (schedule in this.schedules) {

        val openTime = runCatching { LocalTime.parse(schedule.open) }.getOrNull()
        val closeTime = runCatching { LocalTime.parse(schedule.close) }.getOrNull()

        scheduleMap[DayOfWeek.of(schedule.day)] = Schedule(
            open = openTime,
            close = closeTime,
            serviceAvailable = schedule.serviceAvailable
        )
    }

    // Mapeamos el DTO a la entidad Route
    return Route(
        id = this.id,
        agency = this.agency.toEntity(),
        startLocation = LatLng(this.startLocation.lat, this.startLocation.long),
        endLocation = LatLng(this.endLocation.lat, this.endLocation.long),
        name = this.name,
        schedules = scheduleMap,
        units = this.units,
        unitImages = this.unitImages
    )
}