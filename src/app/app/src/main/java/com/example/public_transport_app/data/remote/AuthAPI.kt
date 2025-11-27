package com.example.public_transport_app.data.remote

import com.example.public_transport_app.data.remote.dto.request.LoginRequest
import com.example.public_transport_app.data.remote.dto.response.GenerateDeviceTokenResponse
import com.example.public_transport_app.data.remote.dto.response.LoginResponse
import com.example.public_transport_app.data.remote.dto.response.LogoutResponse
import com.example.public_transport_app.data.remote.dto.response.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthAPI {

    @POST("/api/session/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("/api/session/logout")
    suspend fun logout(
        @Header("Authorization") refreshToken: String
    ): Response<LogoutResponse>

    @POST("/api/session/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<RefreshTokenResponse>

    @POST("/api/session/device")
    suspend fun generateDeviceToken(): Response<GenerateDeviceTokenResponse>
}