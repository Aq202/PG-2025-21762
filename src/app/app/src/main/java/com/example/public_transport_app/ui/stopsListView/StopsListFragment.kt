package com.example.public_transport_app.ui.stopsListView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.databinding.FragmentStopsListBinding
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
class StopsListFragment : BaseToolbarFragment() {

    private var _contentBinding: FragmentStopsListBinding? = null
    private val viewModel: StopsListViewModel by viewModels()

    private val sessionViewModel: SessionViewModel by activityViewModels()

    private var loadingDialog: LoadingDialog? = null

    private lateinit var stopsAdapter: StopsListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)
        _contentBinding = FragmentStopsListBinding.inflate(inflater, binding.contentContainer, true)
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

        // Obtener las agencias del usuario
        viewModel.getAgencies()

        // Configuración del RecyclerView
        val recyclerView = _contentBinding!!.recyclerViewStopsListStops
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            isNestedScrollingEnabled = false

            // Asignar adapter
            stopsAdapter = StopsListAdapter()
            this.adapter = stopsAdapter
        }


        setObservables()
        setListeners()
    }

    private fun setListeners(){
        _contentBinding!!.fabStopsListNewStop.setOnClickListener {
            findNavController().navigate(R.id.action_stopsListFragment_to_createStopFragment)
        }
    }
    private fun setObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        StopsListState.LoadingStops,
                            StopsListState.LoadingAgencies-> openLoadingDialog()
                        is StopsListState.Error -> {
                            closeLoadingDialog()

                            requireContext().showErrorDialog(
                                title = getString(R.string.error_has_occurred),
                                message = state.error
                            )
                        }
                        is StopsListState.StopsLoaded -> {
                            closeLoadingDialog()
                            stopsAdapter.setStops(state.stops)
                        }

                        is StopsListState.AgenciesError -> {
                            closeLoadingDialog()
                            requireContext().showErrorDialog(
                                title = getString(R.string.error_has_occurred),
                                message = state.error
                            )
                        }
                        is StopsListState.AgenciesLoaded -> {
                            closeLoadingDialog()
                            setUpAgenciesSpinner(state.agencies)
                        }
                    }
                }
            }
        }
    }

    private fun setUpAgenciesSpinner(agencies:List<Agency>){

        val spinner = _contentBinding!!.spinnerStopsListAgencies

        // Inicializar el spinner con los nombres
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            agencies.map { it.name}
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Establecer el listener para escuchar el cambio en la opción seleccionada
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener  {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedAgency = agencies[position]
                val agencyId = selectedAgency.id

                viewModel.getRouteStops(agencyId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
