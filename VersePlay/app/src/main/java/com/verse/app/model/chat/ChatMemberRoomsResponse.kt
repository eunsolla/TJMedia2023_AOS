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
data class ChatMemberRoomsResponse(
    @SerialName("result")
    val result: ChatMemberRoomsResult = ChatMemberRoomsResult()
) : BaseResponse() {

    @Serializable
    data class ChatMemberRoomsResult(
        @SerialName("dataList")
        val dataList: List<ChatMemberRoomModel> = listOf()
    ) : BasePaging()

    val list = result.dataList
}
