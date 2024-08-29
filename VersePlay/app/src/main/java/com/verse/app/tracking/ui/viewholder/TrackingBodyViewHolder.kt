package com.verse.app.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.verse.app.R
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.models.TrackingBodyUiModel


internal class TrackingBodyViewHolder(parent: ViewGroup) :
    BaseTrackingViewHolder(
        parent,
        R.layout.vh_tracking_body
    ) {

    private val tvBody: AppCompatTextView by lazy { itemView.findViewById(R.id.tvBody) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingBodyUiModel) {
            tvBody.text = model.body
        }
    }
}
