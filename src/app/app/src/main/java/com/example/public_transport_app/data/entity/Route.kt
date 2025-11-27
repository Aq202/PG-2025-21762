package com.example.public_transport_app.data.entity

import com.google.android.gms.maps.model.LatLng
import java.time.DayOfWeek

data class Route(
    val id: String,
    val agency: Agency,
    val startLocation: LatLng,
    val endLocation: LatLng,
    val name: String,
    val schedules: Map<DayOfWeek, Schedule>,
    val units: List<String>,
    val unitImages: List<String>
)

