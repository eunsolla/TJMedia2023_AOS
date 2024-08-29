package com.verse.app.model.singpass

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingPassUserInfoResponse(
    val result: SingPassUserInfoData = SingPassUserInfoData(),
) : BaseResponse()

@Serializable
data class SingPassUserInfoData(
    @SerialName("singPassUserInfo")
    val singPassUserInfo: SingPassUserInfo = SingPassUserInfo(),
    @SerialName("singPassTerms")
    val singPassTerms: SingPassTerms? = null,
    @SerialName("singPointList")
    val singPointList: MutableList<SingPoint> = mutableListOf(),
)

@Serializable
data class SingPassUserInfo(
    @SerialName("seaLevelNm")
    val seaLevelNm: String = "",
    @SerialName("totSingPoint")
    val totSingPoint: Int = 0,
    @SerialName("memNk")
    val memNk: String = "",
    @SerialName("memGrCd")
    val memGrCd: String = "",
    @SerialName("ranking")
    val ranking: String = "",
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",
    @SerialName("pfBgImgPath")
    val pfBgImgPath: String = "",
    @SerialName("topItemPicPath")
    val topItemPicPath: String = "",
    @SerialName("memStCd")
    val memStCd: String = "",
    @SerialName("memTpCd")
    val memTpCd: String = "",
    @SerialName("subscTpCd")
    val subscTpCd: String = "",
    @SerialName("etcSingPoint")
    val etcSingPoint: Int = 0,   //기타 씽포인트 합
) {
    fun getTopMissionItem(index: Int): String {
        return if (topItemPicPath.isNotEmpty()) {
            val split = topItemPicPath.split(",")
            split.getOrNull(index) ?: ""
        } else {
            ""
        }
    }
}

@Serializable
data class SingPassTerms(
    @SerialName("singPassTermsTitle")
    val singPassTermsTitle: String = "",
    @SerialName("singPassTermsContents")
    val singPassTermsContents: String = "",
)

@Serializable
data class SingPoint(
    @SerialName("svcMiRegTpCd")
    val svcMiRegTpCd: String = "",
    @SerialName("singPoint")
    val singPoint: Int = 0,
)




