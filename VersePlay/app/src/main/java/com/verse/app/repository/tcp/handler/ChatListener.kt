package com.verse.app.repository.tcp.handler

import com.verse.app.model.chat_recv.ReceiveChatMessage
import com.verse.app.model.chat_recv.ResponseChatConnectionInfo
import com.verse.app.model.chat_recv.ResponseResourcePath
import com.verse.app.model.chat_recv.ResponseSendChatMessage

/**
 * Description : 채팅 리스너
 *
 * Created by juhongmin on 2023/06/17
 */
interface ChatListener {
    fun onSendedMessage(msg: ResponseSendChatMessage)
    fun onSuccessPacket()
    fun onUploadImagePath(msg: ResponseResourcePath)
    fun onReceiveMessage(msg: ReceiveChatMessage)
    fun onReadAllMsg()
}