package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetFollowCount (
    val result: GetFollowCountData,
) : BaseResponse()

@Serializable
data class GetFollowCountData(
    @SerialName("followerCount")                                 //팔로워카운트(타인 -> 본인)
    val followerCount: Int = 0,

    @SerialName("followingCount")                                //팔로잉카운트(본인 -> 타인)
    val followingCount: Int = 0,
)

