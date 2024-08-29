package com.verse.app.model.song

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 장르
 *
 * Created by jhlee on 2023-02-27
 */
@Serializable
data class GenreData(

    @SerialName("genreCd")
    val genreCd: String = "",

    @SerialName("genreNm")
    val genreNm: String = "",


): BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GenreData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GenreData
        return this.genreCd == asItem.genreCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GenreData
        return this == asItem
    }
}