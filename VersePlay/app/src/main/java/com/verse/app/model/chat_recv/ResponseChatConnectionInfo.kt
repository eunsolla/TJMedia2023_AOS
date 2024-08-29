package com.verse.app.model.chat_recv

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

data class ResponseChatConnectionInfo(
    @TcpOrder(1)
    val code: Int = 0 // 2000 정상, 2001 실패, 2002 중복 접속, 2003 중복 접속 불가
) : BaseTcpData(TcpHeaderType.RECV_CONNECTION)