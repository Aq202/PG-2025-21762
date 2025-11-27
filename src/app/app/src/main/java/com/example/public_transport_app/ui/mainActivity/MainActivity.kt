package com.example.public_transport_app.ui.mainActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.public_transport_app.R
import com.example.public_transport_app.databinding.ActivityMainBinding
import com.example.public_transport_app.ui.shared.session.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val sessionViewModel: SessionViewModel by viewModels()
    private lateinit var drawerManager: NavigationDrawerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureNavigation()

        drawerManager = NavigationDrawerManager(
            binding,
            lifecycleScope,
            lifecycle,
            sessionViewModel,
            navController
        )
        drawerManager.setupDrawer()

        // Manejar apertura con deep links
        if (intent != null) {
            handleDeepLink(intent)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun configureNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer_mainActivity) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun openDrawer(){
        drawerManager.openDrawer()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "publictransport" && data.host == "route") {
            val routeId = data.lastPathSegment
            if (routeId != null) {
                println("DeepLink: Route ID recibido: $routeId")
                // Navega al fragment deseado

            }
        }
    }

}
