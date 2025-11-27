package com.example.public_transport_app.ui.shared.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.repository.SessionRepository
import com.example.public_transport_app.utils.roles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _sessionStateFlow:MutableStateFlow<SessionState> = MutableStateFlow(SessionState.UNITIALIZED)
    val sessionStateFlow: StateFlow<SessionState> = _sessionStateFlow

    init {
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            sessionRepository.getUserInSessionFlow().collect { user ->

                // Guardar usuario en sesión
                val newState = when{
                    user?.role == null -> SessionState.UNLOGGED
                    user.role == roles.admin -> SessionState.ADMIN_LOGGED(user)
                    user.role == roles.transportCompanyAdmin ->
                        SessionState.AGENCY_ADMIN_LOGGED(user)
                    user.role == roles.driver -> SessionState.DRIVER_LOGGED(user)
                    else -> SessionState.DEFAULT
                }
                _sessionStateFlow.value = newState
            }
        }
    }

    /**
     * Cerrar sesión del usuario
     */
    public suspend fun logout(){
        sessionRepository.logout()
    }
}