package com.example.public_transport_app.data.entity

import com.google.android.gms.maps.model.LatLng
import java.time.DayOfWeek

data class RoutePublicData(
    val id: String,
    val name: String,
    val schedules: Map<DayOfWeek, Schedule>,
    val unitImages: List<String>
)