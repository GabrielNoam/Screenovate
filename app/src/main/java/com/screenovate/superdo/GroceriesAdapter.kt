package com.screenovate.superdo

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.android.synthetic.main.grocery_item_row.view.*

/**
 * GroceriesAdapter
 * @author Gabriel Noam
 */
class GroceriesAdapter(private var list: List<Grocery>? = listOf()) : Adapter<ViewHolder>() {

    override fun getItemCount(): Int = list?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.grocery_item_row, parent, false)
        return GroceryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHolder = holder as GroceryViewHolder
        viewHolder.bind(position, list?.get(position))
    }

    class GroceryViewHolder(itemView: View) : ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(position: Int, grocery: Grocery?) = with(itemView) {
            grocery?.apply {
                nameTextView.text = name
                weightTextView.text = weight
                iconTextView.text = id.toString()
                val background = iconTextView.background
                val gradientDrawable: GradientDrawable = background as GradientDrawable
                val color: Int = Color.parseColor(bagColor)
                gradientDrawable.setColor(color)
            }
        }
    }
}