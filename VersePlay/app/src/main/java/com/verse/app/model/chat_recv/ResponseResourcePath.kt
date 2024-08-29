package com.verse.app.model.chat_recv

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

data class ResponseResourcePath(
    @TcpOrder(1)
    val chatCode: String = "",
    @TcpOrder(2)
    val roomCode: String = "",
    @TcpOrder(3)
    val sendMemberCode: String = "", // 전송자 회원 번호
    @TcpOrder(4)
    val chatType: String = "", // PHOTO
    @TcpOrder(5)
    val uploadPath: String = "", // 업로드 경로
    @TcpOrder(6)
    val sendTime: String = "", // 발송 시간
    @TcpOrder(7)
    val timeStamp: String = "" // 타임 스탬프
) : BaseTcpData(TcpHeaderType.RECV_IMAGE_RESOURCE)
