package com.example.public_transport_app.ui.routeDetailsBottomView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.public_transport_app.databinding.FragmentRouteDetailsBottomViewBinding
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.RoutePublicData
import com.example.public_transport_app.ui.homePage.BottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class RouteDetailsBottomView : Fragment() {

    private lateinit var binding: FragmentRouteDetailsBottomViewBinding
    private val args: RouteDetailsBottomViewArgs by navArgs()
    private val viewModel: RouteDetailsViewModel by viewModels()
    private val bottomSheetViewModel: BottomSheetViewModel by viewModels(
        ownerProducer = { requireParentFragment().requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRouteDetailsBottomViewBinding.inflate(inflater, container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleBackPress()
        getRouteDetails()
        initObservers()
        setListeners()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navController = findNavController()

                    // Navegar hacia atrás en navGraph de bottom sheet
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        // No hay más atrás en este navGraph, hacer back en padre
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun getRouteDetails(){
        val routeId = args.routeId
        viewModel.getRouteDetails(routeId)
    }

    private fun initObservers(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                observeRouteDetailsChange()
            }
        }
    }

    private suspend fun observeRouteDetailsChange(){
        viewModel.routeDetails.collectLatest { routeDetails ->
            when(routeDetails){
                RouteDetailsState.LoadingRouteDetails -> {
                    binding.scrollViewRouteDetailsMainContainer.visibility = View.GONE
                    binding.containerNoResultsContent.visibility = View.GONE
                }
                is RouteDetailsState.RouteDetailsError -> {
                    binding.scrollViewRouteDetailsMainContainer.visibility = View.GONE
                    binding.containerNoResultsContent.visibility = View.VISIBLE
                }
                is RouteDetailsState.RouteDetailsLoaded -> {
                    binding.scrollViewRouteDetailsMainContainer.visibility = View.VISIBLE
                    binding.containerNoResultsContent.visibility = View.GONE

                    setRouteDetailsInUI(routeDetails.routeDetails)
                }
            }
        }
    }

    private fun setRouteDetailsInUI(routeDetails: RoutePublicData){

        // Nombre de la ruta
        binding.textViewRouteDetailsRouteName.text = routeDetails.name

        // Image slider
        val slider = binding.imageSliderRouteDetails
        slider.setImages(routeDetails.unitImages)

        // Colocar horario
        val startScheduleTextView:Map<DayOfWeek, TextView> = mapOf(
            DayOfWeek.MONDAY to binding.textViewMondaySchedule,
            DayOfWeek.TUESDAY to binding.textViewTuesdaySchedule,
            DayOfWeek.WEDNESDAY to binding.textViewThursdaySchedule,
            DayOfWeek.THURSDAY to binding.textViewWednesdaySchedule,
            DayOfWeek.FRIDAY to binding.textViewFridaySchedule,
            DayOfWeek.SATURDAY to binding.textViewSaturdaySchedule,
            DayOfWeek.SUNDAY to binding.textViewSundaySchedule,
        )

        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        for ((dayOfWeek, schedule) in routeDetails.schedules){

            val scheduleText = if (schedule.serviceAvailable) {
                val openTimeStr = schedule.open?.format(formatter)
                val closeTimeStr = schedule.close?.format(formatter)

                when {
                    openTimeStr != null && closeTimeStr != null -> "$openTimeStr - $closeTimeStr"
                    openTimeStr != null -> getString(R.string.open_schedule, openTimeStr)
                    closeTimeStr != null -> getString(R.string.close_schedule, closeTimeStr)
                    else -> getString(R.string.no_service)
                }
            } else {
                getString(R.string.no_service)
            }

            val textView = startScheduleTextView[dayOfWeek]
            textView?.text = scheduleText
        }
    }

    private fun setListeners(){
        binding.imageButtonRouteDetailsCloseButton.setOnClickListener {
            closeRouteDetails()
        }
    }

    private fun closeRouteDetails(){
        // Ejecutar evento en view model compartido con HomePageFragment
        bottomSheetViewModel.closeRouteDetails()
    }

}