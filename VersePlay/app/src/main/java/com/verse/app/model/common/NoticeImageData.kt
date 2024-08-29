package com.verse.app.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoticeImageData(
    @SerialName("svcPopImgPath")
    val svcPopImgPath: String = "",
    @SerialName("svcPopUrl")
    val svcPopUrl: String = "",
    @SerialName("svcLdTpCd")
    val svcLdTpCd: String = "",
)