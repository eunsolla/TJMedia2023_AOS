package com.verse.app.model.comment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 댓글 +답글 총 수
 *
 * Created by jhlee on 2023-04-23
 */
@Serializable
data class CommentRepCountData(
    @SerialName("totalCount")
    val totalCount: Int = 0,                  // 댓글+답글 수
)