package com.verse.app.ui.search.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.CollectionType
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ItemSearchTagBinding
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.startAct
import com.verse.app.model.search.SearchResultTagData
import com.verse.app.ui.feed.activity.CollectionFeedActivity

/**
 * Description : 검색 결과 테그 ViewHolder
 *
 * Created by juhongmin on 2023/05/11
 */
class SearchTagViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemSearchTagBinding = createBinding(
        parent,
        R.layout.item_search_tag,
        viewModel
    )
) : BaseViewHolder<SearchResultTagData>(itemBinding) {

    init {
        itemView.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToTag(model)
        }
    }

    private fun moveToTag(data: SearchResultTagData) {
        itemView.getFragmentActivity()?.startAct<CollectionFeedActivity> {
            putExtra(ExtraCode.COLLECTION_TYPE, CollectionType.TAG.code)
            putExtra(ExtraCode.COLLECTION_PARAM, data.tagName)
        }
    }
}