package com.example.public_transport_app.ui.mainBottomView

import com.example.public_transport_app.data.entity.ClosestStopByRoute

sealed interface NearbyRoutesState {
    data object Loading : NearbyRoutesState
    data class Success(val nearbyRoutes: List<ClosestStopByRoute>) : NearbyRoutesState
    data class Error(val error: String) : NearbyRoutesState
}
