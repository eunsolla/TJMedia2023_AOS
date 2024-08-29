package com.verse.app.model.chat

import android.net.Uri
import com.verse.app.contants.Config
import com.verse.app.contants.ListPagedItemType
import com.verse.app.extension.getChatMessageTime
import com.verse.app.model.base.BaseModel
import com.verse.app.model.chat_recv.ResponseResourcePath

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/15
 */
data class ChatMyPhotoModel(
    val roomCode: String,
    val imagePath: String,
    val sendTime: String,
    var isReadMessage: Boolean = false // true: 읽음, false: 읽지 않음
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_CHAT_MY_PHOTO
    }

    override fun getClassName(): String {
        return "ChatMyPhotoModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMyPhotoModel) {
            imagePath == diffUtil.imagePath
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMyPhotoModel) {
            this == diffUtil
        } else {
            false
        }
    }

    companion object {
        fun getRealImageUrl(entity: ChatMessageModel): String {
            return try {
                val uri = Uri.parse(Config.BASE_FILE_URL.plus(entity.chatMsg))
                uri.buildUpon().clearQuery().build().toString()
            } catch (ex: Exception) {
                ""
            }
        }

        fun getRealImageUrl(entity: ResponseResourcePath): String {
            return try {
                val uri = Uri.parse(Config.BASE_FILE_URL.plus(entity.uploadPath))
                uri.buildUpon().clearQuery().build().toString()
            } catch (ex: Exception) {
                ""
            }
        }
    }

    constructor(
        roomCode: String,
        entity: ChatMessageModel,
        language: String
    ) : this(
        roomCode = roomCode,
        imagePath = getRealImageUrl(entity),
        sendTime = getChatMessageTime(entity.chatTs, language),
        isReadMessage = entity.chatReadYn == "Y"
    )

    constructor(
        roomCode: String,
        entity: ResponseResourcePath,
        language: String
    ) : this(
        roomCode = roomCode,
        imagePath = getRealImageUrl(entity),
        sendTime = getChatMessageTime(entity.timeStamp, language),
        isReadMessage = false
    )
}
