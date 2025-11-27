package com.example.public_transport_app.data.remote.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
import javax.inject.Inject

/**
 * Autenticador que intercepta respuestas HTTP 401 (no autorizado) e intenta
 * refrescar el token de acceso utilizando un TokenProvider.
 *
 * Si se obtiene un nuevo token exitosamente, se reintenta la petición original
 * con el nuevo token en el header "Authorization".
 *
 * @property tokenProvider Componente responsable de proveer y refrescar el token.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider
) : Authenticator {

    /**
     * Lógica de autenticación que se ejecuta cuando una respuesta retorna 401.
     *
     * @param route La ruta de conexión actual.
     * @param response La respuesta que provocó el intento de autenticación.
     * @return Una nueva solicitud con el token actualizado, o null si no se puede renovar.
     */
    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        // Evitar bucles infinitos de autenticación
        if (responseCount(response) >= 2) return null

        // Intentar refrescar el token de forma bloqueante
        val newAccessToken = runBlocking {
            tokenProvider.refreshAccessToken()
        } ?: return null

        // Construir una nueva solicitud con el token actualizado
        return response.request.newBuilder()
            .header("Authorization", newAccessToken)
            .build()
    }

    /**
     * Cuenta la cantidad de respuestas previas para evitar loops infinitos.
     */
    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
