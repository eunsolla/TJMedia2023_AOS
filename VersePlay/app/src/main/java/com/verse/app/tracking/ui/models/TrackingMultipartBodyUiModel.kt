package com.verse.app.tracking.ui.models

import android.graphics.Bitmap
import com.verse.app.R
import okhttp3.MediaType

internal data class TrackingMultipartBodyUiModel(
    val mediaType: MediaType? = null,
    val binary: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_multipart_body) {

    var bitmap: Bitmap? = null

    override fun getClassName() = "TrackingMultipartBodyUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return false
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return false
    }
}
