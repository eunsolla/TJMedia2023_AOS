package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetInquiryCategoryListResponse(
    val result: GetInquiryCategoryList,
) : BaseResponse()

@Serializable
data class GetInquiryCategoryList(
    val dataList: MutableList<GetInquiryCategoryListData>,
) : BasePaging()


@Serializable
data class GetInquiryCategoryListData(
    @SerialName("bctgMngCd")                                //1:1문의카테고리관리코드
    var bctgMngCd: String = "",

    @SerialName("bctgMngNm")                                 //1:1문의카테고리명
    var bctgMngNm: String = "",

    @SerialName("bctgTpCd")                                  //카테고리분류유형코드
    var bctgTpCd: String = "",

//    @SerialName("subDataList")                               //1:1문의 하위 카테고리 목록
//    var subDataList: MutableList<GetInquiryCategoryListSubData>

): BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetInquiryCategoryListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetInquiryCategoryListData
        return this.bctgMngCd == asItem.bctgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetInquiryCategoryListData
        return this == asItem
    }
}

@Serializable
data class GetInquiryCategoryListSubData(
    @SerialName("bctgMngCd")
    val bctgMngCd: String = "",    //1:1문의카테고리관리코드
    @SerialName("bctgMngNm")
    val bctgMngNm: String = "",    //1:1문의카테고리명
    @SerialName("bctgTpCd")
    val bctgTpCd: String = "",     //카테고리분류유형코드

) :BaseModel(){

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetInquiryCategoryListSubData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetInquiryCategoryListSubData
        return this.bctgMngCd == asItem.bctgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetInquiryCategoryListSubData
        return this == asItem
    }
}