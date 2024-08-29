package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetInquiryListResponse(
    var result: GetInquiryList,
) : BaseResponse()

@Serializable
data class GetInquiryList(
    val dataList: MutableList<GetInquiryListData> = mutableListOf(),
) : BasePaging()

@Serializable
data class GetInquiryListData(
    @SerialName("csMngCd")                                 //문의내역관리코드
    val csMngCd: String = "",

    @SerialName("stNm")                                    //문의요청상태
    val stNm: String = "",

    @SerialName("csContent")                               //문의내용
    val csContent: String = "",

    @SerialName("csReqDt")                                 //문의요청일시
    val csReqDt: String = "",
): BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetInquiryListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetInquiryListData
        return this.csMngCd == asItem.csMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetInquiryListData
        return this == asItem
    }

}
