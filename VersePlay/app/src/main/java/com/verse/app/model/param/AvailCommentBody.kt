package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 피드 댓글 허용 여부 변경 요청
 *
 * Created by jhlee on 2023-06-05
 */
@Serializable
data class AvailCommentBody(
    @SerialName("feedMngCd")        // 피드관리코드
    val feedMngCd: String,
    @SerialName("availComment")     //댓글 허용 여부(Y : 댓글 허용 / N : 댓글 비허용)
    val availComment: String,
)
