package com.example.favorites.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.common.data.Favorites
import com.example.favorites.R
import java.util.*

class FavoriteAdapter(
    private val listener: FavoriteClickListener
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {
    private var favorites = mutableListOf<Favorites>()

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var first: TextView = v.findViewById(R.id.first)
        var second: TextView = v.findViewById(R.id.second)
        var del: ImageView = v.findViewById(R.id.del_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.favorite_item, parent, false
            )
        )
    }

    override fun getItemCount(): Int = favorites.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.first.text = favorites[position].first
        holder.second.text = favorites[position].second
        holder.del.setOnClickListener {
            listener.deleteClickListener(holder.adapterPosition)
        }
    }

    val simpleCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(favorites, i, i + 1)
                        notifyItemMoved(i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(favorites, i, i - 1)
                        notifyItemMoved(i, i - 1)
                    }
                }
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) = Unit
        }

    fun setItems(list: List<Favorites>) {
        favorites.clear()
        favorites.addAll(list)
        notifyDataSetChanged()
    }
}

interface FavoriteClickListener {
    fun deleteClickListener(position: Int)
}