package com.verse.app.model.mypage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RecommendNickNameBody(
    @SerialName("recNickName")
    val recNickName: String
)