package com.verse.app.model.user

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Serializable

/**
 * Description : 닉네임 자동생성 Response
 *
 * Created by jspark on 2023-02-27
 */
@Serializable
data class AutoCreateNickNameResponse(
    val result: NickName,
) : BaseResponse()

@Serializable
data class NickName(
    val nickName: String,
)

