package com.example.public_transport_app.ui.selectRouteStopsView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.databinding.FragmentSelectRouteStopsBinding
import com.example.public_transport_app.ui.infoDialog.showErrorDialog
import com.example.public_transport_app.ui.shared.loadingDialog.LoadingDialog
import com.example.public_transport_app.ui.shared.session.SessionNavigationHelper
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SelectRouteStopsFragment : BaseToolbarFragment() {

    private var _contentBinding: FragmentSelectRouteStopsBinding? = null
    private val viewModel: SelectRouteStopsViewModel by viewModels()

    private val sessionViewModel: SessionViewModel by activityViewModels()

    private var loadingDialog: LoadingDialog? = null

    private lateinit var stopsAdapter: SelectRouteStopsAdapter

    private val args: SelectRouteStopsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)
        _contentBinding = FragmentSelectRouteStopsBinding.inflate(inflater, binding.contentContainer, true)
        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.select_stop)

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
            stopsAdapter = SelectRouteStopsAdapter(::handleAddClick)
            this.adapter = stopsAdapter
        }

        viewModel.getRouteStops(args.agencyId)

        setObservables()
    }

    private fun setObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        SelectRouteStopsState.LoadingStops -> openLoadingDialog()
                        is SelectRouteStopsState.Error -> {
                            closeLoadingDialog()

                            requireContext().showErrorDialog(
                                title = getString(R.string.error_has_occurred),
                                message = state.error
                            )
                        }
                        is SelectRouteStopsState.StopsLoaded -> {
                            closeLoadingDialog()
                            setStopsInAdapter(state.stops)
                        }

                    }
                }
            }
        }
    }

    private fun setStopsInAdapter(stops: List<Stop>) {
        val stopsToIgnoreSet = args.stopsToIgnore?.toSet() ?: emptySet()
        val stopsToShow = stops.filter { it.id !in stopsToIgnoreSet }
        stopsAdapter.setStops(stopsToShow)
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

    private fun handleAddClick(stop: Stop) {
        val result = Bundle().apply {
            putString("stopId", stop.id)
            putString("stopName", stop.name)
            putDouble("stopLat", stop.location.latitude)
            putDouble("stopLng", stop.location.longitude)
            putString("agencyId", stop.agency.id)
            putString("agencyName", stop.agency.name)
        }

        setFragmentResult(args.eventKey, result)
        findNavController().popBackStack()
    }
}
