package com.example.public_transport_app.ui.updateRouteStopsView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.databinding.ItemSelectedStopsBinding
import com.example.public_transport_app.utils.replaceAllWithDiffUtil
import com.google.android.gms.maps.model.MarkerOptions

class UpdateRouteStopsAdapter(
    private val onRemoveClick: ((Stop) -> Unit)? = null
) : RecyclerView.Adapter<UpdateRouteStopsAdapter.StopViewHolder>() {

    private val stops: MutableList<Stop> = mutableListOf()

    inner class StopViewHolder(private val binding: ItemSelectedStopsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stop: Stop) {
            binding.textViewSelectedStopsStopName.text = stop.name
            binding.buttonSelectedStopsRemove.setOnClickListener {

                // Eliminar del adapter
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    stops.removeAt(position)
                    notifyItemRemoved(position)
                }

                // Callback cuando se elimina un elemento
                onRemoveClick?.invoke(stop)

            }

            binding.mapViewSelectedStopsLocation.onCreate(null)
            binding.mapViewSelectedStopsLocation.getMapAsync { googleMap ->
                googleMap.uiSettings.setAllGesturesEnabled(false)
                googleMap.moveCamera(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(stop.location, 15f)
                )

                googleMap.addMarker(
                    MarkerOptions()
                        .position(stop.location)
                        .title(stop.name)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val binding = ItemSelectedStopsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bind(stops[position])
    }

    override fun getItemCount(): Int = stops.size

    fun setStops(newStops: List<Stop>) {
        // Actualizar la lista de stops y notificar cambios al recyclerView
        stops.replaceAllWithDiffUtil(
            adapter = this,
            newList = newStops,
            areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id }
        )
    }

    fun getStops(): List<Stop> {
        return stops
    }
}