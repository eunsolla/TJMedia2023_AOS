package com.verse.app.ui.search.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemSearchKeywordPopularBinding
import com.verse.app.extension.setReWidth
import com.verse.app.model.search.PopularKeywordModel
import com.verse.app.utility.provider.DeviceProvider

/**
 * Description : 검색어 입력화면 > 인기 검색어 ViewHolder
 *
 * Created by juhongmin on 2023/05/09
 */
class SearchKeywordPopularViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    itemBinding: ItemSearchKeywordPopularBinding = createBinding(
        parent,
        R.layout.item_search_keyword_popular,
        viewModel
    )
) : BaseViewHolder<PopularKeywordModel>(
    itemBinding
) {

    private val deviceProvider: DeviceProvider by lazy { entryPoint.deviceProvider() }

    init {
        val deviceWidth = deviceProvider.getDeviceWidth()
        val ratioWidth = (deviceWidth.toFloat() * (236.0 / 375.0)).toInt()
        itemBinding.root.setReWidth(ratioWidth)
    }
}
