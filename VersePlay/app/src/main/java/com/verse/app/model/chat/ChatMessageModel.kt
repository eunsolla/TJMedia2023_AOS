package com.verse.app.model.chat

import com.verse.app.contants.ChatMsgType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/14
 */
@Serializable
data class ChatMessageModel(
    @SerialName("isMine")
    val isMine: String = "N", // 내 메시지 여부(Y : 내메시지 / N : 상대방메시지)
    @SerialName("memCd")
    val memberCode: String = "", // 회원 관리 코드
    @SerialName("pfImgPath")
    val profileImagePath: String = "", // 프로필이미지경로
    @SerialName("memNk")
    val nickName: String = "", // 닉네임
    @SerialName("chatMsg")
    val chatMsg: String = "",
    @SerialName("chatTpCd")
    val type: String = "", // 메시지 유형 코드(TEXT(CT001) : 텍스트 / PHOTO(CT002) : 사진)
    @SerialName("chatReadYn")
    val chatReadYn: String = "", // 메시지 확인 여부(Y : 읽음 / N : 읽지않음)
    @SerialName("chatDt")
    val chatDt: String = "", // 메시지 등록 일시
    @SerialName("chatTs")
    val chatTs: String = "" // 타임 스탬프
) {
    fun chatType(): ChatMsgType {
        return ChatMsgType.getType(type)
    }
}
