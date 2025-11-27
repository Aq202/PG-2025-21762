package com.example.public_transport_app.ui.homePage

import com.google.android.gms.maps.model.LatLng


sealed interface RouteBestRunState {

    data object Loading: RouteBestRunState
    data class Success(val runPoints: List<LatLng>): RouteBestRunState
    data class Error(val error: String): RouteBestRunState
    data object Default: RouteBestRunState
}