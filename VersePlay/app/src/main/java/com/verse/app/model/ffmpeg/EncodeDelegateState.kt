package com.verse.app.model.ffmpeg

import com.arthenica.mobileffmpeg.Statistics

/**
 * Description : Video or Audio Encoding
 *
 * Created by juhongmin on 2023/07/06
 */
data class EncodeDelegateState(
    val progress: Float, // 0.0% ~ 100.0%
    val frameNumber: Int,
    val totalFrame: Long
) {
    constructor(entity: Statistics, totalFrame: Long) : this(
        progress = (entity.videoFrameNumber.toFloat() / totalFrame.toFloat()) * 100.0F,
        frameNumber = entity.videoFrameNumber,
        totalFrame = totalFrame
    )
}
