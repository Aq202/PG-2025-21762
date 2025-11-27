package com.example.public_transport_app.ui.infoDialog

import android.content.Context
import com.example.public_transport_app.R

class SuccessDialog(context: Context) : InfoDialog(context) {
    override fun getIconResId(): Int? = R.drawable.ic_success
}

fun Context.showSuccessDialog(
    title: String,
    message: String,
    buttonText: String = "OK",
    callback: (() -> Unit)? = null
) {
    SuccessDialog(this).show(title, message, buttonText, callback)
}