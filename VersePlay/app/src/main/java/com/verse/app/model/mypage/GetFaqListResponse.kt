package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFaqLastCategoryListResponse (
    @SerialName("result")
    val result: GetFaqLastCategoryListInfo = GetFaqLastCategoryListInfo(),
) : BaseResponse()

@Serializable
data class GetFaqLastCategoryListInfo(
    @SerialName("bctgMngNm")
    val bctgMngNm: String = "",    //타이틀
    val dataList: MutableList<GetFaqCategoryListLastData> = mutableListOf(),
) : BasePaging()

@Serializable
data class GetFaqCategoryListLastData(
    @SerialName("faqMngCd")
    val faqMngCd: String = "",    //FAQ카테고리관리코드
    @SerialName("faqTitle")
    val faqTitle: String = "",    //FAQ카테고리명
    @SerialName("faqContents")
    val faqContents: String = "",     //카테고리분류유형코드

) :BaseModel(){

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetFaqCategoryListLastData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetFaqCategoryListLastData
        return this.faqMngCd == asItem.faqMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetFaqCategoryListLastData
        return this == asItem
    }
}