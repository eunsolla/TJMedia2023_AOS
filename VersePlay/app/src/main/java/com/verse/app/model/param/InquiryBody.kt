package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 의견보내기 Body
 *
 * Created by esna on 2023-05-11
 */
@Serializable
class InquiryBody(
    @SerialName("attImagePath")         //첨부파일이미지경로
    val attImagePath: String="",
    @SerialName("bctgMngCd")         //카테고리분류유형코드
    val bctgMngCd: String="",
    @SerialName("csContent")         //1:1문의 내용
    val csContent: String="",
    @SerialName("csMngCd")         //1:1문의관리코드
    val csMngCd: String="",
    @SerialName("recEmail")         //답변 이메일
    val recEmail: String="",
    @SerialName("appVersion")         //현재 사용 중인 앱 버전명(1.0.0)
    val appVersion: String="",
    @SerialName("osVersion")         // 현재 사용 중인 OS 버전명(ANDROID 13 / IOS 13)
    val osVersion: String="",
)