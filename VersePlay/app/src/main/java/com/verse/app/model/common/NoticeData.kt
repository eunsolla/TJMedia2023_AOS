package com.verse.app.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoticeData(

    @SerialName("svcPopMngCd")
    val svcPopMngCd: String = "",
    @SerialName("svcShwTrgCd")
    val svcShwTrgCd: String = "",
    @SerialName("svcStDt")
    val svcStDt: String = "",
    @SerialName("svcFnDt")
    val svcFnDt: String = "",
    @SerialName("svcPopTitle")
    val svcPopTitle: String = "",
    @SerialName("imageList")
    val imageList: ArrayList<NoticeImageData> = ArrayList(),
)