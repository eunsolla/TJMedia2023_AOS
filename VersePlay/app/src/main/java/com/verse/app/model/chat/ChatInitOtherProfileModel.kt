package com.verse.app.model.chat

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/17
 */
data class ChatInitOtherProfileModel(
    val item: ChatMessageIntentModel
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_CHAT_INIT_OTHER_PROFILE
    }

    override fun getClassName(): String {
        return "ChatInitMyProfileModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatInitOtherProfileModel) {
            item.targetMemberCode == diffUtil.item.targetMemberCode
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatInitOtherProfileModel) {
            item == diffUtil.item
        } else {
            false
        }
    }

    var imagePath: String? = null
        get() {
            if (field == null) {
                field = item.targetProfileImagePath
            }
            return field
        }

    var desc: String? = null
        get() {
            if (field == null) {
                field = if (item.targetFollowerCount.isEmpty() && item.targetFeedCount.isEmpty()) {
                    ""
                } else {
                    val str = StringBuilder()
                    str.append("팔로워 ${item.targetFollowerCount}명")
                    str.append("·")
                    str.append("게시물 ${item.targetFeedCount}개")
                    str.append("\n")
                    str.toString()
                }
            }
            return field
        }
}