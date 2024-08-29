package com.verse.app.model.mypage

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.feed.FeedContentsData
import kotlinx.serialization.Serializable

@Serializable
data class GetMyLikeContentsResponse(
    val result: GetMyLikeContentsInfo = GetMyLikeContentsInfo(),
) : BaseResponse()

@Serializable
data class GetMyLikeContentsInfo(
    val dataList: List<FeedContentsData> = listOf(),
) : BasePaging()