package com.verse.app.model.feed

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMyBookMarkAccompanimentResponse(
    @SerialName("result")
    val result: GetMyBookMarkContentsInfo = GetMyBookMarkContentsInfo(),
) : BaseResponse()

@Serializable
data class GetMyBookMarkContentsInfo(
    @SerialName("dataList")
    val dataList: List<CommonAccompanimentData> = listOf(),
) : BasePaging()