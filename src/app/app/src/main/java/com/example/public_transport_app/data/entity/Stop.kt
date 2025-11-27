package com.example.public_transport_app.data.entity

import com.google.android.gms.maps.model.LatLng

data class Stop (
    val id: String,
    val name: String,
    val location: LatLng,
    val agency: Agency,
    val routes: List<EmbeddedRoute>?,
)