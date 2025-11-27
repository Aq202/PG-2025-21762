package com.example.public_transport_app.ui.mainBottomView

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.EmbeddedRoute
import com.example.public_transport_app.data.entity.ClosestStopByRoute
import com.example.public_transport_app.data.entity.RouteAndStop
import com.example.public_transport_app.databinding.ItemDistanceListBinding

class NearbyRoutesAdapter(
    private val context: Context,
    private val items: List<ClosestStopByRoute>,
    private val onItemClick: (RouteAndStop) -> Unit // Callback al hacer click
) : RecyclerView.Adapter<NearbyRoutesAdapter.NearbyRouteViewHolder>() {

    inner class NearbyRouteViewHolder(val binding: ItemDistanceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ClosestStopByRoute) {
            binding.textViewItemDistListDistance.text =
                context.getString(R.string.distance_in_metters, item.distance.toInt())
            binding.textViewItemDistListName.text = item.route.name

            // Setear el click listener
            binding.root.setOnClickListener {
                onItemClick(RouteAndStop(
                    item.route,
                    item.stop
                ))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyRouteViewHolder {
        val binding = ItemDistanceListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NearbyRouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NearbyRouteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
