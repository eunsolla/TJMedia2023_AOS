package com.verse.app.model.chat

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/14
 */
@Serializable
data class ChatMessagesResponse(
    @SerialName("result")
    val result: ChatMessagesResult = ChatMessagesResult()
) : BaseResponse() {
    @Serializable
    data class ChatMessagesResult(
        @SerialName("dataList")
        val dataList: List<ChatMessageModel> = listOf()
    ) : BasePaging()

    val list = result.dataList
}
