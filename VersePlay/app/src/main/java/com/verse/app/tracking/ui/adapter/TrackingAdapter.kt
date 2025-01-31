package com.verse.app.tracking.ui.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.R
import com.verse.app.tracking.ui.diffutil.TrackingDetailDiffUtil
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.viewholder.*

/**
 * Description : HttpTracking 공통 어댑터
 *
 * Created by juhongmin on 2022/09/04
 */
internal class TrackingAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<BaseTrackingViewHolder>() {

    private val dataList = mutableListOf<BaseTrackingUiModel>()

    fun submitList(newList: List<BaseTrackingUiModel>?) {
        if (newList == null) return
        val diffResult = DiffUtil.calculateDiff(
            TrackingDetailDiffUtil(
                dataList,
                newList
            )
        )
        dataList.clear()
        dataList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseTrackingViewHolder {
        return when (viewType) {
            R.layout.vh_tracking_header -> TrackingHeaderViewHolder(parent)
            R.layout.vh_tracking_path -> TrackingPathViewHolder(parent)
            R.layout.vh_tracking_query -> TrackingQueryViewHolder(parent)
            R.layout.vh_tracking_body -> TrackingBodyViewHolder(parent)
            R.layout.vh_tracking_multipart_body -> TrackingMultipartBodyViewHolder(parent)
            R.layout.vh_tracking_title -> TrackingTitleViewHolder(parent)
            R.layout.vh_child_tracking -> TrackingListViewHolder(parent, fragment)
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun onBindViewHolder(holder: BaseTrackingViewHolder, pos: Int) {
        runCatching {
            holder.onBindView(dataList[pos])
        }
    }

    override fun getItemViewType(pos: Int): Int {
        return if (dataList.size > pos) {
            dataList[pos].layoutId
        } else {
            return super.getItemViewType(pos)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}