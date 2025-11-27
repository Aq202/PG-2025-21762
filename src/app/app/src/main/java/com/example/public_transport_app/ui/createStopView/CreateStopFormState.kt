package com.example.public_transport_app.ui.createStopView


import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.ui.createRouteView.CreateRouteFormState
import java.time.DayOfWeek

/**
 * Representa los posibles estados del formulario para la creación de paradas.
 */
sealed class CreateStopFormState {

    data object LoadingAgencies: CreateStopFormState()

    data class AgenciesLoaded(val agencies: List<Agency>): CreateStopFormState()

    /**
     * El formulario tiene uno o más campos con errores.
     * Cada campo puede contener un mensaje de error o ser null si no hay error.
     */
    data class FieldError(
        val agencyId: String? = null,
        val name: String? = null,
        val location: String? = null,

    ) : CreateStopFormState()

    /**
     * Estado inicial del formulario, sin interacción aún.
     */
    data object Default : CreateStopFormState()

    /**
     * El formulario fue enviado correctamente.
     */
    data class Success(
        val stop: Stop
    ) : CreateStopFormState()

    /**
     * El formulario se está enviando.
     */
    data object Loading : CreateStopFormState()

    /**
     * El formulario no pudo crearse o no se obtuvieron las agencias.
     */
    data class Error(val error: String) : CreateStopFormState()
}
