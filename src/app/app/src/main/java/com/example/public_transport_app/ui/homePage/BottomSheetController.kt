package com.example.public_transport_app.ui.homePage

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import com.example.public_transport_app.R
import com.example.public_transport_app.ui.mainBottomView.MainBottomViewFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetController(
    bottomSheet: ConstraintLayout,
    private val navController: NavController,
    private val centerLocationButtonController: CenterLocationButtonController,
) {

    private val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
    var state: Int = BottomSheetBehavior.STATE_COLLAPSED
        private set

    init {
        setupCallback()
    }

    private fun setupCallback() {
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val peekHeight = bottomSheetBehavior.peekHeight
                val expandedOffset = bottomSheetBehavior.expandedOffset
                val totalHeight = bottomSheet.height
                val effectiveMovement = totalHeight - peekHeight - expandedOffset
                val delta = effectiveMovement * slideOffset
                centerLocationButtonController.centerLocationButton.translationY = -delta
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                state = newState
                centerLocationButtonController.setBottomSheetState(newState)
            }
        })
    }

    fun expandHalf() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun collapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun navigateToRouteDetails(routeId: String) {
        val action = MainBottomViewFragmentDirections.actionGlobalRouteDetailsBottomView(routeId)
        val currentArgs = navController.currentBackStackEntry?.arguments
        val currentRouteId = currentArgs?.getString("routeId")

        // Solo navegar si el routeId es diferente
        if (currentRouteId != routeId) {
            navController.navigate(action)
        }
    }

    fun navigateToMainView() {
        navController.navigate(R.id.action_global_mainBottomViewFragment)
    }

}
