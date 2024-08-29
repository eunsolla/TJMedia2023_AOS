package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/14
 */
@Serializable
data class ChatMemberRoomBody(
    @SerialName("targetMemCd")
    val memberCode : String // 채팅 대상자 회원 관리 코드
)