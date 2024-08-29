package com.verse.app.model.user

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class CheckRegMemberResponse(
    var flagRegMember: String = ""
) : BaseResponse()
