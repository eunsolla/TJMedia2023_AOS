package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/14
 */
@Serializable
data class DeleteChatBody(
    @SerialName("chatRoomCd")
    val code: String
)
