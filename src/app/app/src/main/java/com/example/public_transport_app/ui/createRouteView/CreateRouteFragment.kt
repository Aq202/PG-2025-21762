package com.example.public_transport_app.ui.createRouteView

import android.content.Context
import com.example.public_transport_app.ui.shared.adapters.ImageGalleryAdapter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.databinding.FragmentCreateRouteBinding
import com.example.public_transport_app.ui.shared.loadingDialog.LoadingDialog
import com.example.public_transport_app.ui.shared.session.SessionNavigationHelper
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import com.example.public_transport_app.utils.maxUnitImages
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CreateRouteFragment : BaseToolbarFragment() {

    private var _contentBinding: FragmentCreateRouteBinding? = null
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val createRouteViewModel: CreateRouteViewModel by viewModels()

    // Adapter para items de galería de imágenes
    private lateinit var imageGalleryAdapter: ImageGalleryAdapter

    // Loading dialog
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)

        // Inflate el contenido dentro del contenedor del BaseToolbarFragment
        _contentBinding = FragmentCreateRouteBinding.inflate(inflater, binding.contentContainer, true)
        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.create_route_title)

        // Navegar a home si ya no se cuenta con acceso de administrador de agencia
        SessionNavigationHelper.observeSessionAndNavigateIfNoAgencyAdminAccess(
            viewLifecycleOwner,
            sessionViewModel.sessionStateFlow,
            findNavController()
        )

        // Obtener agencias que se van a deplegar en dropdown
        createRouteViewModel.getAgencies()

        // Inicializar mapas con el savedInstanceState correcto
        _contentBinding?.mapViewCreateRouteFragmentStart?.onCreate(savedInstanceState)
        _contentBinding?.mapViewCreateRouteFragmentEnd?.onCreate(savedInstanceState)

        setObservables()
        setListeners()

        _contentBinding?.recyclerViewCreateRouteFragmentImagesGallery?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    private fun openLoadingDialog(){
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog()
            loadingDialog?.show(parentFragmentManager, "loadingDialog")
        }
    }

    private fun closeLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun setListeners(){
        setUpOriginLocationListeners()
        setUpEndLocationListeners()
        setUpRouteNameListener()
        setUpImageGalleryListeners()
        setUpStartScheduleListeners()
        setUpEndScheduleListeners()
        setUpDayOfWorkSwitchListeners()
        setUpAddUnitIdListeners()
        setUpCreateRouteButtonListener()
    }

    /**
     * Añadir el evento click que abre el fragment para seleccionar la ubicación de inicio e
     * inicializa el evento que escucha el resultado de dicho fragment.
     */
    private fun setUpOriginLocationListeners() {
        val requestKey = "start_location"
        // Listener para escuchar click en boton de seleccionar ubicación de inicio
        _contentBinding!!.buttonCreateRouteFragmentSelectStartPoint.setOnClickListener {
            // Navegar al fragmento de selección de ubicación de inicio
            val action = CreateRouteFragmentDirections
                .actionCreateRouteFragmentToSelectLocationMapFragment(requestKey)
            findNavController().navigate(action)
        }

        // Escuchar la respuesta del fragment con la ubicación seleccionada de inicio
        parentFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { _, bundle ->
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")

            val location = LatLng(lat, lng)
            createRouteViewModel.setStartLocation(location)
        }
    }

    /**
     * Añadir el evento click que abre el fragment para seleccionar la ubicación de destino e
     * inicializa el evento que escucha el resultado de dicho fragment.
     */
    private fun setUpEndLocationListeners() {
        val requestKey = "destination_location"

        // Listener para escuchar click en boton de seleccionar ubicación de destino
        _contentBinding!!.buttonCreateRouteFragmentSelectEndPoint.setOnClickListener {
            // Navegar al fragmento de selección de ubicación de destino
            val action = CreateRouteFragmentDirections
                .actionCreateRouteFragmentToSelectLocationMapFragment(requestKey)
            findNavController().navigate(action)
        }

        // Escuchar la respuesta del fragment con la ubicación seleccionada de destino
        parentFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { _, bundle ->
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")
            val location = LatLng(lat, lng)
            createRouteViewModel.setEndLocation(location)
        }
    }

    private fun setUpImageGalleryListeners(){

        val pickMultipleMedia = registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxUnitImages)
        )  { uris ->
            if (uris.isNotEmpty()) {

                val mutableList = mutableListOf<Uri>()
                mutableList.addAll(uris)

                // Guardar imageens seleccionadas en el estado
                createRouteViewModel.setUnitsImages(mutableList)

            }
        }

        _contentBinding?.buttonCreateRouteFragmentSelectImages?.setOnClickListener {
            // Abrir image picker
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

            // Resetear errores previos (si existen)
            createRouteViewModel.clearUnitsImagesError()
        }
    }

    private fun setUpStartScheduleListeners(){
        val startScheduleEditTexts = mapOf(
            DayOfWeek.MONDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleMonday,
            DayOfWeek.TUESDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleTuesday,
            DayOfWeek.WEDNESDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleWednesday,
            DayOfWeek.THURSDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleThursday,
            DayOfWeek.FRIDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleFriday,
            DayOfWeek.SATURDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleSaturday,
            DayOfWeek.SUNDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleSunday,
        )

        for ((dayOfWeek, editText) in startScheduleEditTexts) {
            editText?.let {

                // Agregar en cada input de inicio de hora un click event listener que muestre un
                // time picker para seleccionar y posteriormente guardar la hora
                editText.setOnClickListener{
                    val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Seleccionar hora")
                        .build()

                    picker.addOnPositiveButtonClickListener {
                        val time = LocalTime.of(picker.hour, picker.minute) // Hora seleccionada
                        createRouteViewModel.setStartSchedule(dayOfWeek, time) // Guardar en view model

                        createRouteViewModel.clearStartScheduleError(dayOfWeek)
                    }
                    picker.show(parentFragmentManager, "timePicker")

                }
            }

        }
    }

    private fun setUpEndScheduleListeners(){
        val endScheduleEditTexts = mapOf(
            DayOfWeek.MONDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleMonday,
            DayOfWeek.TUESDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleTuesday,
            DayOfWeek.WEDNESDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleWednesday,
            DayOfWeek.THURSDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleThursday,
            DayOfWeek.FRIDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleFriday,
            DayOfWeek.SATURDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleSaturday,
            DayOfWeek.SUNDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleSunday,
        )

        for ((dayOfWeek, editText) in endScheduleEditTexts) {
            editText?.let {

                // Agregar en cada input de hora de cierre un click event listener que muestre un
                // time picker para seleccionar y posteriormente guardar la hora
                editText.setOnClickListener{
                    val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Seleccionar hora")
                        .build()

                    picker.addOnPositiveButtonClickListener {
                        val closeTime = LocalTime.of(picker.hour, picker.minute) // Hora seleccionada
                        createRouteViewModel.setEndSchedule(dayOfWeek, closeTime) // Guardar en view model

                        createRouteViewModel.clearEndScheduleError(dayOfWeek)
                    }
                    picker.show(parentFragmentManager, "timePicker")

                }
            }

        }
    }

    private fun setUpDayOfWorkSwitchListeners(){
        val dayOfWorkSwitches = mapOf(
            DayOfWeek.MONDAY to _contentBinding?.switchCreateRouteFragmentScheduleMonday,
            DayOfWeek.TUESDAY to _contentBinding?.switchCreateRouteFragmentScheduleTuesday,
            DayOfWeek.WEDNESDAY to _contentBinding?.switchCreateRouteFragmentScheduleWednesday,
            DayOfWeek.THURSDAY to _contentBinding?.switchCreateRouteFragmentScheduleThursday,
            DayOfWeek.FRIDAY to _contentBinding?.switchCreateRouteFragmentScheduleFriday,
            DayOfWeek.SATURDAY to _contentBinding?.switchCreateRouteFragmentScheduleSaturday,
            DayOfWeek.SUNDAY to _contentBinding?.switchCreateRouteFragmentScheduleSunday,
        )

        for((dayOfWeek, switch) in dayOfWorkSwitches.entries){

            // Evento para cuando cambie el valor del switch
            switch?.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    // Dia habilitado
                    createRouteViewModel.enableWorkDay(dayOfWeek)

                }else{
                    // Dia deshabilitado
                    createRouteViewModel.disableWorkDay(dayOfWeek)
                }

                // Limpiar errores para el dia correspondiente (si existen)
                createRouteViewModel.clearStartScheduleError(dayOfWeek)
                createRouteViewModel.clearEndScheduleError(dayOfWeek)
            }

        }
    }

    private fun setUpAddUnitIdListeners(){
        // Click listener para botón de añadir ID de unidad
        _contentBinding?.buttonCreateRouteFragmentAddUnit?.setOnClickListener{
            val textInputLayout = _contentBinding?.textInputCreateRouteFragmentAddUnits
            val editText = textInputLayout?.editText
            val unitIdValue = editText?.text?.toString()?.trim()

            if(!unitIdValue.isNullOrEmpty()){
                createRouteViewModel.addUnitId(unitIdValue)
            }

            // Quitar focus
            editText?.clearFocus()

            // Ocultar teclado
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText?.windowToken, 0)
        }
    }

    private fun setUpCreateRouteButtonListener(){
        // Click listener para enviar formulario
        _contentBinding?.buttonCreateRouteFragmentCreateRoute?.setOnClickListener {

            createRouteViewModel.createRoute()
        }
    }


    /**
     * Inicializar observables de stateFlows de viewmodel
     */
    private fun setObservables(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch{
                    observeRouteNameChanges()
                }
                launch {
                    observeFormStateChanges()
                }
                launch {
                    observeStartLocationChanges()
                }
                launch {
                    observeEndLocationChanges()
                }
                launch {
                    observeUnitImagesChanges()
                }
                launch {
                    observeStartScheduleChanges()
                }
                launch {
                    observeEndScheduleChanges()
                }
                launch {
                    observeUnitsIdChanges()
                }
            }
        }
    }

    private suspend fun observeRouteNameChanges(){
        // Rellenar valor de ruta
        createRouteViewModel.routeName.collectLatest { routeName ->
            // Asignar texto al EditText sólo si es diferente (para evitar ciclos infinitos)
            val editText = _contentBinding?.textInputCreateRouteFragmentRouteName?.editText
            if (editText != null && editText.text.toString() != routeName) {
                editText.setText(routeName)
            }
        }
    }

    private suspend fun observeStartLocationChanges(){
        // Mostrare en el mapa lite la ubicación seleccionada
        createRouteViewModel.startLocation.collectLatest { startLocation ->
            if(startLocation != null){
                _contentBinding?.mapViewCreateRouteFragmentStart?.visibility = VISIBLE
                _contentBinding?.mapViewCreateRouteFragmentStart?.getMapAsync { googleMap ->
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(startLocation))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
                }
            }
        }
    }

    private suspend fun observeEndLocationChanges(){
        // Mostrar en el mapa lite la ubicación seleccionada
        createRouteViewModel.endLocation.collectLatest { endLocation ->
            if(endLocation != null){
                _contentBinding?.mapViewCreateRouteFragmentEnd?.visibility = VISIBLE
                _contentBinding?.mapViewCreateRouteFragmentEnd?.getMapAsync { googleMap ->
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(endLocation))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation, 15f))
                }
            }
        }
    }

    private suspend fun observeUnitImagesChanges(){
        val imagesGalleryRecyclerView = _contentBinding?.recyclerViewCreateRouteFragmentImagesGallery

        // Image gallery adapter
        imageGalleryAdapter = ImageGalleryAdapter { removedUri ->
            // On Uri deleted
            createRouteViewModel.removeUnitImage(removedUri)
        }
        imagesGalleryRecyclerView?.adapter = imageGalleryAdapter


        createRouteViewModel.imageUris.collectLatest { imageUris ->
            println("URI: observe executed")

            imageUris.apply {
                // Show gallery items in recyclerview
                imageGalleryAdapter.replaceAll(imageUris)

                if(imageUris.isNotEmpty()){
                    // Si hay alguna imagen que fue seleccionada, mostrar recycler
                    imagesGalleryRecyclerView?.visibility = VISIBLE
                }else{
                    imagesGalleryRecyclerView?.visibility = GONE
                }
            }
        }
    }

    private suspend fun observeStartScheduleChanges(){
        val scheduleEditTexts = mapOf(
            DayOfWeek.MONDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleMonday,
            DayOfWeek.TUESDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleTuesday,
            DayOfWeek.WEDNESDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleWednesday,
            DayOfWeek.THURSDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleThursday,
            DayOfWeek.FRIDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleFriday,
            DayOfWeek.SATURDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleSaturday,
            DayOfWeek.SUNDAY to _contentBinding?.editTextCreateRouteFragmentStartScheduleSunday,
        )

        createRouteViewModel.startSchedule.collectLatest { scheduleMap ->
            for((dayOfWeek, editText) in scheduleEditTexts.entries){
                val time = scheduleMap[dayOfWeek]
                if(!scheduleMap.containsKey(dayOfWeek)){
                    // Si no contiene el día, el usuario no ha ingresado un valor
                    editText?.setText(getString(R.string.empty_hour))
                    editText?.isEnabled = true
                } else if(time != null){
                    // Si contine la hora, colocarla
                    editText?.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    editText?.isEnabled = true
                }else{
                    // Si es null, el usuario seleccionó que ese día no se trabaja
                    editText?.setText("N/A")
                    editText?.isEnabled = false // Deshabilitado
                }
            }
        }
    }

    private suspend fun observeEndScheduleChanges(){
        val scheduleEditTexts = mapOf(
            DayOfWeek.MONDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleMonday,
            DayOfWeek.TUESDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleTuesday,
            DayOfWeek.WEDNESDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleWednesday,
            DayOfWeek.THURSDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleThursday,
            DayOfWeek.FRIDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleFriday,
            DayOfWeek.SATURDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleSaturday,
            DayOfWeek.SUNDAY to _contentBinding?.editTextCreateRouteFragmentEndScheduleSunday,
        )

        createRouteViewModel.endSchedule.collectLatest { scheduleMap ->
            for((dayOfWeek, editText) in scheduleEditTexts.entries){
                val time = scheduleMap[dayOfWeek]
                if(!scheduleMap.containsKey(dayOfWeek)){
                    // Si no contiene el día, el usuario no ha ingresado un valor
                    editText?.setText(getString(R.string.empty_hour))
                    editText?.isEnabled = true
                } else if(time != null){
                    // Si contine la hora, colocarla
                    editText?.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    editText?.isEnabled = true
                }else{
                    // Si es null, el usuario seleccionó que ese día no se trabaja
                    editText?.setText("N/A")
                    editText?.isEnabled = false // Deshabilitado
                }
            }
        }
    }

    private suspend fun observeUnitsIdChanges() {
        createRouteViewModel.unitsId.collectLatest { unitsId ->
            val chipGroup = _contentBinding?.chipGroupCreateRouteFragmentUnitsId ?: return@collectLatest

            // Limpiar chips existentes
            chipGroup.removeAllViews()

            // Añadir chips nuevos sin duplicados
            for (unitId in unitsId.distinct()) {
                val chip = Chip(chipGroup.context).apply {
                    text = unitId
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        chipGroup.removeView(this)
                        createRouteViewModel.removeUnitId(unitId) // remmover unitId
                    }
                }
                chipGroup.addView(chip)
            }
        }
    }


    private suspend fun observeFormStateChanges(){
        createRouteViewModel.formState.collectLatest { formState ->
            when(formState){
                CreateRouteFormState.Default -> {

                }
                is CreateRouteFormState.FieldError -> {
                    addFieldErrorsToUI(formState)
                }


                CreateRouteFormState.LoadingAgencies -> {
                    openLoadingDialog()
                }

                is CreateRouteFormState.AgenciesError -> {
                    closeLoadingDialog()

                    // Mostrar error al obtener agencias
                    Toast.makeText(context,
                        getString(R.string.get_agencies_error), Toast.LENGTH_SHORT).show()
                }
                is CreateRouteFormState.AgenciesLoaded -> {
                    closeLoadingDialog()

                    setUpAgenciesDropdown(formState.agencies)
                }

                is CreateRouteFormState.Error -> {

                    closeLoadingDialog()
                    // Mostrar mensaje de error
                    val error = formState.error
                    val generalError = getString(R.string.general_error)
                    Toast.makeText(context,
                        "$generalError $error", Toast.LENGTH_SHORT).show()
                }
                CreateRouteFormState.Loading -> {
                    openLoadingDialog()
                }

                is CreateRouteFormState.Success -> {
                    closeLoadingDialog()

                    // Navegar al fragment de detalles de la ruta
                    val routeId = formState.route.id
                    val action = CreateRouteFragmentDirections.actionCreateRouteFragmentToRouteViewFragment(
                        routeId = routeId
                    )

                    requireView().findNavController().navigate(action)

                }
            }
        }
    }

    private fun addFieldErrorsToUI(formState: CreateRouteFormState.FieldError) {

        _contentBinding?.let { ui ->

            ui.textInputCreateRouteFragmentRouteName.error = formState.name
            ui.autoCompleteViewCreateRouteFragmentAgencies.error = formState.agencyId

            ui.buttonCreateRouteFragmentSelectStartPoint.error = formState.startLocation
            ui.textViewCreateRouteFragmentStartPointError.apply {
                text = formState.startLocation
                visibility = if (formState.startLocation != null) View.VISIBLE else View.GONE
            }

            ui.buttonCreateRouteFragmentSelectEndPoint.error = formState.endLocation
            ui.textViewCreateRouteFragmentEndPointError.apply {
                text = formState.endLocation
                visibility = if (formState.endLocation != null) View.VISIBLE else View.GONE
            }

            ui.buttonCreateRouteFragmentSelectImages.error = formState.unitsImages
            ui.textViewCreateRouteFragmentImageError.apply {
                text = formState.unitsImages
                visibility = if (formState.unitsImages != null) View.VISIBLE else View.GONE
            }

            ui.textInputCreateRouteFragmentAddUnits.error = formState.unitsId

            val startScheduleInputs = mapOf(
                DayOfWeek.MONDAY to ui.editTextCreateRouteFragmentStartScheduleMonday,
                DayOfWeek.TUESDAY to ui.editTextCreateRouteFragmentStartScheduleTuesday,
                DayOfWeek.WEDNESDAY to ui.editTextCreateRouteFragmentStartScheduleWednesday,
                DayOfWeek.THURSDAY to ui.editTextCreateRouteFragmentStartScheduleThursday,
                DayOfWeek.FRIDAY to ui.editTextCreateRouteFragmentStartScheduleFriday,
                DayOfWeek.SATURDAY to ui.editTextCreateRouteFragmentStartScheduleSaturday,
                DayOfWeek.SUNDAY to ui.editTextCreateRouteFragmentStartScheduleSunday
            )

            val endScheduleInputs = mapOf(
                DayOfWeek.MONDAY to ui.editTextCreateRouteFragmentEndScheduleMonday,
                DayOfWeek.TUESDAY to ui.editTextCreateRouteFragmentEndScheduleTuesday,
                DayOfWeek.WEDNESDAY to ui.editTextCreateRouteFragmentEndScheduleWednesday,
                DayOfWeek.THURSDAY to ui.editTextCreateRouteFragmentEndScheduleThursday,
                DayOfWeek.FRIDAY to ui.editTextCreateRouteFragmentEndScheduleFriday,
                DayOfWeek.SATURDAY to ui.editTextCreateRouteFragmentEndScheduleSaturday,
                DayOfWeek.SUNDAY to ui.editTextCreateRouteFragmentEndScheduleSunday
            )

            var hasScheduleError = false

            // Limpiar o asignar errores para cada día
            for (day in startScheduleInputs.keys) {

                val startErr = formState.startSchedule[day]
                startScheduleInputs[day]?.error = startErr
                if (startErr != null) hasScheduleError = true

                val endErr = formState.endSchedule[day]
                endScheduleInputs[day]?.error = endErr
                if (endErr != null) hasScheduleError = true
            }

            // Mostrar u ocultar texto de error general
            ui.textViewCreateRouteFragmentScheduleError.apply {
                visibility = if (hasScheduleError) View.VISIBLE else View.GONE
            }
        }
    }



    private fun setUpRouteNameListener(){
        val editText = _contentBinding?.textInputCreateRouteFragmentRouteName?.editText
        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                createRouteViewModel.clearRouteNameError()
            } else {
                val text = editText.text.toString()
                createRouteViewModel.setRouteName(text)
            }
        }
    }

    private fun setUpAgenciesDropdown(agencies:List<Agency>){

        val agenciesDropdownInput = _contentBinding!!.autoCompleteViewCreateRouteFragmentAgencies

        // Inicializar el spinner con los nombres
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            agencies.map { it.name}
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        agenciesDropdownInput.setAdapter(adapter)

        // Establecer el listener para escuchar el cambio en la opción seleccionada
        agenciesDropdownInput.setOnItemClickListener{_, _, position, _ ->

            // Limpiar errores previos al hacer focus
            createRouteViewModel.clearAgencyIdError()

            val selectedAgency = agencies[position]
            createRouteViewModel.setAgencyId(selectedAgency.id)

        }

    }

    override fun onStart() {
        super.onStart()
        _contentBinding?.mapViewCreateRouteFragmentStart?.onStart()
        _contentBinding?.mapViewCreateRouteFragmentEnd?.onStart()
    }

    override fun onResume() {
        super.onResume()
        _contentBinding?.mapViewCreateRouteFragmentStart?.onResume()
        _contentBinding?.mapViewCreateRouteFragmentEnd?.onResume()
    }

    override fun onPause() {
        super.onPause()
        _contentBinding?.mapViewCreateRouteFragmentStart?.onPause()
        _contentBinding?.mapViewCreateRouteFragmentEnd?.onPause()
    }

    override fun onStop() {
        super.onStop()
        _contentBinding?.mapViewCreateRouteFragmentStart?.onStop()
        _contentBinding?.mapViewCreateRouteFragmentEnd?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _contentBinding?.mapViewCreateRouteFragmentStart?.onDestroy()
        _contentBinding?.mapViewCreateRouteFragmentEnd?.onDestroy()
    }



}
