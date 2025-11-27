package com.example.public_transport_app.data.entity

import com.google.android.gms.maps.model.LatLng

typealias EndToEndRunId = String

data class NearbyRun(
    val location: LatLng,
    val routeId: String,
    val routePrediction: EndToEndRunId?,
    val runId: String
)