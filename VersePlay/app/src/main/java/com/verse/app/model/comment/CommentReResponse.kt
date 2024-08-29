package com.verse.app.model.comment

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

/**
 * Description : 콘텐츠 유형 별 댓글 목록 조회
 *
 * Created by jhlee on 2023-04-23
 */
@Serializable
data class CommentReResponse(
    val result: CommentReInfo,
) : BaseResponse()

/**
 * Description : 댓글 목록
 *
 * Created by jhlee on 2023-04-23
 */
@Serializable
data class CommentReInfo(
    var dataList: MutableList<CommentReData> = mutableListOf(),
):BasePaging()