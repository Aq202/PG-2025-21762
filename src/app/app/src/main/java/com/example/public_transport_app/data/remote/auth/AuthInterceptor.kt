package com.example.public_transport_app.data.remote.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor que agrega automáticamente el token de acceso a cada solicitud HTTP saliente.
 *
 * El token se obtiene desde un [TokenProvider], permitiendo desacoplar la lógica de
 * autenticación del interceptor en sí.
 *
 * Este interceptor asegura que todas las peticiones protegidas
 * incluyan el encabezado Authorization correspondiente.
 *
 * @property tokenProvider Proveedor del token de acceso actual.
 */
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    /**
     * Intercepta la solicitud original y agrega el header "Authorization" con el token, si existe.
     *
     * @param chain Cadena de interceptores de OkHttp.
     * @return Respuesta HTTP resultante.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtener el token de forma bloqueante (desde una función suspendida)
        val token = runBlocking { tokenProvider.getAccessToken() }

        // Crear una nueva solicitud con el header Authorization si hay token
        val newRequest = chain.request().newBuilder().apply {
            token?.let {
                header("Authorization", it)
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
