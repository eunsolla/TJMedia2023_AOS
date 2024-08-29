package com.verse.app.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.verse.app.R
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.models.TrackingQueryUiModel

internal class TrackingQueryViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_tracking_query
) {

    private val tvQuery: AppCompatTextView by lazy { itemView.findViewById(R.id.tvQuery) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingQueryUiModel) {
            tvQuery.text = model.query
        }
    }
}
