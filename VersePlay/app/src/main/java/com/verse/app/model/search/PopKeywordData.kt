package com.verse.app.model.search

import android.os.Parcelable
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PopKeywordData(

    @SerialName("popRank")
    val popRank: String = "",

    @SerialName("popKeyword")
    val popKeyword: String = "",

) : Parcelable, BaseModel() {


    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "PopKeywordData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as PopKeywordData
        return this.popRank == asItem.popRank
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as PopKeywordData
        return this == asItem
    }
}