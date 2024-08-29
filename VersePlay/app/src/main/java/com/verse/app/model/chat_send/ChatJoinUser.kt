package com.verse.app.model.chat_send

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/21
 */
data class ChatJoinUser(
    @TcpOrder(1)
    val roomCode: String,
    @TcpOrder(2)
    val memberCode: String,
    @TcpOrder(3)
    val targetMemberCode: String
) : BaseTcpData(TcpHeaderType.SEND_JOIN_USER)