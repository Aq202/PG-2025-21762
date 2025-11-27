package com.example.public_transport_app.ui.imageSlider

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

/**
 * Adaptador para mostrar una lista de imágenes en un RecyclerView tipo slider.
 *
 * @param images Lista de URLs de imágenes.
 */
class ImageSliderAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    /**
     * ViewHolder que contiene la ImageView para cada imagen.
     */
    inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    /**
     * Crea la vista para cada elemento del slider.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ImageViewHolder(imageView)
    }

    /**
     * Carga la imagen en la vista correspondiente.
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.load(images[position]) {
            crossfade(true)
        }
    }

    /**
     * Retorna la cantidad de imágenes.
     */
    override fun getItemCount(): Int = images.size
}
