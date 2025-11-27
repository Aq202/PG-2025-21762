package com.example.public_transport_app.ui.loginPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.example.public_transport_app.R
import com.example.public_transport_app.databinding.FragmentLoginPageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginPageFragment : Fragment() {

    private lateinit var binding:FragmentLoginPageBinding
    private val viewModel: LoginPageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservables()
        initEvents()
    }

    private fun setObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    handleStateChange(state)
                }
            }
        }
    }

    private fun handleStateChange(state: LoginPageState) {
        when(state){
            LoginPageState.Default -> {

            }
            is LoginPageState.Error -> {
                Toast.makeText(context,
                    getString(R.string.email_or_password_incorrect), Toast.LENGTH_SHORT).show()
                binding.progressIndicatorFragmentLogin.visibility = View.GONE
                binding.buttonLoginFragmentLogin.visibility = View.VISIBLE
            }
            LoginPageState.Loading -> {
                binding.progressIndicatorFragmentLogin.visibility = View.VISIBLE
                binding.buttonLoginFragmentLogin.visibility = View.GONE
            }
            LoginPageState.Success -> {
                binding.progressIndicatorFragmentLogin.visibility = View.GONE
                // Dirigir a la página de login
                requireView().findNavController().navigate(R.id.action_loginPageFragment_to_homePageFragment)
            }

            LoginPageState.EmptyEmail -> {
                Toast.makeText(context, getString(R.string.empty_email_error), Toast.LENGTH_SHORT).show()
            }
            LoginPageState.EmptyPassword -> {
                Toast.makeText(context, getString(R.string.empty_password_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initEvents(){
        binding.apply {
            binding.buttonLoginFragmentLogin.setOnClickListener{
                hideKeyboard()
                performLogin()
            }
        }
    }

    private fun performLogin() {
        val email = binding.textFieldLoginFragmentEmail.editText!!.text.toString().trim()
        val password = binding.textFieldLoginFragmentPassword.editText!!.text.toString()

        viewModel.login(email = email, password = password)

    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}