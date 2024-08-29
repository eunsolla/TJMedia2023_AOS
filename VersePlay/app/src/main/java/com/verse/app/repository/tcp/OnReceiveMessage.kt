package com.verse.app.repository.tcp

import com.verse.app.model.chat_recv.ChatCheckSession
import com.verse.app.model.chat_recv.ChatReadMessage
import com.verse.app.model.chat_recv.ChatResponseCode
import com.verse.app.model.chat_recv.ReceiveChatMessage
import com.verse.app.model.chat_recv.ResponseChatConnectionInfo
import com.verse.app.model.chat_recv.ResponseResourcePath
import com.verse.app.model.chat_recv.ResponseSendChatMessage

/**
 * Description : Netty Socket 메시지 처리
 *
 * Created by juhongmin on 2023/06/12
 */
interface OnReceiveMessage {
    fun onCheckSession(msg: ChatCheckSession)
    fun onChatReadMessage(msg: ChatReadMessage)
    fun onChatResponseCode(msg: ChatResponseCode)
    fun onReceiveChatMessage(msg: ReceiveChatMessage)
    fun onResponseChatConnectionInfo(msg: ResponseChatConnectionInfo)
    fun onResponseResourcePath(msg: ResponseResourcePath)
    fun onResponseSendChatMessage(msg: ResponseSendChatMessage)
    fun onHandleDisconnected() // 접속이 끊어진경우 클라이언트에서 접속은 끊었는지, 서버에서 끊은건지 처리하기 위한 함수
    fun onException(err: Throwable?)
}