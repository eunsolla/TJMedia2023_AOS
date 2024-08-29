package com.verse.app.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 공통 Response
 *
 * Created by jhlee on 2023-01-01
 */
@Serializable
open class BaseResponse {
    @SerialName("httpStatus")
    val httpStatus: String = ""

    @SerialName("status")
    val status: String = ""

    @SerialName("message")
    val message: String = ""

    @SerialName("resultCode")
    val resultCode: String = ""

    @SerialName("fgProhibitYn")
    val fgProhibitYn: String = ""
}