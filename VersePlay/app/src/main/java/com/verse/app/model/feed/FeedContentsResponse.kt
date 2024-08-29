package com.verse.app.model.feed

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedContentsResponse(
    var result: FeedInfo,
) : BaseResponse()

@Serializable
data class FeedInfo(
    val dataList: MutableList<FeedContentsData>,
    @SerialName("fgSongBookMarkYn")
    var fgSongBookMarkYn: String = ""
) : BasePaging()



