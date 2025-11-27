package com.example.public_transport_app.ui.homePage

import com.example.public_transport_app.data.entity.NearbyRun


sealed interface NearbyRunsState {

    data object Loading: NearbyRunsState
    data class Success(val runs: Map<String, NearbyRun>): NearbyRunsState
    data class Error(val error: String): NearbyRunsState
}