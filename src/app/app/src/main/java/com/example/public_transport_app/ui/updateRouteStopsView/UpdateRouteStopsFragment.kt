package com.example.public_transport_app.ui.updateRouteStopsView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.databinding.FragmentUpdateRouteStopsBinding
import com.example.public_transport_app.ui.infoDialog.showErrorDialog
import com.example.public_transport_app.ui.shared.loadingDialog.LoadingDialog
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import com.example.public_transport_app.ui.infoDialog.showSuccessDialog
import com.example.public_transport_app.ui.shared.session.SessionNavigationHelper
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class UpdateRouteStopsFragment : BaseToolbarFragment() {

    private var _contentBinding: FragmentUpdateRouteStopsBinding? = null
    private val viewModel: UpdateRouteStopsViewModel by viewModels()

    private val sessionViewModel: SessionViewModel by activityViewModels()

    private var loadingDialog: LoadingDialog? = null

    private val args: UpdateRouteStopsFragmentArgs by navArgs()

    private lateinit var stopsAdapter: UpdateRouteStopsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)
        _contentBinding = FragmentUpdateRouteStopsBinding.inflate(inflater, binding.contentContainer, true)
        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.stops)

        // Navegar a home si ya no se cuenta con acceso de administrador de agencia
        SessionNavigationHelper.observeSessionAndNavigateIfNoAgencyAdminAccess(
            viewLifecycleOwner,
            sessionViewModel.sessionStateFlow,
            findNavController()
        )

        // Configuración del RecyclerView
        val recyclerView = _contentBinding!!.recyclerViewUpdateRouteStopsStops
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            isNestedScrollingEnabled = false

            // Asignar adapter
            stopsAdapter = UpdateRouteStopsAdapter(::stopRemovedCallback)
            this.adapter = stopsAdapter
        }

        val routeId = args.routeId
        viewModel.getRouteStops(routeId)

        setObservables()
        setListeners()
    }

    private fun setObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        UpdateRouteStopsState.LoadingStops,
                        UpdateRouteStopsState.UpdatingStops-> openLoadingDialog()
                        is UpdateRouteStopsState.Error -> {
                            closeLoadingDialog()

                            requireContext().showErrorDialog(
                                title = getString(R.string.error_has_occurred),
                                message = state.error
                            )
                        }
                        is UpdateRouteStopsState.StopsLoaded -> {
                            closeLoadingDialog()
                            stopsAdapter.setStops(state.stops)
                        }
                        is UpdateRouteStopsState.StopsUpdated -> {
                            closeLoadingDialog()
                            showUpdateSuccessDialog()
                        }

                    }
                }
            }
        }
    }

    private fun showUpdateSuccessDialog(){
        requireContext().showSuccessDialog(
            title = getString(R.string.updated_succesfully),
            message = getString(R.string.route_stops_update_success_message)
        ) {
            // Navegar hacia fragment previo
            findNavController().popBackStack()
        }
    }

    private fun setListeners() {
        _contentBinding!!.buttonUpdateStopsConfirm.setOnClickListener {
            // Actualizar paradas
            viewModel.updateRouteStops(args.routeId, stopsAdapter.getStops())
        }

        setSelectStopListeners()
    }

    private fun setSelectStopListeners(){

        val selectStopEventKey = "select_new_stop"

        _contentBinding!!.fabUpdateRouteStopsSelectStop.setOnClickListener {
            // FAB para añadir parada
            val stopsToIgnore = stopsAdapter.getStops().map { it.id }
            val action = UpdateRouteStopsFragmentDirections.actionUpdateRouteStopsFragmentToSelectRouteStopsFragment(
                eventKey = selectStopEventKey,
                stopsToIgnore = stopsToIgnore.toTypedArray(),
                agencyId = args.agencyId
            )
            findNavController().navigate(action)
        }

        // Escuchar la respuesta del fragment con el id de la parada seleccionada
        parentFragmentManager.setFragmentResultListener(selectStopEventKey, viewLifecycleOwner) { _, bundle ->
            val stop = Stop(
                id = bundle.getString("stopId")!!,
                name = bundle.getString("stopName")!!,
                location = LatLng(bundle.getDouble("stopLat"), bundle.getDouble("stopLng")),
                agency = Agency(bundle.getString("agencyId")!!, bundle.getString("agencyName")!!),
                routes = null
            )
            viewModel.addStop(stop)
        }
    }

    private fun stopRemovedCallback(stop: Stop){
        // Eliminar del view model la parada a la que se le dio click en remover
        viewModel.removeStop(stop.id)
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
