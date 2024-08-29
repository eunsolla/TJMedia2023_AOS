package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : Purchase Info
 *
 * Created by jspark on 2023/05/22
 */
@Serializable
data class PurchaseInfoBody(
    @SerialName("subscTpCd")
    val subscTpCd: String = "",
    @SerialName("purchTpCd")
    val purchTpCd: String = "",
    @SerialName("purchPrdId")
    val purchPrdId: String = "",
    @SerialName("purchToken")
    val purchToken: String = "",
    @SerialName("purchPrice")
    val purchPrice: String = "",
    @SerialName("purchUnit")
    val purchUnit: String = "",
)