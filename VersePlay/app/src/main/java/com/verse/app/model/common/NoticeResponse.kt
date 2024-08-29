package com.verse.app.model.common

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class NoticeResponse(
    val result: NoticeData? = NoticeData(),
) : BaseResponse()
