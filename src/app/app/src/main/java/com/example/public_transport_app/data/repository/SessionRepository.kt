package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    suspend fun login(email:String, password:String): Resource<Boolean>
    suspend fun getUserInSessionFlow(): Flow<User?>
    suspend fun logout()
    suspend fun generateDeviceToken(): Resource<Boolean>
}