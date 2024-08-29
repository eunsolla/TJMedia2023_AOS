package com.verse.app.repository.tcp.handler

import com.verse.app.utility.DLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufHolder
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.logging.ByteBufFormat
import io.netty.util.internal.StringUtil
import java.net.SocketAddress

/**
 * Description : TCP Logging Handler
 *
 * Created by juhongmin on 2023/06/16
 */
@ChannelHandler.Sharable
class LoggingHandler : ChannelDuplexHandler() {

    private val byteBufFormat: ByteBufFormat by lazy { ByteBufFormat.HEX_DUMP }

    private fun logD(msg: String?) {
        if (msg.isNullOrEmpty()) return
        DLogger.d("NettyLogger", msg)
    }

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logD(format(ctx, "REGISTERED"))
        ctx.fireChannelRegistered()
    }

    @Throws(Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        logD(format(ctx, "UNREGISTERED"))
        ctx.fireChannelUnregistered()
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        logD(format(ctx, "ACTIVE"))
        ctx.fireChannelActive()
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        logD(format(ctx, "INACTIVE"))
        ctx.fireChannelInactive()
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logD(format(ctx, "EXCEPTION", cause))
        ctx.fireExceptionCaught(cause)
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        logD(format(ctx, "USER_EVENT", evt))
        ctx.fireUserEventTriggered(evt)
    }

    @Throws(Exception::class)
    override fun bind(
        ctx: ChannelHandlerContext,
        localAddress: SocketAddress,
        promise: ChannelPromise?
    ) {
        logD(format(ctx, "BIND", localAddress))
        ctx.bind(localAddress, promise)
    }

    @Throws(Exception::class)
    override fun connect(
        ctx: ChannelHandlerContext,
        remoteAddress: SocketAddress, localAddress: SocketAddress?, promise: ChannelPromise?
    ) {
        logD(format(ctx, "CONNECT", remoteAddress, localAddress))
        ctx.connect(remoteAddress, localAddress, promise)
    }

    @Throws(Exception::class)
    override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        logD(format(ctx, "DISCONNECT"))
        ctx.disconnect(promise)
    }

    @Throws(Exception::class)
    override fun close(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        logD(format(ctx, "CLOSE"))
        ctx.close(promise)
    }

    @Throws(Exception::class)
    override fun deregister(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        logD(format(ctx, "DEREGISTER"))
        ctx.deregister(promise)
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        logD(format(ctx, "READ COMPLETE"))
        ctx.fireChannelReadComplete()
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        logD(format(ctx, "READ READ", msg))
        ctx.fireChannelRead(msg)
    }

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise?) {
        logD(format(ctx, "WRITE", msg))
        ctx.write(msg, promise)
    }

    @Throws(Exception::class)
    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        logD(format(ctx, "WRITABILITY CHANGED"))
        ctx.fireChannelWritabilityChanged()
    }

    @Throws(Exception::class)
    override fun flush(ctx: ChannelHandlerContext) {
        logD(format(ctx, "FLUSH"))
        ctx.flush()
    }

    /**
     * Formats an event and returns the formatted message.
     *
     * @param eventName the name of the event
     */
    protected fun format(ctx: ChannelHandlerContext, eventName: String): String {
        val chStr = ctx.channel().toString()
        return StringBuilder(chStr.length + 1 + eventName.length)
            .append(chStr)
            .append(' ')
            .append(eventName)
            .toString()
    }

    /**
     * Formats an event and returns the formatted message.
     *
     * @param eventName the name of the event
     * @param arg       the argument of the event
     */
    protected fun format(ctx: ChannelHandlerContext, eventName: String, arg: Any): String? {
        return if (arg is ByteBuf) {
            formatByteBuf(ctx, eventName, arg)
        } else if (arg is ByteBufHolder) {
            formatByteBufHolder(ctx, eventName, arg)
        } else {
            formatSimple(ctx, eventName, arg)
        }
    }

    /**
     * Formats an event and returns the formatted message.  This method is currently only used for formatting
     * [ChannelOutboundHandler.connect].
     *
     * @param eventName the name of the event
     * @param firstArg  the first argument of the event
     * @param secondArg the second argument of the event
     */
    protected fun format(
        ctx: ChannelHandlerContext,
        eventName: String,
        firstArg: Any,
        secondArg: Any?
    ): String? {
        if (secondArg == null) {
            return formatSimple(ctx, eventName, firstArg)
        }
        val chStr = ctx.channel().toString()
        val arg1Str = firstArg.toString()
        val arg2Str = secondArg.toString()
        val buf = StringBuilder(
            chStr.length + 1 + eventName.length + 2 + arg1Str.length + 2 + arg2Str.length
        )
        buf.append(chStr).append(' ').append(eventName).append(": ").append(arg1Str).append(", ")
            .append(arg2Str)
        return buf.toString()
    }

    /**
     * Generates the default log message of the specified event whose argument is a [ByteBuf].
     */
    private fun formatByteBuf(
        ctx: ChannelHandlerContext,
        eventName: String,
        msg: ByteBuf
    ): String? {
        val chStr = ctx.channel().toString()
        val length = msg.readableBytes()
        return if (length == 0) {
            val buf = StringBuilder(chStr.length + 1 + eventName.length + 4)
            buf.append(chStr).append(' ').append(eventName).append(": 0B")
            buf.toString()
        } else {
            var outputLength = chStr.length + 1 + eventName.length + 2 + 10 + 1
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                val rows = length / 16 + (if (length % 15 == 0) 0 else 1) + 4
                val hexDumpLength = 2 + rows * 80
                outputLength += hexDumpLength
            }
            val buf = StringBuilder(outputLength)
            buf.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B')
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                buf.append(StringUtil.NEWLINE)
                ByteBufUtil.appendPrettyHexDump(buf, msg)
            }
            buf.toString()
        }
    }

    /**
     * Generates the default log message of the specified event whose argument is a [ByteBufHolder].
     */
    private fun formatByteBufHolder(
        ctx: ChannelHandlerContext,
        eventName: String,
        msg: ByteBufHolder
    ): String? {
        val chStr = ctx.channel().toString()
        val msgStr = msg.toString()
        val content = msg.content()
        val length = content.readableBytes()
        return if (length == 0) {
            val buf = StringBuilder(chStr.length + 1 + eventName.length + 2 + msgStr.length + 4)
            buf.append(chStr).append(' ').append(eventName).append(", ").append(msgStr)
                .append(", 0B")
            buf.toString()
        } else {
            var outputLength = chStr.length + 1 + eventName.length + 2 + msgStr.length + 2 + 10 + 1
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                val rows = length / 16 + (if (length % 15 == 0) 0 else 1) + 4
                val hexDumpLength = 2 + rows * 80
                outputLength += hexDumpLength
            }
            val buf = StringBuilder(outputLength)
            buf.append(chStr).append(' ').append(eventName).append(": ")
                .append(msgStr).append(", ").append(length).append('B')
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                buf.append(StringUtil.NEWLINE)
                ByteBufUtil.appendPrettyHexDump(buf, content)
            }
            buf.toString()
        }
    }

    /**
     * Generates the default log message of the specified event whose argument is an arbitrary object.
     */
    private fun formatSimple(ctx: ChannelHandlerContext, eventName: String, msg: Any): String? {
        val chStr = ctx.channel().toString()
        val msgStr = msg.toString()
        val buf = StringBuilder(chStr.length + 1 + eventName.length + 2 + msgStr.length)
        return buf.append(chStr).append(' ').append(eventName).append(": ").append(msgStr)
            .toString()
    }
}