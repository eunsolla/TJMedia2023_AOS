package com.verse.app.model.community

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class CommunityLoungeResponse(
    @SerialName("result")
    val result: Result = Result()
) : BaseResponse() {
    @Serializable
    data class Result(
        @SerialName("dataList")
        val list: List<CommunityLoungeData> = listOf()
    )
}