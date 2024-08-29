package com.verse.app.ui.decoration

import android.R
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * Description : recycler line
 *
 * Created by jhlee on 2023-02-08
 */
class GridDecorator(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean, //true : 바깥 포함 , false : 안쪽만
) : RecyclerView.ItemDecoration() {

    @Override
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val column: Int = position % spanCount
        val itemCount = state.itemCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing// item bottom
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }

}