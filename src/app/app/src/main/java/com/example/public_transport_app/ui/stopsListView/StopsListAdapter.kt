package com.example.public_transport_app.ui.stopsListView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.public_transport_app.data.entity.Stop
import com.example.public_transport_app.databinding.ItemStopsListBinding
import com.example.public_transport_app.utils.replaceAllWithDiffUtil
import com.google.android.gms.maps.model.MarkerOptions

class StopsListAdapter(
) : RecyclerView.Adapter<StopsListAdapter.StopViewHolder>() {

    private val stops: MutableList<Stop> = mutableListOf()

    inner class StopViewHolder(private val binding: ItemStopsListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stop: Stop) {
            binding.textViewStopsListStopName.text = stop.name

            binding.mapViewStopsListLocation.onCreate(null)
            binding.mapViewStopsListLocation.getMapAsync { googleMap ->
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
        val binding = ItemStopsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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