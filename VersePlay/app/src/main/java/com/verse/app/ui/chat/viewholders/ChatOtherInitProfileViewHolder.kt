package com.verse.app.ui.chat.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemChatInitProfileBinding
import com.verse.app.model.chat.ChatInitOtherProfileModel
import com.verse.app.ui.chat.message.ChatMessageActivityViewModel

/**
 * Description : 채팅 > [채팅할_사용자_프로필]
 *
 * Created by juhongmin on 2023/06/17
 */
class ChatOtherInitProfileViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemChatInitProfileBinding = createBinding(
        parent,
        R.layout.item_chat_init_profile,
        viewModel
    )
) : BaseViewHolder<ChatInitOtherProfileModel>(itemBinding) {

    init {
        itemBinding.tvProfile.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            moveToTargetProfile(data)
        }
    }

    private fun moveToTargetProfile(model: ChatInitOtherProfileModel) {
        if (viewModel is ChatMessageActivityViewModel) {
            viewModel.moveToProfile(model.item)
        }
    }
}
