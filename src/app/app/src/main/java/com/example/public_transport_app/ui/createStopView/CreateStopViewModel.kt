package com.example.public_transport_app.ui.createStopView

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.repository.AgencyRepository
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.StopRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateStopViewModel @Inject constructor(
    private val agencyRepository: AgencyRepository,
    private val stopRepository: StopRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _formState: MutableStateFlow<CreateStopFormState> =
        MutableStateFlow(CreateStopFormState.Default)
    val formState: StateFlow<CreateStopFormState> = _formState

    private val _agencyId: MutableStateFlow<String> = MutableStateFlow("")
    
    private val _stopName: MutableStateFlow<String> = MutableStateFlow("")
    val stopName: StateFlow<String> = _stopName

    private val _location: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    val location: StateFlow<LatLng?> = _location


    fun getAgencies() {

        viewModelScope.launch {
            _formState.value = CreateStopFormState.LoadingAgencies

            when (val agenciesResult = agencyRepository.getAgencies()) {
                is Resource.Error<*> -> {
                    _formState.value = CreateStopFormState.Error(
                        agenciesResult.message ?: ""
                    )
                }

                is Resource.Success<List<Agency>> -> {
                    _formState.value = CreateStopFormState.AgenciesLoaded(agenciesResult.data)
                }
            }
        }
    }

    /**
     * Obtiene el objeto de errores si existe, de lo contrario retorna uno nuevo.
     */
    private fun getCurrentErrors(): CreateStopFormState.FieldError {
        return (_formState.value as? CreateStopFormState.FieldError)
            ?: CreateStopFormState.FieldError()
    }

    private fun validateAgencyId(agencyId: String):String?{
        if (agencyId.trim().isEmpty()) {
            return context.getString(R.string.error_createStop_agencyRequired)
        }
        return null
    }

    fun clearAgencyIdError(){
        val currentErrors = getCurrentErrors()
        _formState.value = currentErrors.copy(
            agencyId = null
        )
    }

    fun setAgencyId(agencyId: String) {

        val error = validateAgencyId(agencyId)
        if (error != null) {
            val currentErrors = getCurrentErrors()
            _formState.value = currentErrors.copy(
                agencyId = error
            )
            return
        }

        _agencyId.value = agencyId.trim()
    }

    private fun validateStopName(stopName: String):String?{
        if (stopName.trim().isEmpty()) {
            return context.getString(R.string.error_createStop_name_required)
        }
        return null
    }

    fun setStopName(stopName: String) {
        val error = validateStopName(stopName)
        if (error != null) {
            val currentErrors = getCurrentErrors()
            _formState.value = currentErrors.copy(
                name = error
            )
            return
        }

        _stopName.value = stopName.trim()
    }

    fun clearStopNameError() {
        val currentErrors = getCurrentErrors()
        _formState.value = currentErrors.copy(
            name = null
        )
    }

    private fun validateLocation(location: LatLng?):String?{
        if(location == null){
            return context.getString(R.string.error_createStop_location_required)
        }
        return null
    }

    fun setLocation(location: LatLng) {
        _location.value = location
    }

    /**
     * Validación completa y envío del formulario.
     */
    fun createStop(){
        // Validar campos del formulario

        val agencyId = _agencyId.value
        val agencyIdError = validateAgencyId(agencyId)

        val stopName = _stopName.value
        val routeNameError = validateStopName(stopName)

        val location = _location.value
        val locationError = validateLocation(location)


        val hasErrors = agencyIdError != null || routeNameError != null || locationError != null

        if(hasErrors){

            _formState.value = CreateStopFormState.FieldError(
                agencyId = agencyIdError,
                name = routeNameError,
                location = locationError,
            )
        }else{

            // No hay errores, realizar la llamada al repositorio.
            _formState.value = CreateStopFormState.Loading

            viewModelScope.launch {
                val result = stopRepository.createStop(
                    agencyId = agencyId,
                    stopName = stopName,
                    location = location as LatLng,
                )

                when(result){
                    is Resource.Error -> {
                        val error = result.message
                        _formState.value = CreateStopFormState.Error(error ?: "")
                    }
                    is Resource.Success -> {
                        val route = result.data
                        _formState.value = CreateStopFormState.Success(route)
                    }
                }
            }
        }

    }

}