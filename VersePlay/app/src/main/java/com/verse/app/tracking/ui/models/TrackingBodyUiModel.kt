package com.verse.app.tracking.ui.models

import com.verse.app.R

internal data class TrackingBodyUiModel(
    val body: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_body) {

    override fun getClassName() = "TrackingBodyUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingBodyUiModel) {
            body == diffItem.body
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingBodyUiModel) {
            body == diffItem.body
        } else {
            false
        }
    }
}