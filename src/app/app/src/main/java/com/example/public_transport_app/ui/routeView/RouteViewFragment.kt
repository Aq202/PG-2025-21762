package com.example.public_transport_app.ui.routeView

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.HandlerThread
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Route
import com.example.public_transport_app.databinding.FragmentRouteViewBinding
import com.example.public_transport_app.ui.infoDialog.ErrorDialog
import com.example.public_transport_app.ui.shared.loadingDialog.LoadingDialog
import com.example.public_transport_app.ui.shared.permissions.LocationPermissionManager
import com.example.public_transport_app.ui.shared.session.SessionNavigationHelper
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import com.example.public_transport_app.ui.trackingService.TrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant

@AndroidEntryPoint
class RouteViewFragment : BaseToolbarFragment() {

     private val args: RouteViewFragmentArgs by navArgs()
     private var _contentBinding: FragmentRouteViewBinding? = null
     private val viewModel: RouteViewModel by viewModels()
     private val sessionViewModel: SessionViewModel by activityViewModels()

     // Thread para obtener ubicación de GPS
     private lateinit var locationThread: HandlerThread

    // Loading dialog
    private var loadingDialog: LoadingDialog? = null

     private lateinit var locationPermissionManager: LocationPermissionManager

     // Servicio en segundo plano
     private var serviceBound = false // Indica si el servicio se encuentra enlazado al fragment
     private var trackingService: TrackingService? = null

     private val serviceConnection = object : ServiceConnection {

         /**
          * Se ejecuta cuando el fragment se conecta al servicio, mediante un intent y un bindservice
          * Esto se ejecuta luego de la función (reconnectToTrackingService), la cual se emplea
          * al iniciar el fragment.
          */
         override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
             val localBinder = binder as TrackingService.LocalBinder
             trackingService = localBinder.getService()
             serviceBound = true

             val routeId = trackingService?.getRouteId()
             val state = trackingService?.getCurrentState()

             // Cambiar el estado de la UI del fragment dependiendo del estado del Tracking service
             // Esto se ejecuta cuando se vuelve a entar al fragment para sincrinizar el estado
             when(state){
                 TrackingService.RunState.RUNNING -> {
                    if(routeId == args.routeId){
                        // Si el viaje corresponde al fragment, cambiar a estado running
                        setRunRunningState()
                    }else{
                        // Otro viaje está en progreso
                        setAnotherRunInCourseState()
                    }
                 }
                 TrackingService.RunState.PAUSED -> {
                     if(routeId == args.routeId){
                         // Si el viaje corresponde al fragment, cambiar a estado paused
                         setRunPausedState()
                     }else{
                         // Otro viaje está en progreso (pausado)
                         setAnotherRunInCourseState()
                     }
                 }
                 TrackingService.RunState.STOPPED -> {
                     setRunStoppedState()
                 }
                 null -> {
                     setRunStoppedState()
                 }
             }

         }

