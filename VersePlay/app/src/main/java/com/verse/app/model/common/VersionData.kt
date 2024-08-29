package com.verse.app.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionData(

    @SerialName("fgCheckServerYn")
    val fgCheckServerYn: String = "",
    @SerialName("osType")
    val osType: String = "",
    @SerialName("updType")
    val updType: String = "",
    @SerialName("versionNm")
    val versionNm: String = "",
    @SerialName("versionCd")
    val versionCd: String = "",
    @SerialName("fgUseNotYn")
    val fgUseNotYn: String = "",
    @SerialName("notTitle")
    val notTitle: String = "",
    @SerialName("notContent")
    val notContent: String = "",
    @SerialName("checkType")
    val checkType: String = "",            //서비스점검유형코드
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