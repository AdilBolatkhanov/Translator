package com.example.translate.network.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.common.data.TranslatedResponse
import com.example.translate.R

class TranslateAdapter(
    private val listener: TranslateItemClickListener
) : RecyclerView.Adapter<TranslateAdapter.ViewHolder>() {
    private val responseMessages = mutableListOf<TranslatedResponse>()
    private var positionOfItem = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.textMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun getItemCount(): Int = responseMessages.size

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = responseMessages[position].text
        val wrapper = ContextThemeWrapper(holder.itemView.context, R.style.BasePopupMenu)
        val popupMenu = PopupMenu(wrapper, holder.textView)
        popupMenu.inflate(R.menu.item_menu)

        val menuPopupHelper =
            MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, holder.textView)
        menuPopupHelper.setForceShowIcon(true)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.koshiry -> {
                    listener.copyToClipboard(positionOfItem)
                    true
                }
                R.id.bolisy -> {
                    listener.shareContent(positionOfItem)
                    true
                }
                R.id.oshiry -> {
                    listener.deleteResponseItems(positionOfItem)
                    true
                }
                R.id.unagan -> {
                    listener.addToFavorites(positionOfItem)
                    true
                }
                else -> false
            }
        }

        holder.textView.setOnClickListener {
            positionOfItem = position
            menuPopupHelper.show()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (responseMessages[position].isMe) {
            R.layout.me_bubble
        } else R.layout.bot_buble
    }

    fun setItems(list: List<TranslatedResponse>) {
        responseMessages.clear()
        responseMessages.addAll(list)
        notifyDataSetChanged()
    }
}

interface TranslateItemClickListener {
    fun copyToClipboard(positionOfItem: Int)
    fun shareContent(positionOfItem: Int)
    fun deleteResponseItems(positionOfItem: Int)
    fun addToFavorites(positionOfItem: Int)
}