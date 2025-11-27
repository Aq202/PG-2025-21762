package com.example.public_transport_app.ui.homePage

sealed class CameraObjective {

    object CurrentLocation : CameraObjective()
    object FocusedBus : CameraObjective()
    object NoObjective : CameraObjective()
}