package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetMyFollowerListResponse (
    @SerialName("result")
    val result: GetMyFollowerListInfo = GetMyFollowerListInfo(),
) : BaseResponse()

@Serializable
data class GetMyFollowerListInfo(
    @SerialName("followerList")
    var followerList: MutableList<GetMyFollowListData> = mutableListOf(),
): BasePaging()