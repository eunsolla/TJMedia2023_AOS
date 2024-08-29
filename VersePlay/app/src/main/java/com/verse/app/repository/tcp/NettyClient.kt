package com.verse.app.repository.tcp

import com.verse.app.repository.tcp.handler.ChatListener
import io.reactivex.rxjava3.core.Single

/**
 * Description : TCP 통신하는 Client
 *
 * Created by juhongmin on 2023/06/12
 */
interface NettyClient : OnReceiveMessage {
    fun start(roomCode: String, targetMemberCode: String): Single<Boolean>
    fun setListener(listener: ChatListener)
    fun clearListener()
    fun send(data: BaseTcpData)
    fun closeClient()
}