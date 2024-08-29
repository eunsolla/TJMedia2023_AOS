package com.verse.app.model.community

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class CommunityVoteResponse(
    @SerialName("result")
    val result: Result = Result()
) {
    @Serializable
    data class Result(
        @SerialName("dataList")
        val list: List<CommunityVoteData> = listOf()
    )
}