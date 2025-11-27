package com.example.public_transport_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale

object BitMap {

    fun getBitMapFromVector(context: Context, resId: Int): Bitmap?{
        return AppCompatResources.getDrawable(context, resId)?.toBitmap()
    }

    /**
     * Devuelve el bitmap volteado horizontalmente.
     */
    fun getFlippedBitmap(context: Context, resId: Int): Bitmap? {
        val bitmap = getBitMapFromVector(context, resId) ?: return null
        val matrix = Matrix().apply { preScale(-1f, 1f) } // voltear horizontal
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Escalar bitmap a tamaño deseado.
     */
    fun scaleBitMap(bitmap: Bitmap, desiredWidth: Int): Bitmap {
        val scaleFactor = desiredWidth.toFloat() / bitmap.width
        val desiredHeight = (bitmap.height * scaleFactor).toInt()
        val scaledBitmap = bitmap.scale(desiredWidth, desiredHeight, false)
        return scaledBitmap
    }
}