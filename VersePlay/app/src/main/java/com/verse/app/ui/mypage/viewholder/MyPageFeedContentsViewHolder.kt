package com.verse.app.ui.mypage.viewholder

import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemMyPageFeedBinding
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.feed.MyPageFeedContentsData
import com.verse.app.ui.mypage.bookmark.MyPageBookmarkFeedFragmentViewModel
import com.verse.app.ui.mypage.like.MyPageLikeUserNormalFragmentViewModel
import com.verse.app.ui.mypage.like.MyPageLikeUserPerformanceFragmentViewModel
import com.verse.app.ui.mypage.upload.MyPageCollectionFragmentViewModel
import com.verse.app.ui.mypage.upload.MyPagePerformanceFragmentViewModel
import com.verse.app.ui.mypage.viewmodel.MypagePrivateViewModel
import com.verse.app.utility.manager.FeedContentsHitManager

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/27
 */
class MyPageFeedContentsViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemMyPageFeedBinding = createBinding(
        parent,
        R.layout.item_my_page_feed,
        viewModel
    )
) : BaseViewHolder<MyPageFeedContentsData>(itemBinding), LifecycleEventObserver {

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
            val data = itemBinding.data ?: return@setOnClickListener
            val pos = bindingAdapterPosition
            when (viewModel) {
                is MyPagePerformanceFragmentViewModel -> {
                    viewModel.onMoveToFeed(pos, data)
                    setHitCount(data)
                }

                is MyPageCollectionFragmentViewModel -> {
                    viewModel.onMoveToFeed(pos, data)
                    setHitCount(data)
                }

                is MyPageLikeUserPerformanceFragmentViewModel -> {
                    viewModel.onMoveToFeed(pos, data)
                    setHitCount(data)
                }

                is MyPageLikeUserNormalFragmentViewModel -> {
                    viewModel.onMoveToFeed(pos, data)
                    setHitCount(data)
                }

                is MyPageBookmarkFeedFragmentViewModel -> {
                    viewModel.onMoveToFeed(pos, data)
                    setHitCount(data)
                }

                is MypagePrivateViewModel -> {
                    viewModel.onMoveToFeed(pos, data)
                    setHitCount(data)
                }
            }
        }
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
