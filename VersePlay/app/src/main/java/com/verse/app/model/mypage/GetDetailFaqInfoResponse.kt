package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetDetailFaqInfoResponse(
    @SerialName("result")
    val result: GetDetailFaqInfo = GetDetailFaqInfo(),
) : BaseResponse()

@Serializable
data class GetDetailFaqInfo(
    @SerialName("faqMngCd")                                 //FAQ관리코드
    var faqMngCd: String = "",

    @SerialName("faqTitle")                                 //FAQ제목
    var faqTitle: String = "",

    @SerialName("faqContent")                               //FAQ내용
    var faqContent: String = "",

    @SerialName("bctgMngCd")                                //FAQ카테고리관리코드
    var bctgMngCd: String = "",
)