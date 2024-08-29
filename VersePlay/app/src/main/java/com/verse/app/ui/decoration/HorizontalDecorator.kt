package com.verse.app.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * Description : recycler line
 *
 * Created by jhlee on 2023-02-08
 */
class HorizontalDecorator(private val rightPadding: Int) : RecyclerView.ItemDecoration() {

    @Override
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (itemCount != position) {
            outRect.right = (rightPadding / 2)
        } else {
            outRect.right = rightPadding
        }
    }
}