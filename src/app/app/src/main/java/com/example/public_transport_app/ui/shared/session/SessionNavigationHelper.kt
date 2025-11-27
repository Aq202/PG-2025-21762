package com.example.public_transport_app.ui.shared.session

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.public_transport_app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object SessionNavigationHelper {

    fun observeSessionAndNavigateIfNoDriverAccess(
        lifecycleOwner: LifecycleOwner,
        sessionStateFlow: Flow<SessionState>,
        navController: NavController,
        homeDestinationId: Int = R.id.homePageFragment
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionStateFlow.collectLatest { sessionState ->
                    if (sessionState.isInitialized() &&!sessionState.hasDriverAccess()) {
                        navController.popBackStack(homeDestinationId, false)
                    }
                }
            }
        }
    }

    fun observeSessionAndNavigateIfNoAgencyAdminAccess(
        lifecycleOwner: LifecycleOwner,
        sessionStateFlow: Flow<SessionState>,
        navController: NavController,
        homeDestinationId: Int = R.id.homePageFragment
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionStateFlow.collectLatest { sessionState ->
                    if (!sessionState.hasAgencyAdminAccess()) {
                        navController.popBackStack(homeDestinationId, false)
                    }
                }
            }
        }
    }
}
