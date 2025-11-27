package com.example.public_transport_app.ui.homePage

import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.data.entity.Stop

sealed class MarkerType {
    data class Bus(val nearbyRun: NearbyRun) : MarkerType()
    data class RouteStop(val stop: Stop): MarkerType()
}