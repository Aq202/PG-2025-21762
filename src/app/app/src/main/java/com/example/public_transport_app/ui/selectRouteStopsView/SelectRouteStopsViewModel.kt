package com.example.public_transport_app.ui.selectRouteStopsView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.StopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectRouteStopsViewModel @Inject constructor(
    private val stopRepository: StopRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<SelectRouteStopsState> =
        MutableStateFlow(SelectRouteStopsState.LoadingStops)
    val uiState: StateFlow<SelectRouteStopsState> = _uiState

    fun getRouteStops(agencyId:String) {
        _uiState.value = SelectRouteStopsState.LoadingStops
        viewModelScope.launch {
            when (val result = stopRepository.getStops(agencyId)) {
                is Resource.Error -> {
                    _uiState.value = SelectRouteStopsState.Error(result.message ?: "")
                }
                is Resource.Success -> {
                    _uiState.value = SelectRouteStopsState.StopsLoaded(result.data)
                }
            }
        }
    }
}
