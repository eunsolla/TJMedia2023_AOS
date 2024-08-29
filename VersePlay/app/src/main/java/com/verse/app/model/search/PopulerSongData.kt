package com.verse.app.model.search

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

data class PopulerSongData(
    var imgFile: String,
    var songName: String,
    var singer: String,
    var likeIcon: String
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "PopulerSongData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as PopulerSongData
        return this.imgFile == asItem.imgFile
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as PopulerSongData
        return this == asItem
    }
}