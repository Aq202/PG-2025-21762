package com.example.public_transport_app.ui.homePage

import com.example.public_transport_app.data.entity.EndToEndRunId
import com.google.android.gms.maps.model.LatLng


sealed interface RoutePredictionState {

    data object Loading: RoutePredictionState
    data class Success(val endToEndRunId: EndToEndRunId,
                       val runPoints: List<LatLng>,
                       val version: Long = System.nanoTime()): RoutePredictionState
    data class Error(val error: String): RoutePredictionState
    data object NoPrediction: RoutePredictionState
}