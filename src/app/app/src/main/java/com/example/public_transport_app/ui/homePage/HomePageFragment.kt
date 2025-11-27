package com.example.public_transport_app.ui.homePage

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.databinding.FragmentHomePageBinding
import com.example.public_transport_app.ui.mainActivity.MainActivity
import com.example.public_transport_app.ui.shared.permissions.LocationPermissionManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomePageFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentHomePageBinding

    private lateinit var bottomSheetNavController: NavController

    private lateinit var mapController: MapController
    private lateinit var locationPermissionManager: LocationPermissionManager

    private lateinit var bottomSheetController: BottomSheetController

    private lateinit var centerLocationButtonController: CenterLocationButtonController

    private lateinit var userLocationController: UserLocationInMapController

    private lateinit var stopsController: StopsController

    private lateinit var routesController: RoutesController

    private lateinit var nearbyRunsController: NearbyRunsController

    private val mapViewModel: MapViewModel by viewModels()
    private val bottomSheetViewModel: BottomSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePageBinding.inflate(inflater, container,false)

        // Inicializar gestor de permisos
        locationPermissionManager = LocationPermissionManager(requireContext(), this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapController = MapController(requireContext(), layoutInflater)

        // Instanciar controlador para botón de centrado
        centerLocationButtonController = CenterLocationButtonController(
            binding.fabHomePageCenterLocation,
        )

        setupBottomSheet()

        // Controlador para ubicación del usuario
        userLocationController = UserLocationInMapController(
            mapController,
            ::handleUserLocationUpdated
        )

        // Controlador para el manejo de buses en el mapa
        nearbyRunsController = NearbyRunsController(
            requireContext(),
            mapViewModel,
            mapController,
            bottomSheetController,
        )

        // Controlador para las paradas
        stopsController = StopsController(
            mapController,
            mapViewModel,
        )

        // Controlador de rutas (no viajes activos)
        routesController = RoutesController(
            requireContext(),
            mapViewModel,
            mapController,
            bottomSheetController,
            centerLocationButtonController
        )

        initEvents()

        checkPermissionsAndInitMap()

    }

    override fun onMapReady(googleMap: GoogleMap) {

        initMapController(googleMap)
        setMapObservers()
    }

    private fun initEvents(){
        binding.imageButtonHomePageMenu.setOnClickListener{
            (activity as? MainActivity)?.openDrawer()
        }

        // Botón para volver a pedir permisos
        binding.buttonHomePageRequestPermissionButton.setOnClickListener {
            checkPermissionsAndInitMap()
        }

        // Centrar en ubicación de usuario
        binding.fabHomePageCenterLocation.setOnClickListener {
            userLocationController.lastKnownLatLng?.let { latLng ->
                mapController.centerCameraInUserLocation(latLng)

                // Manejo de mostrar/ocultar botón de centrado
                centerLocationButtonController.setIsCameraFollowingUser(true)
            }
        }

    }

    private fun checkPermissionsAndInitMap() {

        // Por defecto ocultar mapa (si hay permisos, se muestra hasta que se centra en ubicación actual)
        binding.layoutHomePageMainContainer.visibility = View.GONE

        locationPermissionManager.requestPreciseLocation(
            rationaleTitle = getString(R.string.location_permission_rational_start_run_title),
            rationaleMessage = getString(R.string.location_permission_rational_start_run_description)
        ) { granted ->
            if (granted) {
                // Ocultar UI alternativa. Mapa se muestra luego al centrar en ubicación actual
                binding.layoutHomePageLocationPermissionOverlay.visibility = View.GONE

                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.fragment_map) as? com.google.android.gms.maps.SupportMapFragment
                mapFragment?.getMapAsync(this)

            } else {
                // Mostrar la UI alternativa
                binding.layoutHomePageLocationPermissionOverlay.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMapController(map: GoogleMap) {

        mapController.setMap(map)

        mapController.clearMap()
        mapController.configureMap()
        mapController.setOnBusClickListener(::setBusOnClickListener)
        mapController.setOnCameraMovedListener(::setOnCameraMovedListener)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Centrar el mapa en la ubicación actual
                userLocationController.initializeCameraPosition(requireContext())

                showMapContainer()

                // Obtener continuamente la ubicación del dispositivo
                userLocationController.startLocationUpdates(requireContext())

            }
        }
    }

    private fun showMapContainer(){
        binding.layoutHomePageMainContainer.visibility = View.VISIBLE
    }

    private fun handleUserLocationUpdated(location: LatLng, firstUpdate: Boolean){
        // Realizar actualización de buses cercanos
        nearbyRunsController.requestToUpdateNearbyRunsIfNeeded(
            location,
        )

        if(firstUpdate){
            // Al recibir primera ubicación, inicializar rutas cercanas
            bottomSheetViewModel.setUserLocation(location)
        }
    }



    private fun setMapObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observar cambio en lista de buses cercanos
                launch{
                    nearbyRunsController.observeNearbyActiveRuns()
                }

                // Observar cambio en prediction de ruta
                launch{
                    observeRoutePredictions()
                }

                // Observar resultado de petición de paradas
                launch{
                    stopsController.observeStops()
                }

                // Observar resultado de recorrido más relevante de ruta seleccionada
                launch{
                    routesController.observeRouteBestRunPoints()
                }

                // Observar eventos de bottom sheet
                launch{
                    observeCloseDetailsBottomSheetEvent()
                }
                launch{
                    observeRouteSelectedBottomSheetEvent()
                }

            }
        }
    }



    private suspend fun observeRoutePredictions(){
        mapViewModel.routePrediction.collectLatest { routePrediction ->
            when(routePrediction){
                is RoutePredictionState.Error,
                RoutePredictionState.NoPrediction-> {
                    mapController.removeRouteLine()
                }
                is RoutePredictionState.Success -> {
                    mapController.drawRouteLine(routePrediction.runPoints)
                }
                RoutePredictionState.Loading -> {

                }
            }
        }
    }

    private suspend fun observeCloseDetailsBottomSheetEvent(){
        bottomSheetViewModel.closeRouteDetailsEvent.collectLatest {
            // Ejecución cuando se da click en botón para cerrar routeDetails
            nearbyRunsController.blurNearbyRunInMap()
        }
    }

    private suspend fun observeRouteSelectedBottomSheetEvent(){
            // Mostrar información de la ruta seleccionada
        bottomSheetViewModel.selectedRouteAndStop.collectLatest { routeAndStop ->
            routesController.showRouteInfo(routeAndStop)
        }
    }



    private fun setBusOnClickListener(nearbyRun: NearbyRun){
        nearbyRunsController.focusNearbyRunInMap(nearbyRun)
    }

    private fun setOnCameraMovedListener(){

        // Manejo de mostrar/ocultar botón de centrado
        centerLocationButtonController.setIsCameraFollowingUser(false)

        // Resetear seguimiento de cámara
        mapController.resetCameraObjective()
    }

    private fun setupBottomSheet() {
        // Obtener controlador de navegación para el Bottom Sheet
        bottomSheetNavController = (childFragmentManager
            .findFragmentById(binding.fragmentContainerHomePageBottomSheet.id) as NavHostFragment)
            .navController

        bottomSheetController = BottomSheetController(
            binding.layoutHomePageBottomSheet,
            bottomSheetNavController,
            centerLocationButtonController
        )

    }
}