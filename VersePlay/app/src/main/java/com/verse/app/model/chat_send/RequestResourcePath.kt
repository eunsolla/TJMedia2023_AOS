package com.verse.app.model.chat_send

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

data class RequestResourcePath(
    @TcpOrder(1)
    val nothing1: String = "",
    @TcpOrder(2)
    val roomCode: String = "",
    @TcpOrder(3)
    val senderMemberCode: String = "",
    @TcpOrder(4)
    val msgType: String = "PHOTO"
) : BaseTcpData(TcpHeaderType.SEND_UPLOAD_IMG)