         override fun onServiceDisconnected(name: ComponentName?) {
             serviceBound = false
             trackingService = null
         }
     }

     override fun onStart() {
         super.onStart()

         // Iniciar el thread para obtener ubicación
         locationThread = HandlerThread("LocationThread").apply { start() }

         // Intentar reconectar al servicio de tracking
         reconnectToTrackingService()
     }

     override fun onDestroy() {
         super.onDestroy()

         locationThread.quitSafely()
     }

     override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val parent = super.onCreateView(inflater, container, savedInstanceState)

        // Inflate el contenido dentro del contenedor del BaseToolbarFragment
        _contentBinding = FragmentRouteViewBinding.inflate(inflater, binding.contentContainer, true)

         // Iniciar clase para el manejo de permisos (clase propia)
        locationPermissionManager = LocationPermissionManager(requireContext(), this)

        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.my_routes)

        // Obtener routeId de los argumentos del fragment
        val routeId = args.routeId


        // Navegar a home si ya no se cuenta con acceso de conductor
        SessionNavigationHelper.observeSessionAndNavigateIfNoDriverAccess(
            viewLifecycleOwner,
            sessionViewModel.sessionStateFlow,
            findNavController()
        )

        // Obtener datos de la ruta
        viewModel.getRoute(routeId)

        setObservables()
        setListeners()
    }

     override fun onStop() {
         super.onStop()
         disconnectToTrackingService()
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

                launch {
                    observeRouteData()
                }

                launch {
                    observeRouteRunState()
                }

                launch {
                    observeAgencyAdminPermission()
                }
            }
        }
    }

    private suspend fun observeRouteData(){
        // Listener para obtener información de la ruta
        viewModel.uiState.collectLatest { state ->
            when(state) {
                RouteState.LoadingRoute -> {
                    openLoadingDialog()
                }
                is RouteState.RouteError -> {
                    closeLoadingDialog()

                    // Mostrar error obtenido
                    val genError = getString(R.string.general_error)
                    Toast.makeText(context, "$genError ${state.error}", Toast.LENGTH_SHORT).show()
                }
                is RouteState.RouteLoaded -> {
                    closeLoadingDialog()
                    setRouteInfo(state.route)

                    // Validar si se tiene permiso de agencyAdmin
                    viewModel.verifyIfUserIsAgencyAdmin(state.route.agency.id)
                }
            }
        }
    }

    private suspend fun observeRouteRunState(){
        // Listener para obtener routeId una vez iniciado un viaje
        viewModel.routeRunState.collectLatest { routeRunState ->

            when(routeRunState){
                RouteRunState.Default -> {

                }

                RouteRunState.StartingRun -> {
                    openLoadingDialog()
                }

                is RouteRunState.RunStarted -> {
                    closeLoadingDialog()

                    // Iniciar servicio de tracking en segundo plano
                    startTrackingService(args.routeId, routeRunState.runId)

                    // Cambiar el estado de la UI a running
                    setRunRunningState()
                }

                is RouteRunState.StartingRunError -> {
                    closeLoadingDialog()

                    // Volver el estado de la UI a stoped
                    setRunStoppedState()

                    // Mostrar mensaje de error
                    val genError = getString(R.string.general_error)
                    Toast.makeText(requireContext(),"$genError ${routeRunState.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun observeAgencyAdminPermission(){
        viewModel.isAgencyAdmin.collectLatest { agencyAdminPermissionState ->
            when(agencyAdminPermissionState){
                AgencyAdminPermissionState.Default,
                AgencyAdminPermissionState.NoPermission-> {
                    _contentBinding?.fabRoutesListUpdateStops?.isVisible = false
                }
                is AgencyAdminPermissionState.Error -> {
                    _contentBinding?.fabRoutesListUpdateStops?.isVisible = false
                    ErrorDialog(requireContext())
                        .show(
                            getString(R.string.error_has_occurred),
                            agencyAdminPermissionState.message,
                            )
                }
                AgencyAdminPermissionState.IsAgencyAdmin -> {
                    _contentBinding?.fabRoutesListUpdateStops?.isVisible = true
                }
            }
        }
    }

    private fun setRouteInfo(route: Route){
        // Colocar título en toolbar
        binding.toolbar.title = route.name

        // Colocar título de la ruta
        _contentBinding?.textviewRouteViewFragmentTitle?.text = route.name
    }

     private fun setListeners() {
         _contentBinding?.let {

             // Botón para iniciar viaje
             it.buttonRouteViewFragmentStartRun.setOnClickListener{
                 handleStartRunClickListener()
             }

             // Botón para volver a solicitar permisos
             it.buttonRouteViewFragmentPermissions.setOnClickListener{
                 handleRequestPermissionsListener()
             }

             // Botón para pausar viaje
             it.buttonRouteViewFragmentPauseRun.setOnClickListener{
                 handlePauseRunClickListener()
             }

             // Botón para reaundar viaje
             it.buttonRouteViewFragmentResumeRun.setOnClickListener{
                 handleResumeRunClickListener()
             }

             // Botón para finalizar viaje
             it.buttonRouteViewFragmentFinishRun.setOnClickListener{
                 handleFinishRunClickListener()
             }

             // FAB para modificar paradas
             it.fabRoutesListUpdateStops.setOnClickListener{
                 handleUpdateStopsClickListener()
             }
         }
     }

     private fun handleStartRunClickListener(){
         checkPermissions { granted ->
             // Mostrar u ocultar rational

             if (granted) {
                 // Permisos otorgados, continuar con la acción

                 lifecycleScope.launch {
                     try {
                         // Realizar petición para iniciar viaje
                         viewModel.startRun(
                             routeId = args.routeId,
                             time = Instant.now()
                         )

                     } catch (_: IllegalStateException) {
                         Toast.makeText(
                             requireContext(),
                             getString(R.string.get_location_data_error),
                             Toast.LENGTH_SHORT
                         ).show()
                     }
                 }

             } else {
                 // Permisos no otorgados
                 // deshabilitar botón de iniciar viaje
                 _contentBinding?.buttonRouteViewFragmentStartRun?.isEnabled = false
                 // Habilitar botón para reintentar
                 _contentBinding?.buttonRouteViewFragmentPermissions?.isVisible = true
             }
         }
     }

     private fun handlePauseRunClickListener(){
         // Establecer estado en UI
         setRunPausedState()

         // Pausar tracking en servicio
         trackingService?.pauseTracking()
     }

     private fun handleResumeRunClickListener(){
         // Establecer estado en UI
         setRunRunningState()

         // Reanudar tracking en servicio
         trackingService?.resumeTracking()
     }

     private fun handleFinishRunClickListener(){

         val runId = trackingService?.getRunId()

         // Finalizar viaje en el api (no es determinante si falla)
         if(runId != null){
             viewModel.finishRun(runId)
         }

         // Establecer estado en UI
         setRunStoppedState()

         // Finalizar servicio de tracking si está enlazado
         if (serviceBound) {
             requireActivity().unbindService(serviceConnection)
             serviceBound = false
             trackingService = null
         }

         // Detener el servicio
         val intent = Intent(requireContext(), TrackingService::class.java)
         requireContext().stopService(intent)


     }

    private fun handleUpdateStopsClickListener(){
        // Para cambiar al fragment de actualizar stops, se reguiere el agencyId
        if(viewModel.uiState.value is RouteState.RouteLoaded){
            val agencyId = (viewModel.uiState.value as RouteState.RouteLoaded).route.agency.id
            val action = RouteViewFragmentDirections.actionRouteViewFragmentToUpdateRouteStopsFragment(
                args.routeId, agencyId)
            findNavController().navigate(action)
        }
    }

     private fun handleRequestPermissionsListener(){
         AlertDialog.Builder(context)
             .setTitle(getString(R.string.full_location_permission_rational_start_run_title))
             .setMessage(getString(R.string.full_location_permission_rational_start_run_description))
             .setPositiveButton("Activar") { _, _ ->
                 // Abre la configuración de la app
                 val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                     data = Uri.fromParts("package", requireContext().packageName, null)
                 }
                 requireContext().startActivity(intent)

                 // Resetear botón de iniciar viaje (este volverá a realizar la verificación de permisos)
                 _contentBinding?.buttonRouteViewFragmentStartRun?.isEnabled = true
                 _contentBinding?.buttonRouteViewFragmentPermissions?.isVisible = false
             }
             .setNegativeButton("Cancelar", null)
             .show()
     }

     private fun startTrackingService(routeId: String, runId: String){
         val intent = Intent(requireContext(), TrackingService::class.java).apply {
             putExtra(TrackingService.ROUTE_ID, routeId)
             putExtra(TrackingService.RUN_ID, runId)
         }

         // Iniciar servicio en segundo plano
         startForegroundService(requireContext(), intent)

         // Luego haces bind
         requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

     }

     private fun reconnectToTrackingService(){
         // Intent para reconectar al servicio de tracking
         val intent = Intent(requireContext(), TrackingService::class.java)
         requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
     }

     private fun disconnectToTrackingService(){
         // Si el fragment está conectado, remover la conexión al service
         if(serviceBound){
             requireActivity().unbindService(serviceConnection)
             serviceBound = false
         }
     }

     /**
      * Solicitar los permisos de ubicación precisa, servicios en segundo plano (ubicación)
      */
     private fun checkPermissions(onResult: (Boolean) -> Unit) {
         locationPermissionManager.requestPreciseLocation(
             rationaleTitle = getString(R.string.location_permission_rational_start_run_title),
             rationaleMessage = getString(R.string.location_permission_rational_start_run_description),
         ) { isLocationPermissionGranted ->
             if (!isLocationPermissionGranted) {
                 onResult(false)
                 return@requestPreciseLocation
             }

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                 // Solicitar permiso de ubicación en segundo plano
                 locationPermissionManager.requestBackgroundLocation(
                     rationaleTitle = getString(R.string.back_location_permission_rational_start_run_title),
                     rationaleMessage = getString(R.string.back_location_permission_rational_start_run_description)
                 ) { isBackgroundLocationGranted ->

                     onResult(isBackgroundLocationGranted)
                 }

             } else {
                 onResult(true)
             }
         }
     }

     /**
      * Cambiar el estado de la UI para cuando el viaje del fragment actual no está en curso.
      */
     private fun setRunStoppedState(){
         _contentBinding?.let{
             // Botón de iniciar viaje
             it.buttonRouteViewFragmentStartRun.isVisible = true
             it.buttonRouteViewFragmentStartRun.isEnabled = true

             // Botón de pausar viaje
             it.buttonRouteViewFragmentPauseRun.isVisible = false

             // Botón de resumir viaje
             it.buttonRouteViewFragmentResumeRun.isVisible = false

             // Botón de terminar viaje
             it.buttonRouteViewFragmentFinishRun.isVisible = false
         }
     }

     /**
      * Cambiar el estado de la UI para cuando el viaje del fragment actual está en curso.
      */
     private fun setRunRunningState(){
         _contentBinding?.let{
             // Botón de iniciar viaje
             it.buttonRouteViewFragmentStartRun.isVisible = false

             // Botón de pausar viaje
             it.buttonRouteViewFragmentPauseRun.isVisible = true

             // Botón de resumir viaje
             it.buttonRouteViewFragmentResumeRun.isVisible = false

             // Botón de terminar viaje
             it.buttonRouteViewFragmentFinishRun.isVisible = true
         }
     }

     /**
      * Cambiar el estado de la UI para cuando el viaje del fragment actual se encuentra pausado.
      */
     private fun setRunPausedState(){
         _contentBinding?.let{
             // Botón de iniciar viaje
             it.buttonRouteViewFragmentStartRun.isVisible = false

             // Botón de pausar viaje
             it.buttonRouteViewFragmentPauseRun.isVisible = false

             // Botón de resumir viaje
             it.buttonRouteViewFragmentResumeRun.isVisible = true

             // Botón de terminar viaje
             it.buttonRouteViewFragmentFinishRun.isVisible = true
         }
     }

     /**
      * Cambiar el estado de la UI para cuando un viaje de otra ruta está en curso
      */
     private fun setAnotherRunInCourseState(){
         _contentBinding?.let{
             // Botón de iniciar viaje
             it.buttonRouteViewFragmentStartRun.isVisible = true
             it.buttonRouteViewFragmentStartRun.isEnabled = false

             // Botón de pausar viaje
             it.buttonRouteViewFragmentPauseRun.isVisible = false

             // Botón de resumir viaje
             it.buttonRouteViewFragmentResumeRun.isVisible = false

             // Botón de terminar viaje
             it.buttonRouteViewFragmentFinishRun.isVisible = false
         }
     }

 }