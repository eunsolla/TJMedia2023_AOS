package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 팔로우 On/Off Body
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class FollowBody(
    @SerialName("followYn")         //팔로우 설정/해제(Y:설정 / N:해제)
    val followYn: String,
    @SerialName("userCode")         //팔로우 대상회원관리코드
    val userCode: String,
) {
    constructor(userCode: String, isFollow: Boolean) : this(
        followYn = if (isFollow) "Y" else "N",
        userCode = userCode
    )
}