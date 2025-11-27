package com.example.public_transport_app.ui.shared.loadingDialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.core.graphics.drawable.toDrawable
import com.example.public_transport_app.R

class LoadingDialog:DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog?.setCancelable(false)
        return inflater.inflate(R.layout.fragment_loading_dialog,container, false)
    }
}