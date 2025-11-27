package com.example.public_transport_app.ui.selectRouteStopsView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.databinding.ItemStopsOptionsBinding
import com.example.public_transport_app.utils.replaceAllWithDiffUtil
import com.google.android.gms.maps.model.MarkerOptions

class SelectRouteStopsAdapter(
    private val onAddClick: ((Stop) -> Unit)? = null
) : RecyclerView.Adapter<SelectRouteStopsAdapter.StopViewHolder>() {

    private val stops: MutableList<Stop> = mutableListOf()

    inner class StopViewHolder(private val binding: ItemStopsOptionsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stop: Stop) {
            binding.textViewSelectedStopsStopName.text = stop.name
            binding.buttonSelectedStopsAdd.setOnClickListener {

                // Callback cuando se selecciona un elemento
                onAddClick?.invoke(stop)

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
        val binding = ItemStopsOptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
}