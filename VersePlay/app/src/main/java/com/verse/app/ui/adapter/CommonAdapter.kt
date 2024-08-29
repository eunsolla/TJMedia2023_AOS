package com.verse.app.ui.adapter

import com.verse.app.base.adapter.BaseAdapter
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ListPagedItemType
import com.verse.app.databinding.ItemSongMainChartBinding
import com.verse.app.model.base.BaseViewTypeModel
import com.verse.app.model.song.SongMainInfo

/**
 * Description : RecyclerView.Adapter
 *
 * Created by jhlee on 2023-03-31
 */
class CommonAdapter<T : BaseViewTypeModel>(
    viewModel: BaseViewModel,
) : BaseAdapter<T>(
    viewHolderFactory = { viewType, parent ->

        when (ListPagedItemType.values()[viewType]) {
            ListPagedItemType.SONG_CHART_VIEW -> {
                SongChartViewHolder(BaseViewHolder.createBinding(parent, ListPagedItemType.values()[viewType].layoutId, viewModel)) as BaseViewHolder<T>
            }
            else -> BaseViewHolder.create(parent, ListPagedItemType.values()[viewType].layoutId, viewModel)
        }
    },
    viewTypeFactory = { viewData ->
        viewData.itemViewType.ordinal
    }
) {

    /**
     * 노래 콘텐츠 업로드 -> 노래 차트
     */
    data class SongChartViewHolder(val binding: ItemSongMainChartBinding) : BaseViewHolder<SongMainInfo>(binding) {
        override fun bind(data: SongMainInfo) {
            binding.apply {
                if (vpSong.adapter != null) {
                    vpSong.adapter = null
                }
            }
            super.bind(data)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}
