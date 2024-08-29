package com.verse.app.widget.pagertablayout

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.verse.app.extension.dp

data class PagerTabItem(
        val title: String,
        @DrawableRes var icon: Int? = null,
        var pos: Int = 0, //default 0
        @Dimension var iconWidth: Int? = null,
        @Dimension var txtSize: Int = 16.dp,
        @ColorInt var txtColor: Int = Color.YELLOW,
        @ColorInt var disableTxtColor: Int = Color.WHITE,
        @ColorInt var bgColor: Int = Color.TRANSPARENT,
        @ColorInt var disableBgColor: Int = Color.TRANSPARENT,
        var isChangeTextStyle: Boolean = false,
        var isVisible: Boolean = true,
        var isDot: Boolean = true,
) {
    var isSelected: MutableLiveData<Boolean>? = null
        get() {
            if (field == null) {
                field = MutableLiveData(false)
            }
            return field
        }
}