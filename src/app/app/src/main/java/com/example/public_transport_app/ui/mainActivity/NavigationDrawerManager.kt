package com.example.public_transport_app.ui.mainActivity

import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.public_transport_app.R
import com.example.public_transport_app.databinding.NavigationDrawerHeaderBinding
import com.example.public_transport_app.databinding.ActivityMainBinding
import com.example.public_transport_app.ui.shared.session.SessionState
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NavigationDrawerManager(
    private val binding: ActivityMainBinding,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val lifecycle: Lifecycle,
    private val sessionViewModel: SessionViewModel,
    private val navController: NavController
) {
    private val headerView: View = binding.navView.getHeaderView(0)
    private val navigationDrawerHeaderBinding = NavigationDrawerHeaderBinding.bind(headerView)

    fun setupDrawer() {
        setObservers()
        setupNavItemSelectedListener()
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(binding.navView)
    }

    private fun setObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.sessionStateFlow.collectLatest { session ->
                    setNavDrawerInfo(session)
                    setNavDrawerOptions(session)
                }
            }
        }
    }

    private fun setNavDrawerInfo(session: SessionState) {
        val username = when (session) {
            is SessionState.ADMIN_LOGGED -> session.user.name
            is SessionState.DRIVER_LOGGED -> session.user.name
            is SessionState.AGENCY_ADMIN_LOGGED -> session.user.name
            SessionState.UNLOGGED -> ""
            SessionState.UNITIALIZED -> ""
            SessionState.DEFAULT -> ""
        }

        val txtName: TextView = navigationDrawerHeaderBinding.textViewSideNavBarName
        txtName.text = username
    }

    private fun setNavDrawerOptions(session: SessionState){
        val menu = binding.navView.menu
        val loginOption = menu.findItem(R.id.sideNav_item_login)
        val logoutOption = menu.findItem(R.id.sideNav_item_logout)
        val routesListOption = menu.findItem(R.id.sideNav_item_routes)
        val stopsListOption = menu.findItem(R.id.sideNav_item_stops)

        // Logged vs unlogged
        val logged = session !is SessionState.UNLOGGED
        loginOption.isVisible = !logged
        logoutOption.isVisible = logged
        routesListOption.isVisible = false
        stopsListOption.isVisible = false

        // Determinar opciones según privilegios
        when (session) {
            is SessionState.ADMIN_LOGGED -> {
                routesListOption.isVisible = true
                stopsListOption.isVisible = true
            }
            is SessionState.DRIVER_LOGGED -> {
                routesListOption.isVisible = true
            }
            is SessionState.AGENCY_ADMIN_LOGGED -> {
                routesListOption.isVisible = true
                stopsListOption.isVisible = true
            }
            SessionState.UNLOGGED, -> {
            }

            SessionState.UNITIALIZED -> {
            }

            SessionState.DEFAULT -> {
            }

        }
    }

    private fun setupNavItemSelectedListener() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sideNav_item_logout -> {
                    handleLogout()
                    true
                }
                R.id.sideNav_item_login -> {
                    handleLogin()
                    true
                }
                R.id.sideNav_item_routes -> {
                    handleRoutesItem()
                    true
                }
                R.id.sideNav_item_stops -> {
                    handleStopsItem()
                    true
                }
                else -> false // False - indica que el evento no fue manejado
            }.also {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.logout()
            }
        }
    }

    private fun handleLogin() {
        navController.navigate(
            R.id.action_global_loginPageFragment,
            null,
            NavOptions.Builder()
                .setLaunchSingleTop(true) // Evitar múltiples ventanas de login
                .build()
        )
    }

    private fun handleRoutesItem(){
        navController.navigate(
            R.id.action_global_routesListFragment,
            null,
            NavOptions.Builder()
                .setLaunchSingleTop(true) // Evitar múltiples ventanas
                .build()
        )
    }

    private fun handleStopsItem(){
        navController.navigate(
                R.id.action_global_stopsListFragment,
                null,
            NavOptions.Builder()
                .setLaunchSingleTop(true) // Evitar múltiples ventanas
                .build()
        )
    }
}
