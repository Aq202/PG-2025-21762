package com.example.public_transport_app.ui.routeDetailsBottomView

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.data.entity.RoutePublicData

sealed interface RouteDetailsState {

    data object LoadingRouteDetails: RouteDetailsState
    data class RouteDetailsLoaded(val routeDetails: RoutePublicData): RouteDetailsState
    data class RouteDetailsError(val error: String): RouteDetailsState
}