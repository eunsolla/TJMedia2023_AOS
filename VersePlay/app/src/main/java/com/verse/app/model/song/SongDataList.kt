package com.verse.app.model.song

import android.os.Parcelable
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.TabPageType
import com.verse.app.model.base.BaseModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Description : 신곡,인기곡
 *
 * Created by jhlee on 2023-02-27
 */
@Parcelize
@Serializable
data class SongDataList(
    var dataList: MutableList<SongMainData>? = null
) : Parcelable, BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SongDataList"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SongDataList
        return this.getViewType() == asItem.getViewType()
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SongDataList
        return this == asItem
    }
}