package com.verse.app.model.comment

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

/**
 * Description : 댓글 +답글 총 수 조회
 *
 * Created by jhlee on 2023-04-23
 */
@Serializable
data class CommentRepCountResponse(
    val result: CommentRepCountData,
) : BaseResponse()