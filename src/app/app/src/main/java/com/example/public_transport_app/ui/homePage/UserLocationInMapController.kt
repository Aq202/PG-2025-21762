package com.example.public_transport_app.ui.homePage

import android.content.Context
import com.example.public_transport_app.utils.SensorDataProvider
import com.example.public_transport_app.utils.deviceUpdateLocationInterval
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope

class UserLocationInMapController(
    val mapController: MapController,
    val onLocationUpdated: ((lastKnownLatLng: LatLng, firstUpdate: Boolean) -> Unit)?
) {
    var lastKnownLatLng: LatLng? = null
        private set

    suspend fun initializeCameraPosition(context: Context){
        try {
            val initialMedition =
                SensorDataProvider.getCurrentSensorData(
                    context,
                    1000f,
                    30000
                )

            val latLng = LatLng(initialMedition.latitude, initialMedition.longitude)
            mapController.moveCamera(latLng)
            onLocationUpdated?.invoke(latLng, lastKnownLatLng == null)
            lastKnownLatLng = latLng

        }catch(_: Exception){
        }
    }

    suspend fun startLocationUpdates(context: Context){

        // Obtener continuamente la ubicación del dispositivo
        SensorDataProvider.getSensorDataFlow(
            context = context,
            intervalMs = deviceUpdateLocationInterval,
            accuracyThresholdMeters = null // No es relevante esperar una buena precisión
        ).collect { sensor ->
            val latLng = LatLng(sensor.latitude, sensor.longitude)

            // Si la cámara está centrada, seguir ubicación actual
            mapController.followUserIfNeeded(latLng)
            onLocationUpdated?.invoke(latLng, lastKnownLatLng == null)

            lastKnownLatLng = latLng

        }

    }
}