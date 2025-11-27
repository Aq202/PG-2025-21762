package com.example.public_transport_app.data.entity

import com.google.android.gms.maps.model.LatLng

data class RunPoint(
    val closeToEndLocation: Boolean,
    val closeToStartLocation: Boolean,
    val location: LatLng,
    val routeId: String,
    val runId: String,
    val speed: Int,
    val time: String
)