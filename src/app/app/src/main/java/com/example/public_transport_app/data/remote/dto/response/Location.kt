package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.NearbyRun
import com.google.android.gms.maps.model.LatLng

data class Location(
    val lat: Double,
    val long: Double
)

fun Location.toLatlng(): LatLng {
    return LatLng(lat, long)
}

fun List<Location>.toLatlngList(): List<LatLng> {
    return this.map { it.toLatlng() }
}

