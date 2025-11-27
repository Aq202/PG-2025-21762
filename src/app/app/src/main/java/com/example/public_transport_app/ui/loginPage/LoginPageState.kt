package com.example.public_transport_app.ui.loginPage

sealed interface LoginPageState{
    data object Default: LoginPageState
    data object Success: LoginPageState
    data object Loading: LoginPageState
    data object EmptyEmail: LoginPageState
    data object EmptyPassword: LoginPageState
    data class Error(val message: String?): LoginPageState
}