package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 마이페이지 -> 씽패스 시즌클리어 조회
 *
 * Created by esna on 2023-04-28
 */
@Serializable
class GetSingPassSeasonClearResponse(
    @SerialName("result")
    val result: GetSingPassSeasonClearInfo = GetSingPassSeasonClearInfo(),
) : BaseResponse()

@Serializable
data class GetSingPassSeasonClearInfo(
    @SerialName("dataList")
    var dataList: List<GetSingPassSeasonClearData> = listOf(),
) : BasePaging()

@Serializable
data class GetSingPassSeasonClearData(
    @SerialName("svcSeaMngCd")
    val svcSeaMngCd: String = "",                       //서비스시즌관리코드
    @SerialName("svcSeaMngNm")
    val svcSeaMngNm: String = "",                       //서비스시즌관리명
    @SerialName("clrItemPicPath")
    val clrItemPicPath: String = "",                   //시즌획득아이템이미지경로

) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetSingPassSeasonClearData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is GetSingPassSeasonClearData) {
            this.svcSeaMngCd == diffUtil.svcSeaMngCd
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is GetSingPassSeasonClearData) {
            this == diffUtil
        } else {
            false
        }
    }
}