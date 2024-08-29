package com.verse.app.model.mypage

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.feed.CommonContentsData
import com.verse.app.model.feed.FeedContentsData
import kotlinx.serialization.Serializable

@Serializable
data class GetPrivateContentsResponse(
    val result: GetPrivateContentsInfo = GetPrivateContentsInfo(),
) : BaseResponse()

@Serializable
data class GetPrivateContentsInfo(
    val dataList: List<FeedContentsData> = listOf(),
) : BasePaging()