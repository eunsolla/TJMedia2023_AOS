package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetNoticeListResponse(
    val result: NoticeListInfo = NoticeListInfo(),
) : BaseResponse()

/**
 * Description : 공지사항 목록
 *
 * Created by esna on 2023-04-23
 */
@Serializable
data class NoticeListInfo(
    var dataList: MutableList<NoticeData> = mutableListOf(),
) : BasePaging()

/**
 * 공지 데이터
 *
 * Created by esna on 2023-04-23
 */
@Serializable
data class NoticeData(
    @SerialName("notMngCd")
    val notMngCd: String = "",                       //공지사항관리코드
    @SerialName("svcNotTpCd")
    val svcNotTpCd: String = "",                     //공지사항유형코드(안내/서비스오픈/서비스종료/서비스소식/이벤트/당첨자)
    @SerialName("notTitle")
    val notTitle: String = "",                       //공지사항제목
    @SerialName("updDt")
    val updDt: String = "",                          //최종수정일시

) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "NoticeData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as NoticeData
        return this.notMngCd == asItem.notMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as NoticeData
        return this == asItem
    }

}
