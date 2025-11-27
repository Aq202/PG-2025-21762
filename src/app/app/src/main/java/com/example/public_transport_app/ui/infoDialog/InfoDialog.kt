package com.example.public_transport_app.ui.infoDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.example.public_transport_app.R

open class InfoDialog(private val context: Context) {

    private var dialog: Dialog? = null

    protected open fun getIconResId(): Int? = null

    fun show(
        title: String,
        message: String,
        buttonText: String = "OK",
        callback: (() -> Unit)? = null
    ) {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

            val view = LayoutInflater.from(context).inflate(R.layout.dialog_info, null, false)
            setContentView(view)

            // Configurar los elementos del diálogo
            view.findViewById<TextView>(R.id.dialog_title).text = title
            view.findViewById<TextView>(R.id.dialog_message).text = message
            view.findViewById<Button>(R.id.dialog_button).apply {
                text = buttonText
                setOnClickListener {
                    callback?.invoke()
                    dismiss()
                }
            }

            // Colocar icono según corresponda
            view.findViewById<ImageView>(R.id.dialog_icon)
                .setImageResource(getIconResId() ?: 0)

            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}