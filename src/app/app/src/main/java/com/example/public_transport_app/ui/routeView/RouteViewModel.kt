package com.example.public_transport_app.ui.routeView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.repository.AgencyRepository
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val agencyRepository: AgencyRepository,
): ViewModel() {

    private val _uiState: MutableStateFlow<RouteState> = MutableStateFlow(RouteState.LoadingRoute)
    val uiState: StateFlow<RouteState> = _uiState

    private val _isAgencyAdmin: MutableStateFlow<AgencyAdminPermissionState> = MutableStateFlow(
        AgencyAdminPermissionState.Default)
    val isAgencyAdmin: StateFlow<AgencyAdminPermissionState> = _isAgencyAdmin

    private val _routeRunState: MutableStateFlow<RouteRunState> = MutableStateFlow(RouteRunState.Default)
    val routeRunState: StateFlow<RouteRunState> = _routeRunState

    /**
     * Obtener la información de una ruta
     */
    fun getRoute(routeId: String){
        _uiState.value = RouteState.LoadingRoute

        viewModelScope.launch {
            when(val routeResponse = routeRepository.getRoute(routeId)){
                is Resource.Error -> {
                    _uiState.value = RouteState.RouteError(routeResponse.message ?: "")
                }
                is Resource.Success -> {
                    _uiState.value = RouteState.RouteLoaded(routeResponse.data)
                }
            }
        }
    }

    /**
     * Iniciar un nuevo viaje para la unidad.
     */
    fun startRun(routeId: String, time: Instant){
        _routeRunState.value = RouteRunState.StartingRun

        viewModelScope.launch {
            when(val runResponse = routeRepository.startRun(
                routeId,
                time
            )){
                is Resource.Error -> {
                    _routeRunState.value = RouteRunState.StartingRunError(runResponse.message ?: "")
                }
                is Resource.Success -> {
                    // Enviar el ID del viaje (run)
                    _routeRunState.value = RouteRunState.RunStarted(runResponse.data.id)
                }
            }
        }
    }

    fun finishRun(runId: String){

        viewModelScope.launch {
            val response = routeRepository.finishRun(runId)
            if(response is Resource.Error){
                println("Diego: Error al finalizar ruta: ${response.message}")
            }
        }
    }

    fun verifyIfUserIsAgencyAdmin(agencyId: String){
        viewModelScope.launch {
            when(val result = agencyRepository.verifyIfUserIsAgencyAdmin(agencyId)){
                is Resource.Error ->{
                    _isAgencyAdmin.value = AgencyAdminPermissionState.Error(result.message ?: "")
                }
                is Resource.Success -> {
                    _isAgencyAdmin.value = if(result.data){
                        AgencyAdminPermissionState.IsAgencyAdmin
                    }else{
                        AgencyAdminPermissionState.NoPermission
                    }
                }
            }
        }
    }
}