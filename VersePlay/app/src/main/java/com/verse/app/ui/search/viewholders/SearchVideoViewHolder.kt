package com.verse.app.ui.search.viewholders

import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemSearchVideoBinding
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.search.viewmodel.SearchResultPopularViewModel
import com.verse.app.ui.search.viewmodel.SearchResultVideoViewModel
import com.verse.app.utility.manager.FeedContentsHitManager

/**
 * Description : 검색 결과 > 관련 게시물 ViewHolder
 *
 * Created by juhongmin on 2023/05/11
 */
class SearchVideoViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemSearchVideoBinding = createBinding(
        parent,
        R.layout.item_search_video,
        viewModel
    )
) : BaseViewHolder<FeedContentsData>(itemBinding), LifecycleEventObserver {

    init {

        itemView.doOnAttach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnAttach
            owner.lifecycle.addObserver(this)
        }

        itemView.doOnDetach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnDetach
            owner.lifecycle.removeObserver(this)
        }

        itemBinding.root.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToContents(model)
        }
    }

    private fun moveToContents(data: FeedContentsData) {
        if (viewModel is SearchResultVideoViewModel) {
            viewModel.onMoveToFeed(bindingAdapterPosition, data)
        } else if (viewModel is SearchResultPopularViewModel) {
            viewModel.onMoveToFeed(bindingAdapterPosition, data)
        }

        setHitCount(data)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            if (itemView.isAttachedToWindow) {
                onRefreshHitCount()
            }
        }
    }

    private fun onRefreshHitCount() {
        itemBinding.data?.runCatching {
            if (FeedContentsHitManager.isPrevHitCount(feedMngCd)) {
                hitCnt = FeedContentsHitManager.getHitCount(feedMngCd).toString()
                itemBinding.invalidateAll()
            }
        }
    }

    /**
     * 피드 선택시 카운팅 처리하는 함수
     */
    private fun setHitCount(data: FeedContentsData) {
        FeedContentsHitManager.addHitCount(data.feedMngCd, data.hitCnt)
    }
}