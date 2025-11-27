package com.example.public_transport_app.ui.createRouteView

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.repository.AgencyRepository
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CreateRouteViewModel @Inject constructor(
    private val agencyRepository: AgencyRepository,
    private val routeRepository: RouteRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _formState: MutableStateFlow<CreateRouteFormState> =
        MutableStateFlow(CreateRouteFormState.Default)
    val formState: StateFlow<CreateRouteFormState> = _formState

    // States de campos del formulario
    private val _agencyId: MutableStateFlow<String> = MutableStateFlow("")

    private val _routeName: MutableStateFlow<String> = MutableStateFlow("")
    val routeName: StateFlow<String> = _routeName

    private val _startLocation: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    val startLocation: StateFlow<LatLng?> = _startLocation

    private val _endLocation: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    val endLocation: StateFlow<LatLng?> = _endLocation

    private val _imageUris: MutableStateFlow<List<Uri>> = MutableStateFlow(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    private val _startSchedule = MutableStateFlow<Map<DayOfWeek, LocalTime?>>(emptyMap())
    val startSchedule: StateFlow<Map<DayOfWeek, LocalTime?>> = _startSchedule

    private val _endSchedule = MutableStateFlow<Map<DayOfWeek, LocalTime?>>(emptyMap())
    val endSchedule: StateFlow<Map<DayOfWeek, LocalTime?>> = _endSchedule

    private val _unitsId = MutableStateFlow<Set<String>>(emptySet())
    val unitsId: StateFlow<Set<String>> = _unitsId

    fun getAgencies() {

        viewModelScope.launch {
            _formState.value = CreateRouteFormState.LoadingAgencies

            when (val agenciesResult = agencyRepository.getAgencies()) {
                is Resource.Error<*> -> {
                    println("Error al obtener agencies en formulario de creación de ruta: " + agenciesResult.message)
                    _formState.value = CreateRouteFormState.AgenciesError(
                        agenciesResult.message ?: "Ocurrió un error al obtener agencias."
                    )
                }

                is Resource.Success<List<Agency>> -> {
                    _formState.value = CreateRouteFormState.AgenciesLoaded(agenciesResult.data)
                }
            }
        }

    }

    /**
     * Obtiene el objeto de errores si existe, de lo contrario retorna uno nuevo.
     */
    private fun getCurrentErrors(): CreateRouteFormState.FieldError {
        return (_formState.value as? CreateRouteFormState.FieldError)
            ?: CreateRouteFormState.FieldError()
    }

    private fun validateAgencyId(agencyId: String):String?{
        if (agencyId.trim().isEmpty()) {
            return "El ID de la agencia no puede estar vacío."
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

    private fun validateRouteName(routeName: String):String?{
        if (routeName.trim().isEmpty()) {
            return "El nombre de la ruta es obligatorio."
        }
        return null
    }

    fun setRouteName(routeName: String) {
        val error = validateRouteName(routeName)
        if (error != null) {
            val currentErrors = getCurrentErrors()
            _formState.value = currentErrors.copy(
                name = error
            )
            return
        }

        _routeName.value = routeName.trim()
    }

    fun clearRouteNameError() {
        val currentErrors = getCurrentErrors()
        _formState.value = currentErrors.copy(
            name = null
        )
    }

    private fun validateStartLocation(startLocation: LatLng?):String?{
        if(startLocation == null){
           return "El punto de partida es obligatorio."
        }
        return null
    }

    fun setStartLocation(startLocation: LatLng) {
        _startLocation.value = startLocation
    }

    private fun validateEndLocation(endLocation: LatLng?):String?{
        if(endLocation == null){
            return "El punto de llegada es obligatorio."
        }
        return null
    }

    fun setEndLocation(endLocation: LatLng) {
        _endLocation.value = endLocation
    }

    private fun validateUnitsImages(imageUris:List<Uri>):String?{
        if(imageUris.isEmpty()){
            return "Debes agregar al menos una foto de las unidades."
        }else if (imageUris.size > 10){
            return "Solo puedes añadir 10 fotos de unidades."
        }
        return null
    }

    fun clearUnitsImagesError() {
        val currentErrors = getCurrentErrors()
        _formState.value = currentErrors.copy(
            unitsImages = null
        )
    }

    fun setUnitsImages(imageUris: MutableList<Uri>) {
        _imageUris.value = imageUris
    }

    fun removeUnitImage(imageUri: Uri) {
        _imageUris.value = _imageUris.value.toMutableList().apply {
            remove(imageUri)
        }
    }

    private fun validateStartSchedule(day: DayOfWeek, startSchedule: Map<DayOfWeek, LocalTime?>): String?{

        if(!startSchedule.containsKey(day)){
            return "Especifica el horario de cierre."
        }

        return null
    }

    fun clearStartScheduleError(day: DayOfWeek) {
        val currentState = _formState.value

        if (currentState is CreateRouteFormState.FieldError) {
            // Si hay errores, eliminar el error del día proporcionado
            val updatedErrors = currentState.startSchedule.toMutableMap().apply {
                this[day] = null // eliminar el error del día
            }

            _formState.value = currentState.copy(startSchedule = updatedErrors)
        }
    }


    fun setStartSchedule(day: DayOfWeek, time: LocalTime) {
        _startSchedule.value = _startSchedule.value.toMutableMap().apply {
            put(day, time)
        }
    }

    private fun validateEndSchedule(day: DayOfWeek, endSchedule: Map<DayOfWeek, LocalTime?>): String?{

        if(!endSchedule.containsKey(day)){
            return "Especifica el horario de cierre."
        }

        return null
    }

    fun clearEndScheduleError(day: DayOfWeek) {
        val currentState = _formState.value

        if (currentState is CreateRouteFormState.FieldError) {
            // Si hay errores, eliminar el error del día proporcionado
            val updatedErrors = currentState.endSchedule.toMutableMap().apply {
                this[day] = null // eliminar el error del día
            }

            _formState.value = currentState.copy(endSchedule = updatedErrors)
        }
    }

    fun setEndSchedule(day: DayOfWeek, time: LocalTime) {
        _endSchedule.value = _endSchedule.value.toMutableMap().apply {
            put(day, time)
        }
    }

    /**
     * Indica que el día proporcionado sí tendrá operaciones la ruta.
     * Se resetea el valor del día, eliminándolo del map de horarios start y end.
     */
    fun enableWorkDay(day: DayOfWeek) {
        _startSchedule.value = _startSchedule.value.toMutableMap().apply {
            remove(day)
        }
        _endSchedule.value = _endSchedule.value.toMutableMap().apply {
            remove(day)
        }
    }

    /**
     * Indica que el día proporcionado no tendrá operaciones la ruta.
     * Se coloca null en horarios start y end.
     */
    fun disableWorkDay(day: DayOfWeek) {
        _startSchedule.value = _startSchedule.value.toMutableMap().apply {
            put(day, null)
        }
        _endSchedule.value = _endSchedule.value.toMutableMap().apply {
            put(day, null)
        }
    }

    fun addUnitId(unitId: String) {
        val unitsSet = _unitsId.value.toMutableSet()
        unitsSet.add(unitId)

        _unitsId.value = unitsSet
    }

    fun removeUnitId(unitId: String){
        val unitsSet = _unitsId.value.toMutableSet()
        unitsSet.remove(unitId)

        _unitsId.value = unitsSet
    }

    /**
     * Crea un archivo temporal a partir de un [Uri].
     *
     * Copia el contenido del [Uri] a un archivo dentro del caché de la app.
     *
     * @param context Contexto usado para acceder al contenido y caché.
     * @param uri Uri de la imagen o archivo a convertir.
     * @return Archivo temporal con el contenido del Uri.
     */

    private fun getFileFromUri(context: Context, uri: Uri): Pair<File, String> {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        // Nombre del archivo con o sin extensión, según disponibilidad
        val fileName = if (extension != null) {
            "temp_image_${System.currentTimeMillis()}.$extension"
        } else {
            "temp_image_${System.currentTimeMillis()}"
        }

        val file = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return Pair(file, mimeType ?: "image/*")
    }


    /**
     * Validación completa y envío del formulario.
     */
    fun createRoute(){
        // Validar campos del formulario

        val agencyId = _agencyId.value
        val agencyIdError = validateAgencyId(agencyId)

        val routeName = _routeName.value
        val routeNameError = validateRouteName(routeName)

        val startLocation = _startLocation.value
        val startLocationError = validateStartLocation(startLocation)

        val endLocation = _endLocation.value
        val endLocationError = validateEndLocation(endLocation)

        val unitsImages = _imageUris.value
        val unitsImagesError = validateUnitsImages(unitsImages)

        val unitsId = _unitsId.value

        // Validar horarios de inicio y cierre

        val startSchedule = startSchedule.value
        val endSchedule = endSchedule.value
        val startScheduleErrors = emptyMap<DayOfWeek, String?>().toMutableMap()
        val endScheduleErrors = emptyMap<DayOfWeek, String?>().toMutableMap()

        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

        for(day in days){
            // Añadir item al map solo si hay errores
            validateStartSchedule(day, startSchedule)?.apply {
                startScheduleErrors[day] = this
            }
            validateEndSchedule(day, endSchedule)?.apply {
                endScheduleErrors[day] = this
            }

        }

        val hasErrors = agencyIdError != null || routeNameError != null || startLocationError != null
                || endLocationError != null || unitsImagesError != null
                || (startScheduleErrors.isNotEmpty() && startScheduleErrors.values.any { it != null })
                || (endScheduleErrors.isNotEmpty() && endScheduleErrors.values.any { it != null })

        if(hasErrors){

            _formState.value = CreateRouteFormState.FieldError(
                agencyId = agencyIdError,
                name = routeNameError,
                startLocation = startLocationError,
                endLocation = endLocationError,
                unitsImages = unitsImagesError,
                startSchedule = startScheduleErrors,
                endSchedule = endScheduleErrors
            )
        }else{

            // No hay errores, realizar la llamada al repositorio.
            _formState.value = CreateRouteFormState.Loading

            viewModelScope.launch {
                val result = routeRepository.createRoute(
                    agencyId = agencyId,
                    routeName = routeName,
                    startLocation = startLocation as LatLng,
                    endLocation = endLocation as LatLng,
                    imageFiles = unitsImages.map { uri -> getFileFromUri(context, uri) },
                    startSchedule = startSchedule,
                    endSchedule = endSchedule,
                    unitsId = unitsId
                )

                when(result){
                    is Resource.Error -> {
                        val error = result.message
                        _formState.value = CreateRouteFormState.Error(error ?: "")
                    }
                    is Resource.Success -> {
                        val route = result.data
                        _formState.value = CreateRouteFormState.Success(route)
                    }
                }
            }
        }

    }


}