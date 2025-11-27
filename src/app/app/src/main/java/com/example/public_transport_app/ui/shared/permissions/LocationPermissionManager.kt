package com.example.public_transport_app.ui.shared.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.public_transport_app.R

/**
 * Maneja solicitudes de permisos de ubicación con mensajes racionales personalizados.
 */
class LocationPermissionManager(
    private val context: Context,
    caller: ActivityResultCaller
) {
    private var permissionCallback: ((Boolean) -> Unit)? = null

    private val fineLocationLauncher: ActivityResultLauncher<String> =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionCallback?.invoke(isGranted)
        }

    private val coarseLocationLauncher: ActivityResultLauncher<String> =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionCallback?.invoke(isGranted)
        }

    private val backgroundLocationLauncher: ActivityResultLauncher<String> =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionCallback?.invoke(isGranted)
        }

    fun requestApproximateLocation(
        rationaleTitle: String,
        rationaleMessage: String,
        onResult: (Boolean) -> Unit
    ) {
        requestPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            coarseLocationLauncher,
            rationaleTitle,
            rationaleMessage,
            onResult
        )
    }

    fun requestPreciseLocation(
        rationaleTitle: String,
        rationaleMessage: String,
        onResult: (Boolean) -> Unit
    ) {
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            fineLocationLauncher,
            rationaleTitle,
            rationaleMessage,
            onResult
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundLocation(
        rationaleTitle: String,
        rationaleMessage: String,
        onResult: (Boolean) -> Unit
    ) {

        requestPermission(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            backgroundLocationLauncher,
            rationaleTitle,
            rationaleMessage,
            onResult
        )
    }


    private fun requestPermission(
        permission: String,
        launcher: ActivityResultLauncher<String>,
        rationaleTitle: String,
        rationaleMessage: String,
        onResult: (Boolean) -> Unit
    ) {
        permissionCallback = onResult

        when {
            hasPermission(permission) -> onResult(true)

            shouldShowRationale(permission) -> {
                showRationale(rationaleTitle, rationaleMessage) {
                    launcher.launch(permission)
                }
            }

            else -> launcher.launch(permission)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRationale(permission: String): Boolean {
        return (context as? Activity)?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: false
    }

    private fun showRationale(title: String, message: String, onAccept: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.accept)) { _, _ -> onAccept() }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
}
