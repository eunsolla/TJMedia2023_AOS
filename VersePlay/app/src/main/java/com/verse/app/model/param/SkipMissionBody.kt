package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by jspark on 2023/05/22
 */
@Serializable
data class SkipMissionBody(
    @SerialName("svcMiMngCd")
    val svcMiMngCd: String = "",
    @SerialName("svcMiRegTpCd")
    val svcMiRegTpCd: String = "",
    @SerialName("miSkTpCd")
    val miSkTpCd: String = "",
    @SerialName("svcSeaMngCd")
    val svcSeaMngCd: String = "",
)