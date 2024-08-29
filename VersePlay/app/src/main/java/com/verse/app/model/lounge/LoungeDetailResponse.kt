package com.verse.app.model.lounge

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@Serializable
data class LoungeDetailResponse(
    @SerialName("result")
    val result: LoungeDetailData = LoungeDetailData()
) : BaseResponse()