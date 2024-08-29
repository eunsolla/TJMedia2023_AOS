package com.verse.app.model.vote

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@Serializable
data class VoteDetailResponse(
    @SerialName("result")
    val result: VoteDetailData = VoteDetailData()
) : BaseResponse()