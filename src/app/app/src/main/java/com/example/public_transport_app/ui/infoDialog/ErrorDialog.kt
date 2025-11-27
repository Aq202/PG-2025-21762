package com.example.public_transport_app.ui.infoDialog

import android.content.Context
import com.example.public_transport_app.R

class ErrorDialog(context: Context) : InfoDialog(context) {
    override fun getIconResId(): Int? = R.drawable.ic_error
}

fun Context.showErrorDialog(
    title: String,
    message: String,
    buttonText: String = "OK",
    callback: (() -> Unit)? = null
) {
    ErrorDialog(this).show(title, message, buttonText, callback)
}