package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetTermsListResponse(
    @SerialName("result")
    val result: GetTermsListInfo,
) : BaseResponse()

@Serializable
data class GetTermsListInfo(
    val dataList: MutableList<GetTermsListData> = mutableListOf(),
): BasePaging()

@Serializable
data class GetTermsListData(
    @SerialName("bctgMngCd")
    val bctgMngCd: String = "",    //약관카테고리관리코드
    @SerialName("bctgMngNm")
    val bctgMngNm: String = "",    //약관카테고리명
    @SerialName("bctgTpCd")
    val bctgTpCd: String = "",     //카테고리분류유형코드
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetTermsListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetTermsListData
        return this.bctgMngCd == asItem.bctgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetTermsListData
        return this == asItem
    }

}
