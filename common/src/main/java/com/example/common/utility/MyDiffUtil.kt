package com.example.common.utility

import androidx.recyclerview.widget.DiffUtil
import com.example.common.data.ResponseClass

class MyDiffUtil : DiffUtil.Callback() {
    private var oldList = emptyList<ResponseClass>()
    private var newList = emptyList<ResponseClass>()

    fun setItems(oldList: List<ResponseClass>, newList: List<ResponseClass>) {
        this.oldList = oldList
        this.newList = newList
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].responseId == newList[newItemPosition].responseId

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return oldList[oldPosition] == newList[newPosition]
    }
}
