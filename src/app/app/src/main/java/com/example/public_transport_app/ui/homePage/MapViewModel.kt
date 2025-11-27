package com.example.public_transport_app.ui.homePage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.entity.EndToEndRunId
import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val routeRepository: RouteRepository
): ViewModel() {

    private val _nearbyActiveRuns: MutableStateFlow<NearbyRunsState> = MutableStateFlow(
        NearbyRunsState.Loading)
    val nearbyActiveRuns: StateFlow<NearbyRunsState> = _nearbyActiveRuns

    private var  focusedNearbyRun: NearbyRun? = null

    private val _routePrediction: MutableStateFlow<RoutePredictionState> = MutableStateFlow(
        RoutePredictionState.Loading)
    val routePrediction: StateFlow<RoutePredictionState> = _routePrediction

    val routeCache = mutableMapOf<String, List<LatLng>>()

    private val _routeBestRun: MutableStateFlow<RouteBestRunState> = MutableStateFlow(
        RouteBestRunState.Default)
    val routeBestRun: StateFlow<RouteBestRunState> = _routeBestRun

    private val _routeStops: MutableStateFlow<RouteStopsState> = MutableStateFlow(
        RouteStopsState.NoStops)
    val routeStops: StateFlow<RouteStopsState> = _routeStops

    fun getNearbyActiveRuns(location: LatLng){
        viewModelScope.launch {
            _nearbyActiveRuns.value = NearbyRunsState.Loading

            when(val runsResponse = routeRepository.getNearbyActiveRuns(location)){
                is Resource.Error -> {
                    _nearbyActiveRuns.value = NearbyRunsState.Error(runsResponse.message ?: "")
                }
                is Resource.Success -> {
                    val nearbyRuns:MutableMap<String, NearbyRun> = mutableMapOf()
                    runsResponse.data.forEach {
                        nearbyRuns.put(it.runId, it)
                    }
                    _nearbyActiveRuns.value = NearbyRunsState.Success(nearbyRuns)
                }
            }
        }
    }

    fun focusNearbyRun(nearbyRun: NearbyRun){
        focusedNearbyRun = nearbyRun
    }

    fun blurNearbyRun(){
        focusedNearbyRun = null
    }

    fun getFocusedNearbyRun(): NearbyRun?{
        return focusedNearbyRun
    }

    fun getRoutePrediction(endToEndRunId: EndToEndRunId){

        if(routeCache.containsKey(endToEndRunId)){
            _routePrediction.value = RoutePredictionState.Success(endToEndRunId,
                routeCache[endToEndRunId]!!)
            return
        }

        viewModelScope.launch {
            _routePrediction.value = RoutePredictionState.Loading

            when(val runResponse = routeRepository.getMatchedRunPoints(endToEndRunId)){
                is Resource.Error -> {
                    _routePrediction.value = RoutePredictionState.Error(runResponse.message ?: "")
                }
                is Resource.Success -> {
                    routeCache[endToEndRunId] = runResponse.data
                    _routePrediction.value = RoutePredictionState.Success(endToEndRunId,
                        runResponse.data)
                }
            }
        }
    }

    /**
     * Borra la predicción en la ruta, para que esta se deje de mostrar en el mapa.
     */
    fun clearRoutePrediction(){
        _routePrediction.value = RoutePredictionState.NoPrediction
    }

    fun getCurrentPredictionEndToEndRunId(): EndToEndRunId? {
        return when (val state = routePrediction.value) {
            is RoutePredictionState.Success -> state.endToEndRunId
            else -> null
        }
    }

    fun getRouteStops(routeId: String){
        _routeStops.value = RouteStopsState.LoadingStops

        viewModelScope.launch {
            when(val result = routeRepository.getRouteStops(routeId)){
                is Resource.Error -> {
                    _routeStops.value = RouteStopsState.Error(result.message ?: "")
                }
                is Resource.Success -> {
                    _routeStops.value = RouteStopsState.Success(result.data)
                }
            }
        }
    }

    fun getRouteBestRunPoints(routeId: String){
        _routeBestRun.value = RouteBestRunState.Loading

        viewModelScope.launch {
            when(val result = routeRepository.getBestRouteRun(routeId)){
                is Resource.Error -> {
                    _routeBestRun.value = RouteBestRunState.Error(result.message ?: "")
                }
                is Resource.Success -> {
                    _routeBestRun.value = RouteBestRunState.Success(result.data)
                }
            }
        }
    }

}