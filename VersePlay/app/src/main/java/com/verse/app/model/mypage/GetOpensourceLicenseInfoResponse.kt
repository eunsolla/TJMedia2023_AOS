package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetOpensourceLicenseInfoResponse(
    @SerialName("result")
    val result: GetOpensourceLicenseInfoData,
) : BaseResponse()

@Serializable
data class GetOpensourceLicenseInfoData(
    @SerialName("termsMngCd")
    val termsMngCd: String = "",            //관리코드
    @SerialName("bctgMngNm")
    val bctgMngNm: String = "",             //약관카테고리명
    @SerialName("termsContent")
    val termsContent: String = "",          //서비스약관내용
    @SerialName("bctgMngCd")
    val bctgMngCd: String = "",             //서비스약관카테고리관리코드
    @SerialName("updDt")
    val updDt: String = "",                 //최종수정일시
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetOpensourceLicenseInfoData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetOpensourceLicenseInfoData
        return this.termsMngCd == asItem.termsMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetOpensourceLicenseInfoData
        return this == asItem
    }
}
