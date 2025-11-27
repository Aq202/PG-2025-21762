package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.local.DataKey
import com.example.public_transport_app.data.local.DataStoreManager
import com.example.public_transport_app.data.local.EncryptedPreferencesManager
import com.example.public_transport_app.data.entity.User
import com.example.public_transport_app.data.remote.AuthAPI
import com.example.public_transport_app.data.remote.dto.request.LoginRequest
import com.example.public_transport_app.utils.ErrorParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SessionRepositoryImp @Inject constructor(
    private val authApi: AuthAPI,
    private val dataStore: DataStoreManager,
    private val encryptedPreferences: EncryptedPreferencesManager
): SessionRepository {

    // Flow que proporciona cambios en usuario en sesión
    private val userInSessionFlow: Flow<User?> = combine(
        dataStore.getValueFlow(DataKey.userId, null),
        dataStore.getValueFlow(DataKey.userName, null),
        dataStore.getValueFlow(DataKey.userLastname, null),
        dataStore.getValueFlow(DataKey.userEmail, null),
        dataStore.getValueFlow(DataKey.userRole, null)
    ) { id, name, lastname, email, role ->
        if(id != null && name != null && lastname != null && email != null && role != null){
            User(
                id = id,
                name = name,
                lastname = lastname,
                email = email,
                role = role.toInt()
            )
        }else{
            null
        }

    }

    private suspend fun saveUserData(user: User){
        val userData:Map<String, String> = mapOf(
            DataKey.userId to user.id,
            DataKey.userName to user.name,
            DataKey.userLastname to user.lastname,
            DataKey.userRole to user.role.toString(),
            DataKey.userEmail to user.email,
        )
        dataStore.saveKeyValues(userData)
    }

    private fun saveDeviceToken(deviceToken: String){
        encryptedPreferences.saveKeyValue(DataKey.deviceToken, deviceToken)
    }

    private fun saveSessionAccessToken(accessToken: String){
        encryptedPreferences.saveKeyValue(DataKey.sessionAccessToken, accessToken)
    }

    private fun saveSessionRefreshToken(refreshToken: String){
        encryptedPreferences.saveKeyValue(DataKey.sessionRefreshToken, refreshToken)
    }

    private suspend fun clearUserData(){
        val userDataKeys = listOf(
            DataKey.userId,
            DataKey.userName,
            DataKey.userLastname,
            DataKey.userRole,
            DataKey.userEmail
        )
        dataStore.removeKeys(userDataKeys)
    }

    private fun clearSessionTokens(){
        encryptedPreferences.removeKey(DataKey.sessionAccessToken)
        encryptedPreferences.removeKey(DataKey.sessionRefreshToken)
    }

    private fun getDeviceToken():String?{
        return encryptedPreferences.getValue(DataKey.deviceToken).ifEmpty { null }
    }

    /**
     * Realizar petición para hacer login. Obtiene datos de usuario y tokens de sesión en caso de
     * que sea exitoso.
     * @param email String. Email del usuario
     * @param password String. Contraseña del usuario
     * @return Resource: Success, cuando el login fue exitoso. Error, cuando no se pudo iniciar
     * sesión, ya sea por credenciales incorrectas o por un error en el servidor.
     */
    override suspend fun login(email: String, password: String): Resource<Boolean> {
        try {
            val loginBody = LoginRequest(email, password)
            val result = authApi.login(loginBody)
            val body = result.body()
            if(result.isSuccessful && body != null){

                // Lógica para guardar tokens y sesión
                saveUserData(body.user)
                saveSessionAccessToken(body.accessToken)
                saveSessionRefreshToken(body.refreshToken)

                return Resource.Success(true)

            }else{
                val errorBody = result.errorBody()
                val error = ErrorParser.parseErrorMessage(errorBody)
                return Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en repository "+ex.message)
            ex.printStackTrace()
            return Resource.Error(null)
        }

    }

    override suspend fun getUserInSessionFlow(): Flow<User?> {
        return userInSessionFlow
    }

    /**
     * Cerrar sesión en backend y limpiar datos de sesión locales.
     */
    override suspend fun logout() {

        val refreshToken = encryptedPreferences.getValue(DataKey.sessionRefreshToken, "")
            .takeIf { it.isNotEmpty() }

        // Hacer logout en el backend
        try{
            if (refreshToken != null) authApi.logout(refreshToken)

        }catch (ex: Exception){
            println("Error en logout:sessionRepository"+ex.message)
        }

        // Limpiar datos de sesión locales
        clearUserData()
        clearSessionTokens()
    }

    override suspend fun generateDeviceToken(): Resource<Boolean> {

        try {

            val deviceToken = getDeviceToken()

            // Si el device token ya existe, ignorar
            if (deviceToken != null){
                return Resource.Success(true)
            }
            val result = authApi.generateDeviceToken()
            val body = result.body()
            if(result.isSuccessful && body != null && body.ok){

                // Guardar device token
                saveDeviceToken(body.deviceToken)

                return Resource.Success(true)

            } else{
                val errorBody = result.errorBody()
                val error = ErrorParser.parseErrorMessage(errorBody)
                return Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en repository al generar device token "+ex.message)
            ex.printStackTrace()
            return Resource.Error(null)
        }
    }
}