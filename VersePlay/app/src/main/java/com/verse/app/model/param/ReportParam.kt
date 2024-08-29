package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 신고 하기
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class ReportParam(
    @SerialName("repTpCd")     // RP001:사용자신고 / RP002:피드콘텐츠신고 / RP003:피드댓글신고 / RP004:피드답글신고 / RP005:커뮤니티라운지신고
    val repTpCd: String,        // RP006:커뮤니티라운지댓글신고 / RP007:커뮤니티라운지답글신고 / RP008:커뮤니티투표하기댓글신고 / RP009:커뮤니티투표하기답글신고
    @SerialName("repMngCd")     //선택 신고하기 카테고리관리코드
    val repMngCd: String,
    @SerialName("repConCd")     //신고 콘텐츠 관리 코드
    val repConCd: String,
    @SerialName("fgLoginYn")     //로그인 상태(Y:로그인 / N:비로그인)
    val fgLoginYn: String,
) {
    fun toMap(): MutableMap<String, Any> = mutableMapOf(
        "repTpCd" to repTpCd,
        "repMngCd" to repMngCd,
        "repConCd" to repConCd,
        "fgLoginYn" to fgLoginYn,
    )
}