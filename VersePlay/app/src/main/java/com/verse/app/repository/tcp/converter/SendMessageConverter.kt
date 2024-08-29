package com.verse.app.repository.tcp.converter

import com.verse.app.model.chat_send.ChatResponseSession
import com.verse.app.repository.tcp.BaseTcpData
import com.verse.app.utility.DLogger
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import java.nio.CharBuffer

/**
 * Description : 서버로 보내기 직전 BaseData 를 변환해주는 컴퍼터 클래스
 *
 * Created by hmju on 2021-06-11
 */
class SendMessageConverter : MessageToMessageEncoder<BaseTcpData>() {

    private val charset = Charsets.UTF_8

    override fun encode(ctx: ChannelHandlerContext?, msg: BaseTcpData?, out: MutableList<Any>?) {
        if (msg == null || out == null || ctx == null) return

        if (msg !is ChatResponseSession) {
            DLogger.d("SendMessage ${msg.classToStr()}")
        }

        val buffer = ByteBufUtil.encodeString(
            ctx.alloc(),
            CharBuffer.wrap(msg.classToStr().plus("\r\n")),
            charset
        )
        out.add(buffer)
    }
}