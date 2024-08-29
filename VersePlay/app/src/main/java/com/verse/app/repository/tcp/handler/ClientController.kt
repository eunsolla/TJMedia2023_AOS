package com.verse.app.repository.tcp.handler

import com.verse.app.repository.tcp.OnReceiveMessage
import com.verse.app.utility.DLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ClientController(
    private val callback: OnReceiveMessage
) : ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        if (ctx == null) return
        DLogger.d("NettyLogger","${ctx.channel()} ${ctx.channel().isWritable}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        DLogger.d("NettyLogger","ClientController 서버와의 연결이 종료 되었습니다")
        callback.onHandleDisconnected()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        DLogger.d("NettyLogger","ClientController Error $cause")
        callback.onException(cause)
    }
}
