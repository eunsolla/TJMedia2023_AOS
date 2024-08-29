package com.verse.app.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.verse.app.base.adapter.BasePagingAdapter
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.ui.feed.viewholders.FeedViewHolder
import com.verse.app.ui.mypage.viewholder.MyPageAccompanimentViewHolder
import com.verse.app.ui.mypage.viewholder.MyPageFeedContentsViewHolder
import com.verse.app.ui.mypage.viewholder.MyPageFollowListViewHolder
import com.verse.app.ui.search.viewholders.SearchSongViewHolder
import com.verse.app.ui.search.viewholders.SearchTagViewHolder
import com.verse.app.ui.search.viewholders.SearchUserViewHolder
import com.verse.app.ui.search.viewholders.SearchVideoViewHolder


/**
 * Description : 공통 PagingDataAdapter
 *
 * Created by jhlee on 2023-02-03
 */
open class CommonPagingAdapter<T : BaseModel>(
    viewModel: BaseViewModel,
    viewType: ListPagedItemType,
) : BasePagingAdapter<T>(
    diffUtil = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return if (oldItem.getClassName() == newItem.getClassName()) {
                oldItem.areItemsTheSame(newItem)
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return if (oldItem.getClassName() == newItem.getClassName()) {
                oldItem.areContentsTheSame(newItem)
            } else {
                false
            }
        }
    },
    viewHolderFactory = { viewType, parent ->
        when (ListPagedItemType.values()[viewType]) {
            ListPagedItemType.ITEM_PRIVATE_FEED,
            ListPagedItemType.ITEM_MY_PAGE_FEED -> {
                MyPageFeedContentsViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.FEED -> {
                FeedViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_SEARCH_VIDEO,
            ListPagedItemType.ITEM_SEARCH_CONTENT -> {
                SearchVideoViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_SEARCH_POPULAR_SONG -> {
                SearchSongViewHolder(parent, viewModel) as BaseViewHolder<T>
            }
            ListPagedItemType.ITEM_SEARCH_TAG -> {
                SearchTagViewHolder(parent, viewModel) as BaseViewHolder<T>
            }
            ListPagedItemType.ITEM_SEARCH_USER -> {
                SearchUserViewHolder(parent, viewModel) as BaseViewHolder<T>
            }
            ListPagedItemType.ACCOMPANIMENT -> {
                MyPageAccompanimentViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.FOLLOW_LIST -> {
                MyPageFollowListViewHolder(parent, viewModel) as BaseViewHolder<T>
            }


//            ListPagedItemType.MYPAGE_SETTING_SECURITY -> {
//                MypageLoginDeviceViewHolder(parent, viewModel) as BaseViewHolder<T>
//            }

            else -> BaseViewHolder.create(
                parent,
                ListPagedItemType.values()[viewType].layoutId,
                viewModel
            )
        }
    },
    viewTypeFactory = { viewData ->
        viewData.itemViewType = viewType
        viewData.getViewType().ordinal
    }
)



