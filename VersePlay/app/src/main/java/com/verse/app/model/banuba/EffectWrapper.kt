package com.verse.app.model.banuba

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

data class EffectWrapper(
    var effect: Int,
    var isDownloading: Boolean = false,
    var isSelected: Boolean = false

) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "EffectWrapper"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as EffectWrapper
        return this.effect == asItem.effect
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as EffectWrapper
        return this == asItem
    }
}