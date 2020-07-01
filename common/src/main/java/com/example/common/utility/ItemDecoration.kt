package com.example.common.utility

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration(
    private val insetVertical: Int,
    private val insetHorizontal: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0
        if (position == RecyclerView.NO_POSITION) {
            outRect.set(0, 0, 0, 0)
            return
        }

        outRect.left = insetHorizontal
        outRect.right = insetHorizontal
        outRect.top = if (position == 0) insetVertical * 2 else insetVertical
        outRect.bottom = if (position == itemCount - 1) insetVertical * 2 else insetVertical
    }
}
