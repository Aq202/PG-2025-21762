package com.example.public_transport_app.ui.homePage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.public_transport_app.data.entity.RouteAndStop
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class BottomSheetViewModel: ViewModel() {

    private val _closeRouteDetailsEvent = MutableSharedFlow<Unit>()
    val closeRouteDetailsEvent = _closeRouteDetailsEvent.asSharedFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _selectedRouteAndStop = MutableSharedFlow<RouteAndStop>()
    val selectedRouteAndStop = _selectedRouteAndStop

    fun closeRouteDetails() {
        viewModelScope.launch {
            _closeRouteDetailsEvent.emit(Unit)
        }
    }

    fun setUserLocation(location: LatLng){
        viewModelScope.launch {
            _userLocation.value = location
        }
    }

    fun selectRoute(routeAndStop: RouteAndStop){
        viewModelScope.launch {
            _selectedRouteAndStop.emit(routeAndStop)
        }
    }
}