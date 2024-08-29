package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFaqCategoryListResponse (
    @SerialName("result")
    val result: GetFaqCategoryList = GetFaqCategoryList(),
) : BaseResponse()

@Serializable
data class GetFaqCategoryList(
    @SerialName("dataList")
    val dataList: MutableList<GetFaqCategoryListData> = mutableListOf(),
) : BasePaging()

@Serializable
data class GetFaqCategoryListData(
    @SerialName("bctgMngCd")                                //FAQ카테고리관리코드
    var bctgMngCd: String = "",

    @SerialName("bctgMngNm")                                 //FAQ카테고리명
    var bctgMngNm: String = "",

    @SerialName("bctgTpCd")                                  //카테고리분류유형코드
    var bctgTpCd: String = "",

    @SerialName("subDataList")                               //FAQ 하위 카테고리 목록
    var subDataList: MutableList<GetFaqCategoryListSubData> = mutableListOf(),

) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetFaqCategoryListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetFaqCategoryListData
        return this.bctgMngCd == asItem.bctgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetFaqCategoryListData
        return this == asItem
    }
}

@Serializable
data class GetFaqCategoryListSubData(
    @SerialName("bctgMngCd")
    val bctgMngCd: String = "",    //FAQ카테고리관리코드
    @SerialName("bctgMngNm")
    val bctgMngNm: String = "",    //FAQ카테고리명
    @SerialName("bctgTpCd")
    val bctgTpCd: String = "",     //카테고리분류유형코드

) :BaseModel(){

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetFaqCategoryListSubData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetFaqCategoryListSubData
        return this.bctgMngCd == asItem.bctgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetFaqCategoryListSubData
        return this == asItem
    }
}