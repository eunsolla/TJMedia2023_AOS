package com.verse.app.ui.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.verse.app.base.adapter.BaseListAdapter
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ListPagedItemType
import com.verse.app.databinding.ItemLyricsBinding
import com.verse.app.databinding.ItemSingPassBinding
import com.verse.app.databinding.ItemSingPassListBinding
import com.verse.app.model.base.BaseModel
import com.verse.app.model.singpass.GenreList
import com.verse.app.model.singpass.GenreRankingList
import com.verse.app.model.xtf.XTF_LYRICE_DTO
import com.verse.app.ui.chat.viewholders.ChatMyMessageViewHolder
import com.verse.app.ui.chat.viewholders.ChatMyPhotoViewHolder
import com.verse.app.ui.chat.viewholders.ChatOtherInitProfileViewHolder
import com.verse.app.ui.chat.viewholders.ChatOtherMessageViewHolder
import com.verse.app.ui.chat.viewholders.ChatOtherPhotoViewHolder
import com.verse.app.ui.chat.viewholders.ChatRoomViewHolder
import com.verse.app.ui.community.viewholders.CommunityEventViewHolder
import com.verse.app.ui.community.viewholders.CommunityLoungeViewHolder
import com.verse.app.ui.community.viewholders.CommunityVoteViewHolder
import com.verse.app.ui.feed.viewholders.FeedViewHolder
import com.verse.app.ui.lounge.viewholders.LoungeGalleryViewHolder
import com.verse.app.ui.mypage.viewholder.MyPageFeedContentsViewHolder
import com.verse.app.ui.mypage.viewholder.MyPageRecommendUserViewHolder
import com.verse.app.ui.search.viewholders.NowLoveSongViewHolder
import com.verse.app.ui.search.viewholders.SearchKeywordPopularViewHolder
import com.verse.app.ui.search.viewholders.SearchSongViewHolder

/**
 * Description :
 *
 * Created by jhlee on 2023-02-03
 */
class CommonListAdapter<T : BaseModel>(
    viewModel: BaseViewModel,
    viewType: ListPagedItemType,
) : BaseListAdapter<T>(

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
            ListPagedItemType.MAIN_SING_PASS -> {
                SingPassViewHolder(
                    BaseViewHolder.createBinding(
                        parent,
                        ListPagedItemType.values()[viewType].layoutId,
                        viewModel
                    )
                ) as BaseViewHolder<T>
            }

            ListPagedItemType.MAIN_SING_PASS_LIST -> {
                SingPassRankingViewHolder(
                    BaseViewHolder.createBinding(
                        parent,
                        ListPagedItemType.values()[viewType].layoutId,
                        viewModel
                    )
                ) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_LYRICS,
            ListPagedItemType.ITEM_LYRICS_VIDEO -> {
                LyricsViewHolder(
                    BaseViewHolder.createBinding(
                        parent,
                        ListPagedItemType.values()[viewType].layoutId,
                        viewModel
                    )
                ) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_SEARCH_KEYWORD_POPULAR -> {
                SearchKeywordPopularViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_SEARCH_POPULAR_SONG -> {
                SearchSongViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_SEARCH_LOVELY_SONG -> {
                NowLoveSongViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_COMMUNITY_EVENT -> {
                CommunityEventViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_COMMUNITY_VOTE -> {
                CommunityVoteViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_LOUNGE_GALLERY -> {
                LoungeGalleryViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_MY_PAGE_FEED -> {
                MyPageFeedContentsViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.MY_PAGE_RECOMMEND -> {
                MyPageRecommendUserViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.FEED -> {
                FeedViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_CHAT_ROOM -> {
                ChatRoomViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_CHAT_MY_MESSAGE -> {
                ChatMyMessageViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_CHAT_MY_PHOTO -> {
                ChatMyPhotoViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_CHAT_OTHER_MESSAGE -> {
                ChatOtherMessageViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_CHAT_OTHER_PHOTO -> {
                ChatOtherPhotoViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_CHAT_INIT_OTHER_PROFILE -> {
                ChatOtherInitProfileViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

            ListPagedItemType.ITEM_COMMUNITY_LOUNGE -> {
                CommunityLoungeViewHolder(parent, viewModel) as BaseViewHolder<T>
            }

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
) {

    override fun onBindViewHolder(
        holder: BaseViewHolder<T>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.size == 0) {
            this.onBindViewHolder(holder, position)
        } else {
            if (payloads[0] is Boolean) {
                try {
                    holder.bindPayload()
                } catch (ex: Exception) {
                    // ignore
                }
            }
        }
    }

    /**
     * SingPass
     */
    data class SingPassViewHolder(val binding: ItemSingPassBinding) :
        BaseViewHolder<GenreList>(binding) {

        override fun bind(data: GenreList) {
            defaultExpandView()
            super.bind(data)
        }

        /**
         * onViewDetachedToWindow -> sheet 접음, 스크롤 위치 0
         */
        private fun onCollapsed() {
            BottomSheetBehavior.from(binding.singPassConstraintLayout)?.let {
                it.state = BottomSheetBehavior.STATE_COLLAPSED
                //스크롤 0
                binding.rankRecyclerView?.layoutManager?.let { rv ->
                    rv.scrollToPosition(0)
                }
            }
        }

        fun defaultExpandView() {
            BottomSheetBehavior.from(binding.singPassConstraintLayout)?.let {
                it.state = BottomSheetBehavior.STATE_COLLAPSED

                //it.peekHeight = 305.dp

                //스크롤 0
                binding.rankRecyclerView?.layoutManager?.let { rv ->
                    rv.scrollToPosition(0)
                }
            }
        }

        override fun onViewAttachedToWindow(holder: BaseViewHolder<GenreList>) {
            super.onViewAttachedToWindow(holder)
        }

        override fun onViewDetachedToWindow(holder: BaseViewHolder<GenreList>) {
            onCollapsed()
            super.onViewDetachedToWindow(holder)
        }

        override fun onViewRecycled() {
            super.onViewRecycled()
        }
    }

    /**
     * Sing Pass Main Ranking List ViewHolder
     */
    data class SingPassRankingViewHolder(val binding: ItemSingPassListBinding) :
        BaseViewHolder<GenreRankingList>(binding) {

        override fun bind(data: GenreRankingList) {
            binding.apply {
                super.bind(data)
            }
        }

        override fun onViewAttachedToWindow(holder: BaseViewHolder<GenreRankingList>) {
            super.onViewAttachedToWindow(holder)

            bindingAdapter?.itemCount.let {
                if (it!! > 3) {
                    binding.clJoinArea.visibility = View.GONE
                } else {
                    if (bindingAdapterPosition == it!!.minus(1)) {
                        binding.clJoinArea.visibility = View.VISIBLE
                    } else {
                        binding.clJoinArea.visibility = View.GONE
                    }
                }
            }

        }

        override fun onViewDetachedToWindow(holder: BaseViewHolder<GenreRankingList>) {
            super.onViewDetachedToWindow(holder)
        }

        override fun onViewRecycled() {
            super.onViewRecycled()
        }
    }

    /**
     * 부르기 가사 홀더
     */
    class LyricsViewHolder(val binding: ItemLyricsBinding) :
        BaseViewHolder<XTF_LYRICE_DTO>(binding) {
        override fun bind(data: XTF_LYRICE_DTO) {
            super.bind(data)
        }
    }
}
