package com.verse.app.repository.tcp.receive

import com.verse.app.model.chat_recv.ChatCheckSession
import com.verse.app.model.chat_recv.ChatReadMessage
import com.verse.app.model.chat_recv.ChatResponseCode
import com.verse.app.model.chat_recv.ReceiveChatMessage
import com.verse.app.model.chat_recv.ResponseChatConnectionInfo
import com.verse.app.model.chat_recv.ResponseResourcePath
import com.verse.app.model.chat_recv.ResponseSendChatMessage
import com.verse.app.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ChatCheckSessionReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ChatCheckSession>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ChatCheckSession?) {
        if (msg == null) return
        callback.onCheckSession(msg)
    }
}

class ChatReadMessageReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ChatReadMessage>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ChatReadMessage?) {
        if (msg == null) return
        callback.onChatReadMessage(msg)
    }
}

class ChatResponseCodeReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ChatResponseCode>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ChatResponseCode?) {
        if (msg == null) return
        callback.onChatResponseCode(msg)
    }
}

class ReceiveChatMessageReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ReceiveChatMessage>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ReceiveChatMessage?) {
        if (msg == null) return
        callback.onReceiveChatMessage(msg)
    }
}

class ResponseChatConnectionInfoReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ResponseChatConnectionInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ResponseChatConnectionInfo?) {
        if (msg == null) return
        callback.onResponseChatConnectionInfo(msg)
    }
}

class ResponseResourcePathReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ResponseResourcePath>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ResponseResourcePath?) {
        if (msg == null) return
        callback.onResponseResourcePath(msg)
    }
}

class ResponseSendChatMessageReceiver(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<ResponseSendChatMessage>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ResponseSendChatMessage?) {
        if (msg == null) return
        callback.onResponseSendChatMessage(msg)
    }
}
