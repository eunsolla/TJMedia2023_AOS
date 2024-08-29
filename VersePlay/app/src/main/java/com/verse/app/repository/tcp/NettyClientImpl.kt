package com.verse.app.repository.tcp

import com.verse.app.contants.Config.BASE_CHAT_PORT
import com.verse.app.contants.Config.BASE_CHAT_URL
import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.chat_recv.ChatCheckSession
import com.verse.app.model.chat_recv.ChatReadMessage
import com.verse.app.model.chat_recv.ChatResponseCode
import com.verse.app.model.chat_recv.ReceiveChatMessage
import com.verse.app.model.chat_recv.ResponseChatConnectionInfo
import com.verse.app.model.chat_recv.ResponseResourcePath
import com.verse.app.model.chat_recv.ResponseSendChatMessage
import com.verse.app.model.chat_send.ChatConnectionInfo
import com.verse.app.model.chat_send.ChatJoinUser
import com.verse.app.model.chat_send.ChatResponseSession
import com.verse.app.repository.tcp.converter.ReceiveMessageConverter
import com.verse.app.repository.tcp.converter.SendMessageConverter
import com.verse.app.repository.tcp.handler.ChatListener
import com.verse.app.repository.tcp.handler.ClientController
import com.verse.app.repository.tcp.receive.ChatCheckSessionReceiver
import com.verse.app.repository.tcp.receive.ChatReadMessageReceiver
import com.verse.app.repository.tcp.receive.ChatResponseCodeReceiver
import com.verse.app.repository.tcp.receive.ReceiveChatMessageReceiver
import com.verse.app.repository.tcp.receive.ResponseChatConnectionInfoReceiver
import com.verse.app.repository.tcp.receive.ResponseResourcePathReceiver
import com.verse.app.repository.tcp.receive.ResponseSendChatMessageReceiver
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPipeline
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLException

/**
 * Description : TCP 통신 구현체 클래스
 *
 * Created by juhongmin on 2023/06/12
 */
