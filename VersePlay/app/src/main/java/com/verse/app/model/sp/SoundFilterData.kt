package com.verse.app.model.sp

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

data class SoundFilterData(
    var icon: Int,
    var name: String? = "",
) : BaseModel() {

    var isSelected: MutableLiveData<Boolean>? = null
        get() {
            if (field == null) {
                field = MutableLiveData(false)
            }
            return field
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SoundFilterData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SoundFilterData
        return this.icon == asItem.icon
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SoundFilterData
        return this == asItem
    }
}