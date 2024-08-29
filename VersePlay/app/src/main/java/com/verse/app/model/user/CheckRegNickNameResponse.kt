package com.verse.app.model.user

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
class CheckRegNickNameResponse(
    var result: String,
) : BaseResponse()