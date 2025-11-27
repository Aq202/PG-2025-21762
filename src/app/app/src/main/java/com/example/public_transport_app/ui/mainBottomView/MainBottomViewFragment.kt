package com.example.public_transport_app.ui.mainBottomView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.EmbeddedRoute
import com.example.public_transport_app.data.entity.RouteAndStop
import com.example.public_transport_app.databinding.FragmentMainBottomViewBinding
import com.example.public_transport_app.ui.homePage.BottomSheetViewModel
import com.example.public_transport_app.ui.infoDialog.ErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class MainBottomViewFragment : Fragment() {

    private lateinit var binding: FragmentMainBottomViewBinding
    private val bottomSheetViewModel: BottomSheetViewModel by viewModels(
        ownerProducer = { requireParentFragment().requireParentFragment() }
    )

    private val viewModel: MainBottomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBottomViewBinding.inflate(inflater, container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    observeUserLocation()
                }
                launch{
                    observeClosestStopsByRoute()
                }
            }
        }
    }

    private suspend fun observeUserLocation(){
        bottomSheetViewModel.userLocation.collectLatest { userLocation ->
            if(userLocation != null){
                viewModel.getClosestStopByRoute(userLocation)
            }
        }
    }

    private suspend fun observeClosestStopsByRoute(){
        viewModel.closestStopByRoute.collectLatest { state ->
            when(state){
                is NearbyRoutesState.Error -> {
                    ErrorDialog(requireContext())
                        .show(
                            getString(R.string.error_has_occurred),
                            state.error
                        )
                }
                NearbyRoutesState.Loading -> { }
                is NearbyRoutesState.Success -> {
                    // Inicializar recyclerview con listado de rutas
                    val adapter = NearbyRoutesAdapter(
                        requireContext(),
                        state.nearbyRoutes,
                        ::onRouteItemClick
                    )
                    binding.recyclerViewMainBottomViewRoutes.adapter = adapter
                    binding.recyclerViewMainBottomViewRoutes.layoutManager =
                        LinearLayoutManager(context)
                }
            }
        }
    }

    private fun onRouteItemClick(routeAndStop: RouteAndStop) {
        // Enviar evento para que se seleccione la ruta en el mapa
        bottomSheetViewModel.selectRoute(routeAndStop)
    }

}