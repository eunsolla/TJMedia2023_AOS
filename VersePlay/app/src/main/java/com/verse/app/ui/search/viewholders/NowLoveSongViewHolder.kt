package com.verse.app.ui.search.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.CollectionType
import com.verse.app.databinding.ItemSearchLovelySongBinding
import com.verse.app.extension.dp
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.setReWidth
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.search.NowLoveSongData
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.moveToCollectionFeed

/**
 * Description : 검색어 입력화면 > 지금 사랑받는 노래 ViewHolder
 *
 * Created by juhongmin on 2023/05/10
 */
class NowLoveSongViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    itemBinding: ItemSearchLovelySongBinding = createBinding(
        parent,
        R.layout.item_search_lovely_song,
        viewModel
    )
) : BaseViewHolder<NowLoveSongData>(
    itemBinding
) {
    private val deviceProvider: DeviceProvider by lazy { entryPoint.deviceProvider() }

    init {
        // Divider 20 * 6
        // | 여백 | 이미지 | 여백 | 이미지 | 여백 | 이미지 | 여백 | 이미지 | 여백 | 이미지 | 여백
        val deviceWidth = deviceProvider.getDeviceWidth().minus(120.dp)
        val ratioWidth = (deviceWidth.toFloat() / 5.0).toInt()
        itemBinding.root.setReWidth(ratioWidth)

        itemBinding.root.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            moveToSong(data)
        }
    }

    private fun moveToSong(data: NowLoveSongData) {
        itemView.getFragmentActivity()?.moveToCollectionFeed(CollectionType.FEED, FeedContentsData(songMngCd = data.songMngCd, songId = data.songId, songNm = data.songNm, albImgPath = data.albImgPath))
    }
}