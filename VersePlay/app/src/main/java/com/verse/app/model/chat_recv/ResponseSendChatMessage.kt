package com.verse.app.model.chat_recv

import com.verse.app.contants.ChatMsgType
import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

data class ResponseSendChatMessage(
    @TcpOrder(1)
    val chatCode: String = "",
    @TcpOrder(2)
    val roomCode: String = "",
    @TcpOrder(3)
    val sendMemberCode: String = "", // 전송자 회원 번호
    @TcpOrder(4)
    val targetMemberCode: String = "", // 대상자 회원 번호
    @TcpOrder(5)
    val msgType: String = "", // CT001 일반 문자열, CT002 이미지
    @TcpOrder(6)
    val msg: String = "", // 메시지 내용
    @TcpOrder(7)
    val sendTime: String = "",
    @TcpOrder(8)
    val timeStamp: String = ""
) : BaseTcpData(TcpHeaderType.RECV_MSG) {
    fun chatType(): ChatMsgType {
        return ChatMsgType.getType(msgType)
    }
}