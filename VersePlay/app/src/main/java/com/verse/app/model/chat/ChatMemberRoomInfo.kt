package com.verse.app.model.chat

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/14
 */
@Serializable
data class ChatMemberRoomInfo(
    @SerialName("result")
    val result: RoomResult = RoomResult()
) : BaseResponse() {

    @Serializable
    data class RoomResult(
        @SerialName("chatRoomCd")
        val roomCode: String = "",
        @SerialName("fgPrivateYn")
        val fgPrivateYn: String = "",       //비공개 계정 여부
        @SerialName("fgBlockYn")
        val fgBlockYn: String = ""          //차단 대상 여부
    )
}