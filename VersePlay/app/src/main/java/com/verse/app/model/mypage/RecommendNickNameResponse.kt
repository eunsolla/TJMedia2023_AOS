package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RecommendNickNameResponse(
    @SerialName("result")
    var result: RecommendNickNameData = RecommendNickNameData(),
) : BaseResponse()

@Serializable
data class RecommendNickNameData(
    @SerialName("remainCount")
    var remainCount: String = "", //초대 가능 남은 횟수
)