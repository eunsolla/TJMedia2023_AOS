package com.verse.app.model.comment

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 콘텐츠 유형 별 댓글 목록 조회
 *
 * Created by jhlee on 2023-04-23
 */
@Serializable
data class CommentResponse(
    val result: CommentInfo,
) : BaseResponse()

/**
 * Description : 댓글 목록
 *
 * Created by jhlee on 2023-04-23
 */
@Serializable
data class CommentInfo(
    @SerialName("dataList")
    var dataList: MutableList<CommentData>,
) : BasePaging()