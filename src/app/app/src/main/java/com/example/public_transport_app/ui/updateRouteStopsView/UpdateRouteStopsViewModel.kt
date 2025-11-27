package com.example.public_transport_app.ui.updateRouteStopsView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class UpdateRouteStopsViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<UpdateRouteStopsState> =
        MutableStateFlow(UpdateRouteStopsState.LoadingStops)
    val uiState: StateFlow<UpdateRouteStopsState> = _uiState

    private var hasLoaded = false
    private var currentStops: MutableList<Stop> = mutableListOf()

    fun getRouteStops(routeId: String) {
        if (hasLoaded) {
            // Ya estaban cargadas, solo emite el estado actual
            _uiState.value = UpdateRouteStopsState.StopsLoaded(currentStops)
            return
        }

        _uiState.value = UpdateRouteStopsState.LoadingStops
        viewModelScope.launch {
            when (val result = routeRepository.getRouteStops(routeId)) {
                is Resource.Error -> {
                    _uiState.value = UpdateRouteStopsState.Error(result.message ?: "Error al obtener paradas")
                }
                is Resource.Success -> {
                    hasLoaded = true
                    currentStops = result.data.toMutableList()
                    _uiState.value = UpdateRouteStopsState.StopsLoaded(currentStops)
                }
            }
        }
    }

    fun addStop(stop: Stop) {
        currentStops = (currentStops + stop).toMutableList()
        _uiState.value = UpdateRouteStopsState.StopsLoaded(currentStops)
    }

    fun removeStop(stopId: String) {
        currentStops = currentStops.filter { it.id != stopId }.toMutableList()
        _uiState.value = UpdateRouteStopsState.StopsLoaded(currentStops)
    }

    fun updateRouteStops(routeId: String, stops: List<Stop>) {
        _uiState.value = UpdateRouteStopsState.UpdatingStops
        viewModelScope.launch {
            when (val result = routeRepository.updateRouteStops(routeId, stops)) {
                is Resource.Error -> {
                    _uiState.value = UpdateRouteStopsState.Error(result.message ?: "")
                }
                is Resource.Success -> {
                    _uiState.value = UpdateRouteStopsState.StopsUpdated
                }
            }
        }
    }
}
