package com.example.public_transport_app.data.repository

sealed class Resource<T>(val message:String? = null) {

    class Success<T>(val data:T): Resource<T>()
    class Error<T>(message:String?):Resource<T>(message=message)
}