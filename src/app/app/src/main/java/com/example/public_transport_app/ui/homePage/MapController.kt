package com.example.public_transport_app.ui.homePage

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.annotation.RequiresPermission
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.NearbyRun
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.utils.BitMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import kotlin.math.*
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapController(val context: Context, val layoutInflater: LayoutInflater) {

    private var map: GoogleMap? = null
    private val busDesiredWidth = 170

    private val stopMarkerDesiredWidth = 80
    private val busMarkers: MutableMap<String, Marker> = mutableMapOf()

    private val routeStops: MutableMap<String, Marker> = mutableMapOf()
    private val busCurrentLocation: MutableMap<String, LatLng> = mutableMapOf()
    private val busFlipped: MutableMap<String, Boolean> = mutableMapOf()

    private var routeLine: Polyline? = null

    private var onBusClickListener: ((NearbyRun) -> Unit)? = null
    private var onCameraMovedListener: (() -> Unit)? = null

    private var cameraObjective: CameraObjective = CameraObjective.CurrentLocation

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    fun configureMap(){
        map?.apply{
            this.isMyLocationEnabled = true
            this.uiSettings.isMyLocationButtonEnabled = false
        }
    }

    /**
     * Calcula el bearing (ángulo) geográfico entre dos puntos (0 grad = norte)
     */
    private fun getBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))

        // Normalizar entre 0 y 360
        return ((bearing + 360) % 360).toFloat()
    }

    /**
     * Interpola suavemente entre dos bearings, eligiendo siempre el camino más corto.
     */
    private fun interpolateBearing(start: Float, end: Float, fraction: Float): Float {
        val diff = ((end - start + 540) % 360) - 180 // normaliza a [-180,180]
        return (start + diff * fraction + 360) % 360
    }


    fun addBus(nearbyRun: NearbyRun, location: LatLng) {
        // Obtener bitmap original
        val bitmap = BitMap.getBitMapFromVector(context, R.drawable.ic_bus) ?: return

        // Calcular scale factor y altura proporcional
        val scaledBitmap = BitMap.scaleBitMap(bitmap, busDesiredWidth)

        // Crear marker
        val marker = map?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                .anchor(0.5f, 0.5f)
                .flat(true) // permite rotación
        )

        marker?.let {
            it.tag = MarkerType.Bus(nearbyRun)
            busMarkers[nearbyRun.runId] = it
        }

        busCurrentLocation[nearbyRun.runId] = location
        busFlipped[nearbyRun.runId] = false
    }

    fun addStop(stop: Stop) {
        // Obtener bitmap original
        val bitmap = BitMap.getBitMapFromVector(context, R.drawable.ic_stop_marker) ?: return

        // Calcular scale factor y altura proporcional
        val scaledBitmap = BitMap.scaleBitMap(bitmap, stopMarkerDesiredWidth)

        // Crear marker
        val marker = map?.addMarker(
            MarkerOptions()
                .position(stop.location)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                .anchor(0.5f, 1f)
                .flat(true) // permite rotación
        )

        marker?.let{
            it.tag = MarkerType.RouteStop(stop)
            routeStops[stop.id] = it
        }

    }

    fun updateBusLocation(nearbyRun: NearbyRun, newLocation: LatLng) {
        val runId = nearbyRun.runId
        val marker = busMarkers[runId] ?: return
        val start = busCurrentLocation[runId] ?: return // Ignorar, no existe el marcador

        // Actualizar el tag
        marker.tag = MarkerType.Bus(nearbyRun)

        // Si start y newLocation son iguales, no actualizar
        if (start.latitude == newLocation.latitude && start.longitude == newLocation.longitude) {
            return
        }

        // Determinar flip horizontal
        val shouldFlip = newLocation.longitude < start.longitude
        val isFlipped = busFlipped[runId] ?: false

        if (shouldFlip != isFlipped) {
            val bitmap = if (shouldFlip) {
                BitMap.getFlippedBitmap(context, R.drawable.ic_bus)
            } else {
                BitMap.getBitMapFromVector(context, R.drawable.ic_bus)
            } ?: return

            // Escalar bitmap
            val scaledBitmap = BitMap.scaleBitMap(bitmap, busDesiredWidth)

            marker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
            busFlipped[runId] = shouldFlip
        }

        // Calcular bearing
        val startBearing = marker.rotation
        var endBearing = getBearing(start, newLocation) - 90f
        if (busFlipped[runId] == true) endBearing = (endBearing + 180f) % 360

        // Animación de posición y rotación
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                val lat = start.latitude + (newLocation.latitude - start.latitude) * fraction
                val lng = start.longitude + (newLocation.longitude - start.longitude) * fraction
                marker.position = LatLng(lat, lng)
                marker.rotation = interpolateBearing(startBearing, endBearing, fraction)
            }
        }
        valueAnimator.start()
        busCurrentLocation[runId] = newLocation
    }

    /**
     * Elimina los marcadores de buses que no están en la lista de ID proporcionadas.
     */
    fun removeBusesNotInList(runIds: List<String>) {
        val idsToRemove = busMarkers.keys - runIds.toSet()
        idsToRemove.forEach { id ->
            busMarkers[id]?.remove()
            busMarkers.remove(id)
            busCurrentLocation.remove(id)
            busFlipped.remove(id)
        }
    }

    /**
     * Elimina todos los marcadores de buses.
     */
    fun removeAllBuses(){
        busMarkers.forEach { (_, marker) ->
            marker.remove()
        }
        busMarkers.clear()
        busCurrentLocation.clear()
        busFlipped.clear()
    }

    /**
     * Verifica si un marcador de bus existe para el runId dado.
     */
    fun hasBus(runId: String): Boolean {
        return busMarkers.containsKey(runId)
    }

    fun setMap(googleMap: GoogleMap) {
        map = googleMap
        initMapListeners()
        setMarkerInfoWindowAdapter()
    }

    fun initMapListeners(){
        // Escuchar clic en marcadores
        map?.setOnMarkerClickListener { marker ->

            when(val markerType = marker.tag){
                is MarkerType.Bus -> {
                    if(onBusClickListener != null){
                        onBusClickListener!!(markerType.nearbyRun)
                    }
                }
            }
            return@setOnMarkerClickListener false
        }

        // Escuchar movimiento de cámara
        map?.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE &&
                onCameraMovedListener != null) {
                onCameraMovedListener!!()
            }
        }

        // Manejar zoom y estilo personalizado de labels
        map?.setOnCameraIdleListener {
            val zoom = map?.cameraPosition?.zoom ?: return@setOnCameraIdleListener

            if (zoom <= 16f) {
                // Zoom lejos → mapa limpio
                map?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_low_zoom)
                )
            } else {
                // Zoom cerca → labels normales
                map?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_default)
                )
            }
        }
    }

    private fun setMarkerInfoWindowAdapter(){
        map?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // dejamos que decida getInfoContents
            }

            override fun getInfoContents(marker: Marker): View? {
                val tag = marker.tag
                if (tag !is MarkerType.RouteStop) {
                    // No mostrar ventana para otros tipos de marker
                    return null
                }

                val view = layoutInflater.inflate(R.layout.route_stop_info_window, null)
                val textView = view.findViewById<TextView>(R.id.textView_routeStopInfo)

                textView.text = tag.stop.name

                return view
            }
        })


    }

    private fun drawLine(points: List<LatLng>): Polyline?{
        return map?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(Color.BLUE)
                .width(8f)
        )
    }

    /**
     * Dibuja la línea del recorrido en el mapa usando una lista de LatLng.
     * @param points lista de LatLng que conforman la línea
     */
    fun drawRouteLine(points: List<LatLng>) {
        removeRouteLine()
        routeLine = drawLine(points)

    }

    /**
     * Elimina la línea del mapa del recorrido.
     */
    fun removeRouteLine(){
        routeLine?.remove()
    }

    fun removeRouteStops(){
        for (stop in routeStops.values){
            stop.remove()
        }

        routeStops.clear()
    }

    fun setOnBusClickListener(onBusClickListener: (NearbyRun) -> Unit) {
        this.onBusClickListener = onBusClickListener
    }

    fun setOnCameraMovedListener(onCameraMovedListener:() -> Unit){
        this.onCameraMovedListener = onCameraMovedListener
    }

    fun moveCamera(latLng: LatLng){
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    fun animateMoveCamera(latLng: LatLng, zoom: Float = 16f){
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun centerCameraInUserLocation(latLng: LatLng){
        animateMoveCamera(latLng)
        cameraObjective = CameraObjective.CurrentLocation
    }

    fun centerCameraInBusLocation(busLocation: LatLng){
        // Centrar cámara en el bus
        animateMoveCamera(busLocation)

        // Comenzar a seguir el recorrido con la cámara
        cameraObjective = CameraObjective.FocusedBus
    }

    /**
     * Mueve la cámara a la ubicación del usuario, solo si el objetivo de la cámara es CurrentLocation.
     */
    fun followUserIfNeeded(userLocation: LatLng){
        if (cameraObjective is CameraObjective.CurrentLocation) {
            animateMoveCamera(userLocation)
        }
    }

    /**
     * Mueve la cámara a la ubicación del bus, si el objetivo de la cámara es el bus.
     */
    fun followBusIfNeeded(busLocation: LatLng){
        if (cameraObjective is CameraObjective.FocusedBus){
            animateMoveCamera(busLocation)
        }
    }

    fun isCameraFollowingBus():Boolean{
        return cameraObjective is CameraObjective.FocusedBus
    }

    fun resetCameraObjective(){
        cameraObjective = CameraObjective.NoObjective
    }

    fun clearMap(){
        removeAllBuses()
        removeRouteStops()
        removeRouteLine()
        map?.clear()
    }
}

