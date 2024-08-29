package com.verse.app.model.mypage

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.feed.FeedContentsData
import kotlinx.serialization.Serializable

@Serializable
data class GetMyBookMarkContentsResponse(
    val result: GetMyBookMarkContentsInfo = GetMyBookMarkContentsInfo(),
) : BaseResponse()

@Serializable
data class GetMyBookMarkContentsInfo(
    val dataList: List<FeedContentsData> = listOf(),
) : BasePaging()