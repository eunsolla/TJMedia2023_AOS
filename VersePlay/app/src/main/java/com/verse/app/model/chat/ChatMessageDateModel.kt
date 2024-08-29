package com.verse.app.model.chat

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description : 채팅 > 중간 중간 나타내는 날짜
 *
 * Created by juhongmin on 2023/06/15
 */
data class ChatMessageDateModel(
    val date: String
) : BaseModel(){
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_CHAT_MESSAGE_DATE
    }

    override fun getClassName(): String {
        return "ChatMessageDateModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMessageDateModel) {
            date == diffUtil.date
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMessageDateModel) {
            date == diffUtil.date
        } else {
            false
        }
    }
}