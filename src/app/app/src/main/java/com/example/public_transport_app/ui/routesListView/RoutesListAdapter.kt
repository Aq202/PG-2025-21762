package com.example.public_transport_app.ui.routesListView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.public_transport_app.R
import com.example.public_transport_app.data.entity.Route
import com.google.android.material.card.MaterialCardView

class RoutesListAdapter(
    private val routes: List<Route>,
    private val onClick: (Route) -> Unit
) : RecyclerView.Adapter<RoutesListAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(route: Route) {
            val cardView = view.findViewById<MaterialCardView>(R.id.cardView_accountItemTemplate_parentContainer)
            val textView = cardView.getChildAt(0) as TextView
            textView.text = route.name

            cardView.setOnClickListener {
                onClick(route)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_single_list, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    override fun getItemCount(): Int = routes.size
}
