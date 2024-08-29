package com.verse.app.model.common

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckSystemNotPopupResponse(
    @SerialName("result")
    val result: CheckSystemNotPopupData = CheckSystemNotPopupData(),
) : BaseResponse()

@Serializable
data class CheckSystemNotPopupData(

    @SerialName("checkMngCd")
    val checkMngCd: String = "",            //서비스점검관리코드
    @SerialName("checkTitle")
    val checkTitle: String = "",            //서비스점검제목
    @SerialName("checkDesc")
    val checkDesc: String = "",             //서비스점검내용
    @SerialName("checkStDt")
    val checkStDt: String = "",             //서비스점검시작일시
    @SerialName("checkFnDt")
    val checkFnDt: String = "",             //서비스점검종료일시
)

