package com.example.public_transport_app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.time.Instant
import kotlin.coroutines.resume

/**
 * Representa una lectura de ubicación con metadatos.
 *
 * @property latitude Latitud en grados decimales.
 * @property longitude Longitud en grados decimales.
 * @property speedMps Velocidad en metros por segundo.
 * @property timestamp Marca de tiempo de la lectura.
 * @property accuracy Precisión reportada por el proveedor (metros).
 */
data class SensorData(
    val latitude: Double,
    val longitude: Double,
    val speedMps: Float,
    val timestamp: Instant,
    val accuracy: Float
)

/**
 * Proveedor de datos de ubicación.
 *
 * Incluye métodos para obtener flujo continuo y lectura puntual con fallback.
 */
object SensorDataProvider {

    /**
     * Flujo continuo de ubicaciones con filtro de precisión y fallback.
     *
     * @param context Contexto de la app.
     * @param intervalMs Intervalo deseado entre actualizaciones (ms).
     * @param accuracyThresholdMeters Umbral para filtrar lecturas según precisión (metros).
     * @param emitLowAccuracyAfterMs Tiempo máximo para emitir lectura baja precisión (fallback).
     * @param looper Looper para callbacks (por defecto main).
     */
    @SuppressLint("MissingPermission")
    fun getSensorDataFlow(
        context: Context,
        intervalMs: Long = 2000L,
        accuracyThresholdMeters: Float? = 10f,
        emitLowAccuracyAfterMs: Long = 15_000L,
        looper: Looper = Looper.getMainLooper(),
    ): Flow<SensorData> = callbackFlow {
        val fused = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        )
            .setWaitForAccurateLocation(true)
            .build()

        var lastSentTime = System.currentTimeMillis()

        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val loc = result.lastLocation ?: return

                val sensor = SensorData(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    speedMps = loc.speed,
                    timestamp = Instant.now(),
                    accuracy = loc.accuracy
                )

                // No hay umbral de precisión
                if (accuracyThresholdMeters == null) {
                    trySend(sensor)
                    return
                }

                val now = System.currentTimeMillis()

                // Si la precisión es buena, enviar inmediatamente
                if (loc.accuracy <= accuracyThresholdMeters) {
                    trySend(sensor)
                    lastSentTime = now
                    return
                }

                // 3. Enviar de todas formas si pasó el tiempo de espera
                if (now - lastSentTime >= emitLowAccuracyAfterMs) {
                    trySend(sensor)
                    lastSentTime = now
                }
            }
        }

        fused.requestLocationUpdates(locationRequest, callback, looper)

        awaitClose {
            fused.removeLocationUpdates(callback)
        }
    }

    /**
     * Obtiene una única ubicación precisa, devuelve la mejor recibida si no alcanza umbral.
     *
     * @param context Contexto de la app.
     * @param accuracyThresholdMeters Umbral para precisión deseada (metros).
     * @param timeoutMs Tiempo máximo para esperar (ms).
     * @param looper Looper para callbacks (por defecto main).
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentSensorData(
        context: Context,
        accuracyThresholdMeters: Float = 10f,
        timeoutMs: Long = 7000L,
        looper: Looper = Looper.getMainLooper()
    ): SensorData {
        var bestCandidate: SensorData? = null
        return try {
            withTimeout(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    val fused = LocationServices.getFusedLocationProviderClient(context)

                    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        0
                    )
                        .setWaitForAccurateLocation(true)
                        .build()

                    val callback = object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            val loc = result.lastLocation ?: return
                            val candidate = SensorData(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                speedMps = loc.speed,
                                timestamp = Instant.now(),
                                accuracy = loc.accuracy
                            )

                            if (bestCandidate == null || candidate.accuracy < bestCandidate!!.accuracy) {
                                bestCandidate = candidate
                            }

                            if (candidate.accuracy <= accuracyThresholdMeters) {
                                if (cont.isActive) {
                                    cont.resume(candidate)
                                }
                                fused.removeLocationUpdates(this)
                            }
                        }
                    }

                    fused.requestLocationUpdates(locationRequest, callback, looper)

                    cont.invokeOnCancellation {
                        fused.removeLocationUpdates(callback)
                    }
                }
            }
        } catch (e: Exception) {
            println("Diego: Error al obtener ubicación individual: ${e.toString()}")
            bestCandidate ?: throw IllegalStateException("No se obtuvo ubicación válida")
        }
    }
}
