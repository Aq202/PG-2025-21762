package com.example.public_transport_app.ui.shared.session

import android.se.omapi.Session
import com.example.public_transport_app.data.entity.User


sealed class SessionState {
    data object UNITIALIZED : SessionState()
    class ADMIN_LOGGED(val user:User): SessionState()
    class AGENCY_ADMIN_LOGGED(val user:User): SessionState()
    class DRIVER_LOGGED(val user:User): SessionState()
    data object UNLOGGED: SessionState()
    data object DEFAULT: SessionState()
}

fun SessionState.isInitialized(): Boolean = this !is SessionState.UNITIALIZED

fun SessionState.hasAdminAccess() = this is SessionState.ADMIN_LOGGED

fun SessionState.hasAgencyAdminAccess() =
    this.hasAdminAccess() || this is SessionState.AGENCY_ADMIN_LOGGED

fun SessionState.hasDriverAccess() =
    this.hasAgencyAdminAccess() || this is SessionState.DRIVER_LOGGED