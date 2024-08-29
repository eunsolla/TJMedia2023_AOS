package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetDetailNoticeInfoResponse (
    @SerialName("result")
    val result: GetDetailNoticeInfoData = GetDetailNoticeInfoData(),
) : BaseResponse()

@Serializable
data class GetDetailNoticeInfoData(
    @SerialName("notMngCd")
    val notMngCd: String = "",              //공지사항관리코드
    @SerialName("svcNotTpCd")
    val svcNotTpCd: String = "",            //공지사항유형코드(안내/서비스오픈/서비스종료/서비스소식/이벤트/당첨자)
    @SerialName("notTitle")
    val notTitle: String = "",              //공지사항제목
    @SerialName("notContent")
    val notContent: String = "",            //공지사항내용
    @SerialName("updDt")
    val updDt: String = "",                 //최종수정일시
)