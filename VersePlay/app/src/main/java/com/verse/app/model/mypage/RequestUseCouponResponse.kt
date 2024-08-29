package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestUseCouponResponse (
    @SerialName("result")
    var result: RequestUseCouponData = RequestUseCouponData(),

) : BaseResponse()

@Serializable
data class RequestUseCouponData(
    @SerialName("refreshJwt")
    val refreshJwt: String = "",                        //JWT 토큰

    @SerialName("userCode")
    val userCode: String = "",                          //사용자회원관리코드

    @SerialName("userNick")
    val userNick: String = "",                          //닉네임

    @SerialName("userStatus")
    val userStatus: String = "",                        //회원상태코드(정상/정지/휴면/탈퇴)

    @SerialName("userGrade")
    val userGrade: String = "",                         //회원구매등급코드

    @SerialName("userEmail")
    val userEmail: String = "",                         //회원이메일

    @SerialName("memberType")
    val memberType: String = "",                        //회원유형코드(신규/무료/유료/TJ대리점/협력사/BP파트너/인플루언서)

    @SerialName("authType")
    val authType: String = "",                          //회원계정인증유형코드(AU001 / AU002 / AU003 / AU004 / AU005 / AU006)

    @SerialName("authToken")
    val authToken: String = "",                         //회원계정인증토큰(SNS인증토큰)

)