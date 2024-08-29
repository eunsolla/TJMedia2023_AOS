package com.verse.app.model.report

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 신고하기 카테고리 정보 조회
 *
 * Created by jhlee on 2023-02-27
 */
@Serializable
data class ReportResponse(
    val result: ReportInfo,
) : BaseResponse()

@Serializable
data class ReportInfo(
    val dataList: MutableList<ReportData>,
) : BasePaging()


@Serializable
data class ReportData(
    @SerialName("repCtgMngCd")
    val repCtgMngCd: String = "",    //신고하기카테고리관리코드
    @SerialName("repCtgMngNm")
    val repCtgMngNm: String = "",    //신고하기카테고리명
    @SerialName("bctgTpCd")
    val bctgTpCd: String = "",          //카테고리분류유형코드
    @SerialName("subDataList")
    val subDataList: MutableList<ReportSubData> = mutableListOf(), //서브 카테고리

) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "ReportData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as ReportData
        return this.repCtgMngCd == asItem.repCtgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as ReportData
        return this == asItem
    }
}

@Serializable
data class ReportSubData(
    @SerialName("repCtgMngCd")
    val repCtgMngCd: String = "",    //신고하기카테고리관리코드
    @SerialName("repCtgMngNm")
    val repCtgMngNm: String = "",    //신고하기카테고리명
    @SerialName("bctgTpCd")
    val bctgTpCd: String = "",          //카테고리분류유형코드
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "ReportSubData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as ReportSubData
        return this.repCtgMngCd == asItem.repCtgMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as ReportSubData
        return this == asItem
    }
}

