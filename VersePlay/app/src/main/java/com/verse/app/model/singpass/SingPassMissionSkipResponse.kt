package com.verse.app.model.singpass

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingPassMissionSkipResponse(
    val result: MissionSkipData = MissionSkipData(),
) : BaseResponse()

@Serializable
data class MissionSkipData(
    @SerialName("miJoinMngCd")
    val miJoinMngCd: String = "",
    @SerialName("memCd")
    val memCd: String = "",
    @SerialName("userSkCnt")
    val userSkCnt: Int = 0,
    @SerialName("miSkTpCd")
    val miSkTpCd: String = "",
    @SerialName("svcMiMngCd")
    val svcMiMngCd: String = "",
    @SerialName("updId")
    val updId: String = "",
    @SerialName("regId")
    val regId: String = "",
    @SerialName("langCd")
    val langCd: String = "",
    @SerialName("nationCd")
    val nationCd: String = "",
    @SerialName("norSkCnt")
    val norSkCnt: Int = 0,
    @SerialName("seaSkCnt")
    val seaSkCnt: Int = 0,
)
