package com.example.public_transport_app.ui.routeDetailsBottomView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.RouteRepository
import com.example.public_transport_app.ui.routeView.RouteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteDetailsViewModel @Inject constructor(
    private val routeRepository: RouteRepository
): ViewModel() {

    private val _routeDetails: MutableStateFlow<RouteDetailsState> = MutableStateFlow(RouteDetailsState.LoadingRouteDetails)
    val routeDetails: StateFlow<RouteDetailsState> = _routeDetails

    /**
     * Obtener la información de una ruta
     */
    fun getRouteDetails(routeId: String){
        _routeDetails.value = RouteDetailsState.LoadingRouteDetails

        viewModelScope.launch {
            when(val routeResponse = routeRepository.getRoutePublicData(routeId)){
                is Resource.Error -> {
                    _routeDetails.value = RouteDetailsState.RouteDetailsError(routeResponse.message ?: "")
                }
                is Resource.Success -> {
                    _routeDetails.value = RouteDetailsState.RouteDetailsLoaded(routeResponse.data)
                }
            }
        }
    }


}