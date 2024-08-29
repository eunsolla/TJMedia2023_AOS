package com.verse.app.model.user

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
class VerifyNicknameResponse(
    var data: VerifyNickNameData,
) : BaseResponse()

@Serializable
data class VerifyNickNameData(
    var isNickname: String = ""
)