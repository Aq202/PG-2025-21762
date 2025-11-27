package com.example.public_transport_app.ui.routesListView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.databinding.FragmentRoutesListBinding
import com.example.public_transport_app.ui.shared.loadingDialog.LoadingDialog
import com.example.public_transport_app.ui.shared.session.SessionNavigationHelper
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoutesListFragment : BaseToolbarFragment() {

    private var _contentBinding: FragmentRoutesListBinding? = null
    private val viewModel: RoutesListViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()

    // Loading dialog
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)

        // Inflate el contenido dentro del contenedor del BaseToolbarFragment
        _contentBinding = FragmentRoutesListBinding.inflate(inflater, binding.contentContainer, true)

        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.my_routes)

        // Obtener las agencias del usuario
        viewModel.getAgencies()

        // Navegar a home si ya no se cuenta con acceso de conductor
        SessionNavigationHelper.observeSessionAndNavigateIfNoDriverAccess(
            viewLifecycleOwner,
            sessionViewModel.sessionStateFlow,
            findNavController()
        )

        // Configurar recyclerView
        val recyclerView = _contentBinding!!.recyclerViewRoutesFragmentRoutes
        recyclerView.apply{
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)            // Indicando que el tamaño puede cambiar
            isNestedScrollingEnabled = false  // scroll de recycler deshabilitado
        }

        setObservables()
        setCreateRouteButtonClickListener()
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

    private fun setObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    when(state){
                        is RoutesListState.AgenciesError ->{
                            closeLoadingDialog()
                            Toast.makeText(context,
                                getString(R.string.get_agencies_error), Toast.LENGTH_SHORT).show()
                        }
                        is RoutesListState.AgenciesLoaded -> {
                            closeLoadingDialog()
                            setUpAgenciesSpinner(state.agencies)
                        }
                        RoutesListState.LoadingAgencies ->{
                            openLoadingDialog()
                        }
                        RoutesListState.LoadingRoutes -> {
                            openLoadingDialog()
                        }
                        is RoutesListState.RoutesError -> {
                            closeLoadingDialog()
                            val genError = getString(R.string.general_error)
                            Toast.makeText(context, "$genError ${state.error}", Toast.LENGTH_SHORT).show()
                        }
                        is RoutesListState.RoutesLoaded -> {

                            // Cuando se obtiene el listado de rutas

                            closeLoadingDialog() // Cerrar loading dialog

                            val routes = state.routes

                            // Cargar el contenido en el recycler view

                            val recyclerView = _contentBinding!!.recyclerViewRoutesFragmentRoutes
                            recyclerView.adapter = RoutesListAdapter(routes) { selectedRoute ->
                                // Al hacer click en un item, navegar a la página de la ruta

                                val routeId = selectedRoute.id
                                val action = RoutesListFragmentDirections.actionRoutesListFragmentToRouteViewFragment(
                                    routeId
                                )
                                findNavController().navigate(action)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpAgenciesSpinner(agencies:List<Agency>){

        val spinner = _contentBinding!!.spinnerRoutesFragmentAgencies

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

                viewModel.getRoutes(agencyId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun setCreateRouteButtonClickListener(){
        _contentBinding!!.fabRoutesListCreateRoute.setOnClickListener {
            findNavController().navigate(R.id.action_routesListFragment_to_createRouteFragment)
        }
    }

}