package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 관심없음  On/Off Body
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class UnInterestedBody(
    @SerialName("contentCode")               //관심없음 콘텐츠 관리코드(피드관리코드)
    val contentCode: String,
    @SerialName("uninterYn")                   //관심없음 설정/해제 (Y:설정 / N:해제)
    val uninterYn: String,
)