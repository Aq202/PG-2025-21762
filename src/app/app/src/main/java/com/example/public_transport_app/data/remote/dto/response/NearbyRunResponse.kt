package com.example.public_transport_app.data.remote.dto.response

import com.example.public_transport_app.data.entity.NearbyRun
import com.google.android.gms.maps.model.LatLng

data class NearbyRunResponse(
    val location: Location,
    val routeId: String,
    val routePrediction: String,
    val runId: String
)

fun NearbyRunResponse.toEntity(): NearbyRun {
    return NearbyRun(
        location = LatLng(location.lat, location.long),
        routeId = routeId,
        routePrediction = routePrediction,
        runId = runId
    )

}