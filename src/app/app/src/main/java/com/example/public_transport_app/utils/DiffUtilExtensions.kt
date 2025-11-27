package com.example.public_transport_app.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Reemplaza la lista de manera eficiente usando DiffUtil y notifica al adapter solo los cambios necesarios.
 *
 * @param newList La nueva lista a mostrar.
 */
fun <T> MutableList<T>.replaceAllWithDiffUtil(
    adapter: RecyclerView.Adapter<*>,
    newList: List<T>,
    areItemsTheSame: (oldItem: T, newItem: T) -> Boolean = { o, n -> o == n },
    areContentsTheSame: (oldItem: T, newItem: T) -> Boolean = { o, n -> o == n }
) {
    val diffCallback = object : DiffUtil.Callback() {
        override fun getOldListSize() = this@replaceAllWithDiffUtil.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            areItemsTheSame(this@replaceAllWithDiffUtil[oldItemPosition], newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            areContentsTheSame(this@replaceAllWithDiffUtil[oldItemPosition], newList[newItemPosition])
    }

    val diffResult = DiffUtil.calculateDiff(diffCallback)
    clear()
    addAll(newList)
    diffResult.dispatchUpdatesTo(adapter)
}