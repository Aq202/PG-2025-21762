package com.example.public_transport_app.ui.stopsListView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.repository.AgencyRepository
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.StopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StopsListViewModel @Inject constructor(
    private val stopRepository: StopRepository,
    private val agencyRepository: AgencyRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<StopsListState> =
        MutableStateFlow(StopsListState.LoadingStops)
    val uiState: StateFlow<StopsListState> = _uiState

    fun getAgencies(){

        viewModelScope.launch {
            _uiState.value = StopsListState.LoadingAgencies

            when(val agenciesResult = agencyRepository.getAgencies()){
                is Resource.Error<*> -> {
                    _uiState.value = StopsListState.AgenciesError(agenciesResult.message ?: "")
                }
                is Resource.Success<List<Agency>> -> {
                    _uiState.value = StopsListState.AgenciesLoaded(agenciesResult.data)
                }
            }
        }

    }

    fun getRouteStops(agencyId: String) {
        _uiState.value = StopsListState.LoadingStops
        viewModelScope.launch {
            when (val result = stopRepository.getStops(agencyId)) {
                is Resource.Error -> {
                    _uiState.value = StopsListState.Error(result.message ?: "")
                }
                is Resource.Success -> {
                    _uiState.value = StopsListState.StopsLoaded(result.data)
                }
            }
        }
    }
}
