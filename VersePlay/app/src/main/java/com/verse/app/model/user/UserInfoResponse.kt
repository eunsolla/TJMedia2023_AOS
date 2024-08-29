package com.verse.app.model.user

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    @SerialName("result")
    val result: UserData = UserData()
) : BaseResponse()
