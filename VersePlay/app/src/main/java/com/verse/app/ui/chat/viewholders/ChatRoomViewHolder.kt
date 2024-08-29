package com.verse.app.ui.chat.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemChatRoomBinding
import com.verse.app.model.chat.ChatMemberRoomModel
import com.verse.app.ui.chat.rooms.ChatRoomsActivityViewModel

/**
 * Description : 메시지함
 *
 * Created by juhongmin on 2023/06/15
 */
class ChatRoomViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemChatRoomBinding = createBinding(
        parent,
        R.layout.item_chat_room,
        viewModel
    )
) : BaseViewHolder<ChatMemberRoomModel>(itemBinding) {

    init {
        itemBinding.root.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            if (viewModel is ChatRoomsActivityViewModel) {
                viewModel.moveToChatMessage(data)
            }
        }
        itemBinding.ivMore.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            if (viewModel is ChatRoomsActivityViewModel) {
                viewModel.showRoomModifyDialog(data)
            }
        }
    }
}
