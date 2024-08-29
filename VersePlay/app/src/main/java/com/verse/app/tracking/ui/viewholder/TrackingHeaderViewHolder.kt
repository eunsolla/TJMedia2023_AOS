package com.verse.app.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.R
import com.verse.app.tracking.ui.models.TrackingHeaderUiModel

internal class TrackingHeaderViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(parent, R.layout.vh_tracking_header) {

    private val tvContents: AppCompatTextView by lazy { itemView.findViewById(R.id.tvContents) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingHeaderUiModel) {
            tvContents.text = model.contents
        }
    }
}
