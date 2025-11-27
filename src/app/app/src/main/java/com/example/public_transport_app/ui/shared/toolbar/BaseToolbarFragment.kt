package com.example.public_transport_app.ui.shared.toolbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.public_transport_app.R
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.example.public_transport_app.databinding.FragmentToolbarContainerBinding

/**
 * Clase padre que implementa la lógica de toolbar en el contenedor padre.
 */
abstract class BaseToolbarFragment : Fragment() {

    private var _binding: FragmentToolbarContainerBinding? = null
    protected val binding get() = _binding!! // Binding object to child classes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolbarContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupMenu()
    }

    private fun setupMenu() {
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Por defecto no inflamos nada, lo harán los fragments concretos si quieren
                this@BaseToolbarFragment.onCreateCustomMenu(menu, menuInflater)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return this@BaseToolbarFragment.onMenuItemSelected(menuItem)
            }
        }, viewLifecycleOwner)
    }

    open fun onCreateCustomMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
    }

    open fun onMenuItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.item_toolbar_closeIcon){
            // Volver a home si está en el back stack
            val navController = findNavController()
            val popped = navController.popBackStack(R.id.homePageFragment, false)

            if (!popped) {
                // Si no estaba en el back stack, navegamos globalmente
                navController.navigate(R.id.action_global_homePageFragment)
            }

            return true
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
