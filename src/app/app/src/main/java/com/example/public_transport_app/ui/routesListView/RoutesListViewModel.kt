package com.example.public_transport_app.ui.routesListView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.repository.AgencyRepository
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesListViewModel @Inject constructor(
    private val agencyRepository: AgencyRepository,
    private val routeRepository: RouteRepository
): ViewModel() {

    private val _uiState: MutableStateFlow<RoutesListState> = MutableStateFlow(RoutesListState.LoadingAgencies)
    val uiState: StateFlow<RoutesListState> = _uiState

    fun getAgencies(){

        viewModelScope.launch {
            _uiState.value = RoutesListState.LoadingAgencies

            when(val agenciesResult = agencyRepository.getAgencies()){
                is Resource.Error<*> -> {
                    println("Error al obtener agencies: " + agenciesResult.message)
                    _uiState.value = RoutesListState.AgenciesError(agenciesResult.message ?: "Ocurrió un error al obtener agencias.")
                }
                is Resource.Success<List<Agency>> -> {
                    _uiState.value = RoutesListState.AgenciesLoaded(agenciesResult.data)
                }
            }
        }

    }

    /**
     * Obtener el listado de rutas para una agencia dada
     */
    fun getRoutes(agencyId: String){
        _uiState.value = RoutesListState.LoadingRoutes

        viewModelScope.launch {
            when(val routesResponse = routeRepository.getRoutesList(agencyId)){
                is Resource.Error -> {
                    _uiState.value = RoutesListState.RoutesError(routesResponse.message ?: "")
                }
                is Resource.Success -> {
                    _uiState.value = RoutesListState.RoutesLoaded(routesResponse.data)
                }
            }
        }
    }
}