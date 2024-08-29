package com.verse.app.model.chat_recv

import com.verse.app.contants.TcpHeaderType
import com.verse.app.repository.tcp.BaseTcpData

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/16
 */
class ChatCheckSession:BaseTcpData(TcpHeaderType.RECV_SESSION)