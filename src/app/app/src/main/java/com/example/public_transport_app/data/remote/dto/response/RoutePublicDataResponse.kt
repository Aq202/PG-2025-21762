package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.RoutePublicData

data class RoutePublicDataResponse(
    val id: String,
    val name: String,
    val schedules: List<ScheduleResponse>,
    val unitImages: List<String>
)

fun RoutePublicDataResponse.toEntity(): RoutePublicData {
    return RoutePublicData(
        id = id,
        name = name,
        schedules = schedules.toEntity(),
        unitImages = unitImages
    )
}
