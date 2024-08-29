package com.verse.app.tracking.ui.models

import com.verse.app.R

internal data class TrackingTitleUiModel(
    val title: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_title) {

    override fun getClassName() = "TrackingTitleUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingTitleUiModel) {
            title == diffItem.title
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingTitleUiModel) {
            title == diffItem.title
        } else {
            false
        }
    }
}
