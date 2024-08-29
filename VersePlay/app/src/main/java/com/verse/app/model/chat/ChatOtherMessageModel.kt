package com.verse.app.model.chat

import com.verse.app.contants.ListPagedItemType
import com.verse.app.extension.getChatMessageTime
import com.verse.app.model.base.BaseModel
import com.verse.app.model.chat_recv.ReceiveChatMessage

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/15
 */
data class ChatOtherMessageModel(
    val msg: String,
    val sendTime: String,
    val profileImagePath: String,
    val isGroups: Boolean = false
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_CHAT_OTHER_MESSAGE
    }

    override fun getClassName(): String {
        return "ChatOtherMessageModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatOtherMessageModel) {
            sendTime == diffUtil.sendTime
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatOtherMessageModel) {
            this == diffUtil
        } else {
            false
        }
    }

    constructor(
        entity: ChatMessageModel,
        language: String,
        prevModel: BaseModel?
    ) : this(
        msg = entity.chatMsg,
        sendTime = getChatMessageTime(entity.chatTs, language),
        profileImagePath = entity.profileImagePath,
        isGroups = prevModel is ChatOtherMessageModel ||
                prevModel is ChatOtherPhotoModel
    )

    constructor(
        entity: ReceiveChatMessage,
        language: String,
        profileImagePath: String,
        prevModel: BaseModel?
    ) : this(
        msg = entity.msg,
        sendTime = getChatMessageTime(entity.timeStamp, language),
        profileImagePath = profileImagePath,
        isGroups = prevModel is ChatOtherMessageModel ||
                prevModel is ChatOtherPhotoModel
    )

    var msgText: String? = null
        get() {
            if (field == null) {
                field = msg.replace("<br>", "\n")
            }
            return field
        }
}
