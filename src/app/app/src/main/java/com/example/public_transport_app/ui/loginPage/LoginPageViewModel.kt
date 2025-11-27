package com.example.public_transport_app.ui.loginPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.repository.Resource
import com.example.public_transport_app.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginPageViewModel @Inject constructor(
    private val repository: SessionRepository
): ViewModel(){

    private val _uiState: MutableStateFlow<LoginPageState> = MutableStateFlow(LoginPageState.Default)
    val uiState: StateFlow<LoginPageState> = _uiState

    fun login(email:String, password:String){
        viewModelScope.launch {
            _uiState.value = LoginPageState.Loading

            if(email.trim() == ""){
                _uiState.value = LoginPageState.EmptyEmail
                return@launch
            }

            if(password.trim() == ""){
                _uiState.value = LoginPageState.EmptyPassword
                return@launch
            }

            when(val result = repository.login(email, password)){
                is Resource.Success ->{
                    _uiState.value = LoginPageState.Success
                }
                is Resource.Error -> {
                    _uiState.value = LoginPageState.Error(result.message)
                }
            }
        }
    }
}