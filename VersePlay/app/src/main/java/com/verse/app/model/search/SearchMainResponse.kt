package com.verse.app.model.search

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SearchMainResponse(
    val result: SearchMainInfo = SearchMainInfo(),
) : BaseResponse()

@Serializable
data class SearchMainInfo(
    var popKeywordList: List<PopKeywordData>? = listOf(), // 인기검색어 목록
    var nowLoveSongList: List<NowLoveSongData>? = listOf() // 지금사람받는노래 목록
) : BaseModel() {


    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SearchMainInfo"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as SearchMainInfo
        return this == asItem
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as SearchMainInfo
        return this == asItem
    }


}
