package com.verse.app.model.videoupload

import android.net.Uri

data class VideouploadItem(
    var uri: Uri,
    var isChecked: Boolean,
    var pickNumber: Int,
    var durationTime: String
)