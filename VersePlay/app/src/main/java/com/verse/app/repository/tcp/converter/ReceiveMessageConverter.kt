package com.verse.app.repository.tcp.converter

import com.verse.app.contants.TcpHeaderType
import com.verse.app.extension.multiNullCheck
import com.verse.app.extension.toDoubleOrDef
import com.verse.app.extension.toEmptyStr
import com.verse.app.extension.toIntOrDef
import com.verse.app.extension.toLongOrDef
import com.verse.app.model.base.TcpOrder
import com.verse.app.model.chat_recv.ChatCheckSession
import com.verse.app.model.chat_recv.ChatReadMessage
import com.verse.app.model.chat_recv.ChatResponseCode
import com.verse.app.model.chat_recv.ReceiveChatMessage
import com.verse.app.model.chat_recv.ResponseChatConnectionInfo
import com.verse.app.model.chat_recv.ResponseResourcePath
import com.verse.app.model.chat_recv.ResponseSendChatMessage
import com.verse.app.utility.DLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.lang.reflect.Field
import java.nio.charset.Charset

/**
 * Description : ByteBuf -> BaseData Converter Factory
 *
 * Created by juhongmin on 6/10/21
 */
class ReceiveMessageConverter : MessageToMessageDecoder<ByteBuf>() {
    override fun decode(ctx: ChannelHandlerContext?, _msg: ByteBuf?, _out: MutableList<Any>?) {
        multiNullCheck(_msg, _out) { msg, out ->
            runCatching {
                val str = msg.toString(Charset.defaultCharset())
                if (!str.startsWith(TcpHeaderType.RECV_SESSION.key)) {
                    DLogger.d("=============[S Receive Message]================")
                    DLogger.d("Recv $str")
                    DLogger.d("=============[E Receive Message]================")
                }

                if (str.length > 1) {
                    when ("${str[0]}${str[1]}") {
                        TcpHeaderType.RECV_SESSION.key -> out.add(str.strToClass<ChatCheckSession>())
                        TcpHeaderType.RECV_CONNECTION.key -> out.add(str.strToClass<ResponseChatConnectionInfo>())
                        TcpHeaderType.RECV_MSG.key -> out.add(str.strToClass<ResponseSendChatMessage>())
                        TcpHeaderType.RECV_CHAT.key -> out.add(str.strToClass<ChatResponseCode>())
                        TcpHeaderType.RECV_CHAT_MSG.key -> out.add(str.strToClass<ReceiveChatMessage>())
                        TcpHeaderType.RECV_CHAT_READ.key -> out.add(str.strToClass<ChatReadMessage>())
                        TcpHeaderType.RECV_IMAGE_RESOURCE.key -> out.add(str.strToClass<ResponseResourcePath>())
                        else -> {
                            DLogger.e("Invalid Type $str")
                        }
                    }
                } else {
                    DLogger.e("Invalid Type $str")
                }
            }.onFailure {
                DLogger.e("Decode Converter ${it.message}")
            }
        }
    }

    /**
     * 문자열 -> 데이터 클래스 형변환 처리 함수.
     * @param separator 구분자
     * @param isTypAdded 문자열 앞에 SocketType 구분이 포함되어 있는지 유무
     *
     * @return hmju
     */
    inline fun <reified T : Any> String.strToClass(
        separator: Char = '|',
        isTypAdded: Boolean = true
    ): T {
        val obj = T::class.java.newInstance()
        val fieldMap = hashMapOf<Int, Field>()
        obj.javaClass.declaredFields.forEach {
            if (it.isAnnotationPresent(TcpOrder::class.java)) {
                fieldMap[it.getAnnotation(TcpOrder::class.java)?.num ?: 0] = it
            }
        }
        this.split(separator).forEachIndexed { index, str ->
            // 앞에 타입이 있는 경우 건너뜀
            if (isTypAdded && index == 0) return@forEachIndexed
            val startIndex = if (isTypAdded) index else index + 1
            runCatching {
                val field = fieldMap[startIndex] ?: return@forEachIndexed
                if (!field.isAccessible) field.isAccessible = true

                when {
                    field.type.isAssignableFrom(String::class.java) -> field.set(
                        obj,
                        str.toEmptyStr()
                    )

                    field.type.isAssignableFrom(Int::class.java) -> field.set(obj, str.toIntOrDef())
                    field.type.isAssignableFrom(Long::class.java) -> field.set(
                        obj,
                        str.toLongOrDef()
                    )

                    field.type.isAssignableFrom(Double::class.java) -> field.set(
                        obj,
                        str.toDoubleOrDef()
                    )
                }
            }
        }
        return obj
    }
}