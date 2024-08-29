package com.verse.app.model.mypage

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.feed.FeedContentsData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMyUploadContentsResponse(
    @SerialName("result")
    val result: GetMyUploadContentsInfo = GetMyUploadContentsInfo(),
) : BaseResponse()

@Serializable
data class GetMyUploadContentsInfo(
    val dataList: List<FeedContentsData> = listOf(),
) : BasePaging()
