package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetDetailTermsInfoResponse (
    @SerialName("result")
    val result: GetDetailTermsInfoResponseData = GetDetailTermsInfoResponseData(),
) : BaseResponse()

@Serializable
data class GetDetailTermsInfoResponseData(
    @SerialName("termsMngCd")
    val termsMngCd: String = "",            //서비스약관관리코드
    @SerialName("bctgMngNm")
    val bctgMngNm: String = "",             //약관카테고리명
    @SerialName("termsContent")
    val termsContent: String = "",          //서비스약관내용
    @SerialName("bctgMngCd")
    val bctgMngCd: String = "",             //서비스약관카테고리관리코드
    @SerialName("updDt")
    val updDt: String = "",                 //최종수정일시

)