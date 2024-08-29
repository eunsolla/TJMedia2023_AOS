package com.verse.app.model.chat_recv

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

data class ChatResponseCode(
    @TcpOrder(1)
    val roomCode: String = "",
    @TcpOrder(2)
    val code: Int = 0, // 5000 요청 정상 처리, 5001 실패
    @TcpOrder(3)
    val msg : String = ""
) : BaseTcpData(TcpHeaderType.RECV_CHAT)