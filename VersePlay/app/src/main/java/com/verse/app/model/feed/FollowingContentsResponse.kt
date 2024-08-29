package com.verse.app.model.feed

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.mypage.RecommendUserData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowingContentsResponse(
    @SerialName("result")
    val result: FollowingContentsInfo
) : BaseResponse()

@Serializable
data class FollowingContentsInfo(
    @SerialName("feedList")
    val feedList: MutableList<FeedContentsData> = mutableListOf(),
    @SerialName("userList")
    val userList: MutableList<RecommendUserData> = mutableListOf()
) : BasePaging()


