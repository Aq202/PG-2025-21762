package com.example.public_transport_app.ui.createRouteView

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.ui.routesListView.RoutesListState
import java.time.DayOfWeek

/**
 * Representa los posibles estados del formulario para la creación de rutas.
 */
sealed class CreateRouteFormState {

    data object LoadingAgencies: CreateRouteFormState()

    data class AgenciesLoaded(val agencies: List<Agency>): CreateRouteFormState()

    data class AgenciesError(val error: String):CreateRouteFormState()

    /**
     * El formulario tiene uno o más campos con errores.
     * Cada campo puede contener un mensaje de error o ser null si no hay error.
     */
    data class FieldError(
        val agencyId: String? = null,
        val startLocation: String? = null,
        val endLocation: String? = null,
        val name: String? = null,
        val startSchedule: Map<DayOfWeek, String?> = emptyMap(),
        val endSchedule: Map<DayOfWeek, String?> = emptyMap(),
        val unitsImages: String? = null,
        val unitsId:String? = null

    ) : CreateRouteFormState()

    /**
     * Estado inicial del formulario, sin interacción aún.
     */
    data object Default : CreateRouteFormState()

    /**
     * El formulario es válido y puede enviarse.
     */
    data class Success(
        val route: Route
    ) : CreateRouteFormState()

    /**
     * El formulario se está enviando.
     */
    data object Loading : CreateRouteFormState()

    /**
     * El formulario no pudo crearse. Error general.
     */
    data class Error(val error: String) : CreateRouteFormState()
}
