package com.example.public_transport_app.ui.mainBottomView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.StopRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainBottomViewModel @Inject constructor(
    private val stopRepository: StopRepository
): ViewModel() {

    private val _closestStopByRoute: MutableStateFlow<NearbyRoutesState> = MutableStateFlow(
        NearbyRoutesState.Loading)
    val closestStopByRoute: StateFlow<NearbyRoutesState> = _closestStopByRoute

    /**
     * Obtener las rutas cercanas
     */
    fun getClosestStopByRoute(location: LatLng){
        _closestStopByRoute.value = NearbyRoutesState.Loading

        viewModelScope.launch {
            when(val closestStopByRoute = stopRepository.getclosestStopByRoute(location)){
                is Resource.Error -> {
                    _closestStopByRoute.value = NearbyRoutesState.Error(closestStopByRoute.message ?: "")
                }
                is Resource.Success -> {
                    _closestStopByRoute.value = NearbyRoutesState.Success(closestStopByRoute.data)
                }
            }
        }
    }


}