internal class NettyClientImpl @Inject constructor(
    private val loginManager: LoginManager
) : NettyClient {

    private var group: EventLoopGroup? = null
    private var channel: Channel? = null
    private var isSSLCheck = true
    private val host: String by lazy { BASE_CHAT_URL }
    private val port: Int by lazy { BASE_CHAT_PORT }
    private var chatListener: ChatListener? = null
    private var currentRoomCode: String = ""
    private var currentTargetMemberCode: String = ""

    override fun start(
        roomCode: String,
        targetMemberCode: String
    ): Single<Boolean> {
        currentRoomCode = roomCode
        currentTargetMemberCode = targetMemberCode
        return if (isConnection()) {
            closeClientSingle()
                .delay(500, TimeUnit.MILLISECONDS)
                .flatMap { startConnection(roomCode) }
        } else {
            startConnection(roomCode)
        }
    }

    /**
     * 서버가 켜져있는지 유무 상태
     */
    private fun isConnection(): Boolean {
        val _group = group
        return if (_group != null) {
            DLogger.d("서버와 연결상태 ${_group.isShutdown} 서버 종료중 ${_group.isShuttingDown}")
            if (_group.isShuttingDown) {
                true
            } else {
                !_group.isShutdown
            }
        } else {
            false
        }
    }

    /**
     * 서버 연결 하는 함수
     * @param roomCode 방 관리 번호
     */
    private fun startConnection(roomCode: String): Single<Boolean> {
        return Single.create { emitter ->
            try {
                group = NioEventLoopGroup()
                val bootstrap = Bootstrap()
                bootstrap.group(group)
                bootstrap.channel(NioSocketChannel::class.java)
                initOptions(bootstrap)
                bootstrap.handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel?) {
                        if (ch == null) {
                            emitter.onError(NullPointerException("Channel is Null"))
                            return
                        }
                        setPipeline(ch, roomCode, emitter)
                    }
                })
                // start server connection
                // TEST
                // channel = bootstrap.connect("stg-chat.verseplay.com", 802).sync().channel()
                channel = bootstrap.connect(host, port).sync().channel()
            } catch (ex: Exception) {
                emitter.onSuccess(false)
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun setListener(listener: ChatListener) {
        chatListener = listener
    }

    override fun clearListener() {
        chatListener = null
    }

    override fun send(data: BaseTcpData) {
        channel?.runCatching {
            write(data)
            flush()
        }
    }

    override fun closeClient() {
        DLogger.d("Request Close Client!!!! $group")

        group?.shutdownGracefully()?.addListener {
            channel?.closeFuture()
            group = null
            channel = null
        }
    }

    private fun closeClientSingle(): Single<Unit> {
        return Single.create { emitter ->
            try {
                group?.shutdownGracefully()?.addListener {
                    channel?.closeFuture()
                    group = null
                    channel = null
                    emitter.onSuccess(Unit)
                } ?: run {
                    emitter.onSuccess(Unit)
                }
            } catch (ex: Exception) {
                emitter.onSuccess(Unit)
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun onHandleDisconnected() {
        DLogger.d("onHandleDisconnected")
        closeClient()
    }

    override fun onException(err: Throwable?) {
        DLogger.d("onException $err")
        closeClient()
    }

    private fun initSslHandler(alloc: ByteBufAllocator): SslHandler? {
        return try {
            val sslContextBuilder = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
            return sslContextBuilder.newHandler(alloc)
        } catch (ex: SSLException) {
            DLogger.d("NettyClient", "ERROR $ex")
            null
        } catch (ex: Error) {
            DLogger.d("NettyClient", "ERROR $ex")
            null
        }
    }

    private fun sendConnectionInfo(roomCode: String) {
        val data = ChatConnectionInfo(
            roomCode = roomCode,
            memberCode = loginManager.getUserLoginData().memCd
        )
        send(data)
    }

    // [s] TCP Callback
    override fun onCheckSession(msg: ChatCheckSession) {
        // SK
        send(ChatResponseSession(memberCode = loginManager.getUserLoginData().memCd))
    }

    override fun onChatReadMessage(msg: ChatReadMessage) {
        // SG
        chatListener?.onReadAllMsg()
    }

    override fun onChatResponseCode(msg: ChatResponseCode) {
        // SE
        if (msg.code == 5000) {
            chatListener?.onSuccessPacket()
        }
    }

    override fun onReceiveChatMessage(msg: ReceiveChatMessage) {
        // SM
        chatListener?.onReceiveMessage(msg)
    }

    override fun onResponseChatConnectionInfo(msg: ResponseChatConnectionInfo) {
        // SR
        if (msg.code == 2000) {
            val packet = ChatJoinUser(
                roomCode = currentRoomCode,
                memberCode = loginManager.getUserLoginData().memCd,
                targetMemberCode = currentTargetMemberCode
            )
            send(packet)
        }
    }

    override fun onResponseResourcePath(msg: ResponseResourcePath) {
        // SP
        chatListener?.onUploadImagePath(msg)
    }

    override fun onResponseSendChatMessage(msg: ResponseSendChatMessage) {
        // SL
        chatListener?.onSendedMessage(msg)
    }
    // [s] TCP Callback

    // [s] private functions
    private fun initOptions(bootstrap: Bootstrap) {
        bootstrap.option(ChannelOption.TCP_NODELAY, true)
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        bootstrap.option(ChannelOption.SO_SNDBUF, 2048)
        bootstrap.option(ChannelOption.SO_RCVBUF, 2048)
    }

    /**
     * Set Pipeline 처리하는 함수
     * @param ch 소켓 채널
     * @param roomCode 방 번호
     * @param emitter 서버와 통신을 할수 있는 상태면 리턴하는 리스너
     */
    private fun setPipeline(ch: SocketChannel, roomCode: String, emitter: SingleEmitter<Boolean>) {
        val pipeline = ch.pipeline()
        if (isSSLCheck) {
            // 개발중에는 Logger 추가 충분히 테스트 이후에는 삭제할 예정
            val sslHandler = initSslHandler(ch.alloc())
            // pipeline.addFirst("ssl", sslHandler)
            // pipeline.addAfter("ssl", "init", LoggingHandler())
            // pipeline.addFirst("logger", LoggingHandler())
            // pipeline.addAfter("logger", "init", sslHandler)
            pipeline.addFirst("init", sslHandler)
            sslHandler?.handshakeFuture()?.addListener { handShake ->
                DLogger.d("Handle Shake ${handShake.isSuccess}  ${ch.isWritable}")
                if (handShake.isSuccess) {
                    emitter.onSuccess(true)
                    if (ch.isWritable) {
                        handleConnectionInfo(roomCode)
                    } else {
                        handleConnectionInfo(roomCode, 500L)
                    }
                } else {
                    emitter.onSuccess(false)
                }
            }
        } else {
            // 개발중에는 Logger 추가 충분히 테스트 이후에는 삭제할 예정
            // pipeline.addFirst("logger", LoggingHandler())
//            pipeline.addAfter("logger", "init", object : ChannelInboundHandlerAdapter() {
//                override fun channelActive(ctx: ChannelHandlerContext?) {
//                    super.channelActive(ctx)
//                    emitter.onSuccess(true)
//                    handleConnectionInfo(roomCode)
//                }
//            })
            pipeline.addFirst("init", object : ChannelInboundHandlerAdapter() {
                override fun channelActive(ctx: ChannelHandlerContext?) {
                    super.channelActive(ctx)
                    emitter.onSuccess(true)
                    handleConnectionInfo(roomCode)
                }
            })
        }
        pipeline.addAfter("init", "control", ClientController(this))
        pipeline.addAfter(
            "control",
            "delimiter",
            DelimiterBasedFrameDecoder(2048, *Delimiters.lineDelimiter())
        )
        pipeline.addAfter("delimiter", "receive", ReceiveMessageConverter())
        pipeline.addReceiver(TcpHeaderType.RECV_SESSION, ChatCheckSessionReceiver(this))
        pipeline.addReceiver(TcpHeaderType.RECV_CHAT_READ, ChatReadMessageReceiver(this))
        pipeline.addReceiver(TcpHeaderType.RECV_CHAT, ChatResponseCodeReceiver(this))
        pipeline.addReceiver(TcpHeaderType.RECV_CHAT_MSG, ReceiveChatMessageReceiver(this))
        pipeline.addReceiver(
            TcpHeaderType.RECV_CONNECTION,
            ResponseChatConnectionInfoReceiver(this)
        )
        pipeline.addReceiver(TcpHeaderType.RECV_IMAGE_RESOURCE, ResponseResourcePathReceiver(this))
        pipeline.addReceiver(TcpHeaderType.RECV_MSG, ResponseSendChatMessageReceiver(this))
        pipeline.addLast(SendMessageConverter())
    }

    /**
     * Receive Add 쉽게 할수 있는 함수
     * @param type 이벤트 이름이 될 EnumClass
     * @param receiver Receiver Handler
     */
    private fun ChannelPipeline.addReceiver(
        type: TcpHeaderType,
        receiver: SimpleChannelInboundHandler<out BaseTcpData>
    ) {
        addAfter("receive", type.key, receiver)
    }

    /**
     * 접속 정보 패킷 처리하는 함수
     */
    private fun handleConnectionInfo(roomCode: String, delay: Long = 0L) {
        if (delay == 0L) {
            sendConnectionInfo(roomCode)
        } else {
            Single.just(roomCode)
                .delay(delay, TimeUnit.MILLISECONDS)
                .doOnSuccess { sendConnectionInfo(roomCode) }
                .subscribe()
        }
    }

    // [e] private functions
}
