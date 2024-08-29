package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 광고 확인 이력 적재 Body
 *
 * Created by jhlee on 2023-05-31
 */
@Serializable
data class AdHistoryBody(
    @SerialName("adMngCd")     //광고관리코드
    val adMngCd: String,
    @SerialName("viewTime")    //광고확인시간(초)
    val viewTime: Int,
    @SerialName("adConUrl")    //광고연결URL
    val adConUrl: String?=null,
)
