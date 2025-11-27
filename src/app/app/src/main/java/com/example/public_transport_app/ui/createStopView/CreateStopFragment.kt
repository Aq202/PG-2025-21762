package com.example.public_transport_app.ui.createStopView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.databinding.FragmentCreateStopBinding
import com.example.public_transport_app.ui.infoDialog.ErrorDialog
import com.example.public_transport_app.ui.infoDialog.SuccessDialog
import com.example.public_transport_app.ui.shared.loadingDialog.LoadingDialog
import com.example.public_transport_app.ui.shared.session.SessionNavigationHelper
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class CreateStopFragment : BaseToolbarFragment() {

    private var _contentBinding: FragmentCreateStopBinding? = null
    private val viewModel: CreateStopViewModel by viewModels()

    private val sessionViewModel: SessionViewModel by activityViewModels()

    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)
        _contentBinding = FragmentCreateStopBinding.inflate(inflater, binding.contentContainer, true)
        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.create_stop)

        // Navegar a home si ya no se cuenta con acceso de administrador de agencia
        SessionNavigationHelper.observeSessionAndNavigateIfNoAgencyAdminAccess(
            viewLifecycleOwner,
            sessionViewModel.sessionStateFlow,
            findNavController()
        )

        // Inicializar ciclo de vida del MapView
        _contentBinding?.mapViewCreateStopLocation?.onCreate(savedInstanceState)

        viewModel.getAgencies()
        setListeners()
        setObservers()
    }

    private fun setListeners(){
        setStopNameListener()
        setLocationListener()
        setSubmitButtonListener()
    }

    private fun setStopNameListener(){
        _contentBinding?.textInputCreateStopStopName?.editText?.let{ editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    viewModel.clearStopNameError()
                }
            }

            editText.doOnTextChanged { text, _, _, _ ->
                viewModel.setStopName(text.toString())
            }
        }
    }

    private fun setLocationListener(){
        // Seleccionar ubicación
        val requestKey = "stop_location"
        _contentBinding?.buttonCreateStopSelectLocation?.setOnClickListener {
            val action = CreateStopFragmentDirections
                .actionCreateStopFragmentToSelectLocationMapFragment(requestKey)
            findNavController().navigate(action)
        }

        parentFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { _, bundle ->
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")
            viewModel.setLocation(LatLng(lat, lng))
        }

    }

    private fun setSubmitButtonListener(){
        _contentBinding?.buttonCreateStopSubmit?.setOnClickListener {
            viewModel.createStop()
        }
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    observeFormStateChanges()
                }

                launch {
                    observeStopNameChanges()
                }

                launch {
                    observeLocationChanges()
                }
            }
        }
    }

    private suspend fun observeStopNameChanges(){
        viewModel.stopName.collectLatest { stopName ->
            // Asignar texto al EditText sólo si es diferente (para evitar ciclos infinitos)
            val editText = _contentBinding?.textInputCreateStopStopName?.editText
            if (editText != null && editText.text.toString() != stopName) {
                editText.setText(stopName)
            }
        }
    }

    private suspend fun observeLocationChanges(){
        // Mostrare en el mapa lite la ubicación seleccionada
        viewModel.location.collectLatest { location ->
            if(location != null){
                _contentBinding?.mapViewCreateStopLocation?.apply {
                    this.visibility = VISIBLE
                    this.getMapAsync { googleMap ->
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(location))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    }
                }
            }
        }
    }

    private suspend fun observeFormStateChanges() {
        viewModel.formState.collectLatest { state ->
            when (state) {
                CreateStopFormState.Default -> Unit
                CreateStopFormState.Loading,
                CreateStopFormState.LoadingAgencies -> openLoadingDialog()

                is CreateStopFormState.AgenciesLoaded -> {
                    closeLoadingDialog()
                    setUpAgenciesDropdown(state.agencies)
                }
                is CreateStopFormState.FieldError -> {
                    // Añadir error para todos los inputs (si poseen error)
                    setFormErrorsInUi(state)
                }
                is CreateStopFormState.Error -> {
                    closeLoadingDialog()
                    ErrorDialog(requireContext())
                        .show(
                            title = getString(R.string.error_has_occurred),
                            message = state.error
                        )
                }
                is CreateStopFormState.Success -> {
                    closeLoadingDialog()
                    SuccessDialog(requireContext())
                        .show(
                            title = getString(R.string.alert_title_stop_created_succesfully),
                            message = getString(
                                R.string.alert_content_stop_created_succesfully,
                                state.stop.name
                            ),
                            ){

                            // Al aceptar la alerta, navegar hacia atrás
                            findNavController().navigateUp()
                        }
                }
            }
        }
    }

    private fun setFormErrorsInUi(errors: CreateStopFormState.FieldError){
        _contentBinding?.let{
            it.textInputCreateStopStopName.error = errors.name
            it.autoCompleteViewCreateStopAgencies.error = errors.agencyId
            it.buttonCreateStopSelectLocation.error = errors.location
        }
    }

    private fun setUpAgenciesDropdown(agencies: List<Agency>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            agencies.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _contentBinding?.autoCompleteViewCreateStopAgencies?.setAdapter(adapter)

        _contentBinding?.autoCompleteViewCreateStopAgencies?.setOnItemClickListener { _, _, position, _ ->

            // Limpiar errores previos al hacer focus
            viewModel.clearAgencyIdError()

            viewModel.setAgencyId(agencies[position].id)
        }
    }

    private fun openLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog()
            loadingDialog?.show(parentFragmentManager, "loadingDialog")
        }
    }

    private fun closeLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}