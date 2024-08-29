package com.verse.app.model.chat_recv

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

data class ChatReadMessage(
    @TcpOrder(1)
    val chatCode: String = "",
    @TcpOrder(2)
    val memberCode: String = "",
    @TcpOrder(3)
    val targetMemberCode: String = ""
) : BaseTcpData(TcpHeaderType.RECV_CHAT_READ)