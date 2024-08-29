package com.verse.app.ui.lounge.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemLoungeGalleryBinding
import com.verse.app.model.lounge.LoungeGalleryData
import com.verse.app.ui.lounge.modify.LoungeModifyFragmentViewModel
import com.verse.app.ui.lounge.write.LoungeWriteFragmentViewModel

/**
 * Description : 라운지에 등록하는 갤러리 전용 ViewHolder
 *
 * Created by juhongmin on 2023/05/18
 */
class LoungeGalleryViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemLoungeGalleryBinding = createBinding(
        parent,
        R.layout.item_lounge_gallery,
        viewModel
    )
) : BaseViewHolder<LoungeGalleryData>(itemBinding) {

    interface Listener {
        fun onGalleryRemove(data: LoungeGalleryData)
    }

    init {

        itemBinding.icRemove.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener

            if (viewModel is LoungeWriteFragmentViewModel) {
                viewModel.onGalleryRemove(data)
            } else if (viewModel is LoungeModifyFragmentViewModel) {
                viewModel.onGalleryRemove(data)
            }
        }

    }
}