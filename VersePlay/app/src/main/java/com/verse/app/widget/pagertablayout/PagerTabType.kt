package com.verse.app.widget.pagertablayout

import androidx.annotation.LayoutRes
import com.verse.app.R

enum class PagerTabType(@LayoutRes val layoutId: Int) {
    DEFAULT(R.layout.item_line_tab_layout_v2),
    COMMUNITY(R.layout.item_line_tab_layout_v2),
}