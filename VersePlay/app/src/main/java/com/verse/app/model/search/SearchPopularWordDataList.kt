package com.verse.app.model.search

import android.os.Parcelable
import com.google.common.collect.Lists
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.TabPageType
import com.verse.app.livedata.ListLiveData
import com.verse.app.model.base.BaseModel
import com.verse.app.model.song.SongDataList
import com.verse.app.model.song.SongMainData
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SearchPopularWordDataList(
    var dataList: MutableList<PopKeywordData>? = null
) : Parcelable, BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SearchPopularWordDataList"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SearchPopularWordDataList
        return this.getViewType() == asItem.getViewType()
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SearchPopularWordDataList
        return this == asItem
    }
}