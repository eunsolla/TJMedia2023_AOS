package com.verse.app.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckRegNickNameBody(
    @SerialName("nickName")
    val nickName: String
)