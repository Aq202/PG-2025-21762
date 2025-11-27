package com.example.public_transport_app.data.remote.auth

/**
 * Proveedor de tokens que permite obtener y refrescar el access token actual.
 *
 * Esta interfaz abstrae el mecanismo de acceso y renovación del token,
 * permitiendo desacoplar componentes como interceptores o autenticadores
 * de los detalles concretos de la sesión o almacenamiento.
 */
interface TokenProvider {

    /**
     * Obtiene el access token actual
     * @return El access token si está disponible, o null si no existe.
     */
    suspend fun getAccessToken(): String?

    /**
     * Refresca el access token utilizando el token de actualización (refresh token).
     * Este método debe encargarse también de persistir el nuevo token.
     *
     * @return El nuevo access token si el refresco fue exitoso, o null si falló.
     */
    suspend fun refreshAccessToken(): String?
}
