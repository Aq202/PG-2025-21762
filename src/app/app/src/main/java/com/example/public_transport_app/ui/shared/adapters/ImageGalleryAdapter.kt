package com.example.public_transport_app.ui.shared.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.public_transport_app.R

/**
 * Adapter para mostrar una galería horizontal de imágenes a partir de URIs.
 * Permite eliminar imágenes con un botón "X".
 *
 * @param onItemDeleted Callback que se ejecuta cuando una imagen es eliminada.
 */
class ImageGalleryAdapter(
    private val onItemDeleted: (Uri) -> Unit
) : RecyclerView.Adapter<ImageGalleryAdapter.MediaViewHolder>() {

    // Lista mutable interna que se llenará con los métodos replaceAll y removeItemAt
    private val uris = mutableListOf<Uri>()

    /**
     * ViewHolder que contiene la imagen y el botón para eliminarla.
     */
    inner class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        // Infla el layout del item de la galería
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_gallery, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = uris[position]

        // Carga la imagen con Coil, con efecto crossfade y placeholders
        holder.imageView.load(uri) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.ic_menu_report_image)
        }

        // Ejecutar callback cuando se presiona el botón de eliminar imagen. Devolver uri a eliminar.
        holder.deleteButton.setOnClickListener {
            onItemDeleted(uri)
        }
    }

    override fun getItemCount(): Int = uris.size

    /**
     * Reemplaza todos los elementos del RecyclerView con una nueva lista de URIs.
     * Verifica la diferencia entre la lista previa y la actual para realizar cambios solo donde
     * es requerido.
     *
     * @param newUris Nueva lista de URIs para mostrar.
     */
    fun replaceAll(newUris: List<Uri>) {

        // Calcular las diferencias entre lista existente y nueva lista
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = uris.size
            override fun getNewListSize() = newUris.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // Comparar los items de ambas listas
                return uris[oldItemPosition] == newUris[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // Para Uri, si son iguales, su contenido también.
                return uris[oldItemPosition] == newUris[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        uris.clear()
        uris.addAll(newUris)

        // Notificar al adapter los cambios detectados entre ambas listas
        diffResult.dispatchUpdatesTo(this)
    }
}
