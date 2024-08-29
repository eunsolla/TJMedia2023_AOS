package com.verse.app.model.chat_send

import com.verse.app.contants.AppData
import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import com.verse.app.repository.tcp.BaseTcpData

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/16
 */
data class ChatResponseSession(
    @TcpOrder(1)
    val memberCode: String,
    @TcpOrder(2)
    val osCode: String = AppData.TCP_OS
) : BaseTcpData(TcpHeaderType.SEND_SESSION)