package com.example.public_transport_app.data.remote.auth

import com.example.public_transport_app.data.local.DataKey
import com.example.public_transport_app.data.local.DataStoreManager
import com.example.public_transport_app.data.local.EncryptedPreferencesManager
import com.example.public_transport_app.data.remote.AuthAPI
import javax.inject.Inject

class TokenProviderImp @Inject constructor(
    private val encryptedPreferences: EncryptedPreferencesManager,
    private val api: AuthAPI,
    private val dataStore: DataStoreManager
): TokenProvider {

    /**
     * Método de tokenProvider para obtener AccessToken
     * @return String | Null. Devuelve token si existe, de lo contrario null.
     */
    override suspend fun getAccessToken(): String? {
        return encryptedPreferences.getValue(DataKey.sessionAccessToken, "")
            .takeIf { it.isNotEmpty() }
    }


    /**
     * Método de tokenProvider para solicitar nuevo accessToken y reemplazar el actual.
     * @return String | Null. Devuelve el token si se logró refrescar, de lo contrario null.
     */
    override suspend fun refreshAccessToken(): String? {
        val refreshToken = encryptedPreferences.getValue(DataKey.sessionRefreshToken)

        try {
            val response = api.refreshToken(refreshToken)

            if (response.isSuccessful) {
                val body = response.body()
                val newAccessToken = body?.accessToken

                if (!newAccessToken.isNullOrBlank()) {
                    encryptedPreferences.saveKeyValue(DataKey.sessionAccessToken, newAccessToken)
                    return newAccessToken
                }
            }

            // Si el token no se pudo refrescar, limpiar tokens de sesión
            clearSession()
            return null
        } catch (e: Exception) {
            println("Error al refrescar token: ${e.message}")
            return null
        }
    }

    private suspend fun clearSession(){
        encryptedPreferences.removeKey(DataKey.sessionAccessToken)
        encryptedPreferences.removeKey(DataKey.sessionRefreshToken)

        val userDataKeys = listOf(
            DataKey.userId,
            DataKey.userName,
            DataKey.userLastname,
            DataKey.userRole,
            DataKey.userEmail
        )
        dataStore.removeKeys(userDataKeys)
    }
}
