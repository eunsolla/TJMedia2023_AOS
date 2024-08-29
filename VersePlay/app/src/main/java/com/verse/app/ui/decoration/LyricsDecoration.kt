package com.verse.app.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Description : 부르기 Rv 하단 패딩 값
 *
 * Created by jhlee on 2023-04-01
 */
class LyricsDecoration() : RecyclerView.ItemDecoration() {

    @Override
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if(position == itemCount -1 ){
            outRect.bottom = parent.height
        }
    }
}