package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 피드 재생 확인 이력 적재 Body
 *
 * Created by jhlee on 2023-05-31
 */
@Serializable
data class FeedHistoryBody(
    @SerialName("feedCode")     //피드관리코드
    val feedCode: String,
    @SerialName("viewTime")    //피드재생시간(초)
    val viewTime: Int
)
