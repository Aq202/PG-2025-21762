package com.example.public_transport_app.ui.routeView

sealed class AgencyAdminPermissionState {
    object Default: AgencyAdminPermissionState()
    data class Error(val message: String): AgencyAdminPermissionState()
    object IsAgencyAdmin: AgencyAdminPermissionState()
    object NoPermission: AgencyAdminPermissionState()
}