package com.verse.app.model.chat

import com.verse.app.contants.ListPagedItemType
import com.verse.app.extension.getChatMessageTime
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/15
 */
data class ChatMyMessageModel(
    val roomCode: String,
    val msg: String,
    var sendTime: String,
    var isReadMessage: Boolean = false // true: 읽음, false: 읽지 않음
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_CHAT_MY_MESSAGE
    }

    override fun getClassName(): String {
        return "ChatMyMessageModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMyMessageModel) {
            sendTime == diffUtil.sendTime
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMyMessageModel) {
            this == diffUtil
        } else {
            false
        }
    }

    constructor(
        roomCode: String,
        entity: ChatMessageModel,
        language: String
    ) : this(
        roomCode = roomCode,
        msg = entity.chatMsg,
        sendTime = getChatMessageTime(entity.chatTs, language),
        isReadMessage = entity.chatReadYn == "Y"
    )

    var msgText: String? = null
        get() {
            if (field == null) {
                field = msg.replace("<br>", "\n")
            }
            return field
        }
}