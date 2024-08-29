package com.verse.app.ui.chat.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemChatOtherMessageBinding
import com.verse.app.model.chat.ChatOtherMessageModel

/**
 * Description : 채팅 > [이외_사용자_메시지]
 *
 * Created by juhongmin on 2023/06/15
 */
class ChatOtherMessageViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemChatOtherMessageBinding = createBinding(
        parent,
        R.layout.item_chat_other_message,
        viewModel
    )
) : BaseViewHolder<ChatOtherMessageModel>(itemBinding) {

}
