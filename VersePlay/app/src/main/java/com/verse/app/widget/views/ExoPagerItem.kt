package com.verse.app.widget.views

import android.os.Parcelable
import androidx.annotation.StringRes
import com.verse.app.contants.ExoPageType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExoPagerItem(
    @StringRes
    val name: Int? = -1,
    val type: ExoPageType,
    val isFirstPlay: Boolean,
    var pos: Int = -1,
) : Parcelable