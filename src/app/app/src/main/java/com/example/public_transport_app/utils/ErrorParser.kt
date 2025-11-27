package com.example.public_transport_app.utils

import okhttp3.ResponseBody
import org.json.JSONObject

object ErrorParser {

    /**
     * Extrae el mensaje de error del cuerpo de la respuesta.
     * @param errorBody El cuerpo del error devuelto por la API.
     * @return El mensaje extraído o un mensaje por defecto.
     */
    fun parseErrorMessage(errorBody: ResponseBody?): String? {
        return try {
            val errorJson = errorBody?.string()
            println(errorJson)
            val json = JSONObject(errorJson ?: "")
            json.optString("message").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            println("Error al obtener mensaje de error: $e")
            null
        }
    }
}