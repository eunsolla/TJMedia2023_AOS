package com.verse.app.ui.chat.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemChatOtherPhotoBinding
import com.verse.app.extension.getFragmentActivity
import com.verse.app.gallery.ui.GalleryImageDetailBottomSheetDialog
import com.verse.app.model.chat.ChatOtherPhotoModel

/**
 * Description : 채팅 > [이외_사용자_사진]
 *
 * Created by juhongmin on 2023/06/15
 */
class ChatOtherPhotoViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemChatOtherPhotoBinding = createBinding(
        parent,
        R.layout.item_chat_other_photo,
        viewModel
    )
) : BaseViewHolder<ChatOtherPhotoModel>(itemBinding) {
    init {
        itemBinding.ivThumb.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            showFullImage(data)
        }
    }

    private fun showFullImage(data: ChatOtherPhotoModel) {
        val act = itemView.getFragmentActivity() ?: return
        GalleryImageDetailBottomSheetDialog()
            .setImageUrl(data.imagePath)
            .simpleShow(act.supportFragmentManager)
    }
}
