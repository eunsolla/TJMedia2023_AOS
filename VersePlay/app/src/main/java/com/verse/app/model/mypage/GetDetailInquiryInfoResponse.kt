package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetDetailInquiryInfoResponse(
    var result: GetDetailInquiryListData,

    ) : BaseResponse()

@Serializable
data class GetDetailInquiryListData(
    @SerialName("csMngCd")                                 //문의내역관리코드
    val csMngCd: String = "",

    @SerialName("bctgMngCd")                               //문의내역카테고리관리코드
    val bctgMngCd: String = "",

    @SerialName("stNm")                                    //문의요청상태
    val stNm: String = "",

    @SerialName("csContent")                               //문의내용
    val csContent: String = "",

    @SerialName("csReqAttImgPath")                         //문의요청첨부파일경로
    val csReqAttImgPath: String = "",

    @SerialName("csReqDt")                                 //문의요청일시
    val csReqDt: String = "",

    @SerialName("csResContent")                            //문의답변내용
    val csResContent: String = "",

    @SerialName("csResDt")                                 //문의답변일시
    val csResDt: String = "",

    @SerialName("memEmail")                                //답변수신이메일
    val memEmail: String = "",
)