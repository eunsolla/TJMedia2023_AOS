package com.verse.app.model.chat_send

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.model.chat.ChatMessageIntentModel
import com.verse.app.model.user.UserData
import com.verse.app.repository.tcp.BaseTcpData

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/17
 */
data class SendChatMessage(
    @TcpOrder(1)
    val nothing1: String = "",
    @TcpOrder(2)
    val roomCode: String = "",
    @TcpOrder(3)
    val senderMemberCode: String = "",
    @TcpOrder(4)
    val senderNickName: String = "",
    @TcpOrder(5)
    val targetMemberCode: String = "", // 대상자 통합 회원 번호
    @TcpOrder(6)
    val msgType: String = "", // CT001 일반 문자열, // CT002 이미지
    @TcpOrder(7)
    val contents: String = "", // 메시지 or 이미지 경로
    @TcpOrder(8)
    val nothing2: String = "",
    @TcpOrder(9)
    val ts: String = "",
    @TcpOrder(10)
    val locale: String = ""
) : BaseTcpData(TcpHeaderType.SEND_MSG) {
    companion object {
        /**
         * 일반 메시지 보내는 타입
         */
        fun createMsg(
            msg: String,
            senderData: UserData,
            targetData: ChatMessageIntentModel,
            locale: String
        ): SendChatMessage {
            return SendChatMessage(
                nothing1 = "",
                roomCode = targetData.roomCode ?: "",
                senderMemberCode = senderData.memCd,
                senderNickName = senderData.memNk,
                targetMemberCode = targetData.targetMemberCode,
                msgType = "CT001",
                contents = msg,
                nothing2 = "",
                ts = "",
                locale = locale
            )
        }

        fun createPhoto(
            uploadUrl: String,
            senderData: UserData,
            targetData: ChatMessageIntentModel,
            locale: String
        ): SendChatMessage {
            return SendChatMessage(
                nothing1 = "",
                roomCode = targetData.roomCode ?: "",
                senderMemberCode = senderData.memCd,
                senderNickName = senderData.memNk,
                targetMemberCode = targetData.targetMemberCode,
                msgType = "CT002",
                contents = uploadUrl,
                nothing2 = "",
                ts = "",
                locale = locale
            )
        }
    }
}