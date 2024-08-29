package com.verse.app.gallery.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Description : 갤러리 전용 아이템
 *
 * Created by juhongmin on 2023/05/13
 */
sealed class GalleryItem(
    open val imagePath: String,
    open var isSelected: Boolean = false,
    open var selectedNum: String = "1",
    open var isSelectCheck: Boolean = false,
) {
    data class Image(
        override val imagePath: String,
        override var isSelected: Boolean = false,
        override var selectedNum: String = "1",
        override var isSelectCheck: Boolean = false
    ) : GalleryItem(
        imagePath,
        isSelected,
        selectedNum,
        isSelectCheck
    )

    data class Video(
        override val imagePath: String,
        override var isSelected: Boolean = false,
        override var selectedNum: String = "1",
        override var isSelectCheck: Boolean = false,
        val duration: Long
    ) : GalleryItem(
        imagePath,
        isSelected,
        selectedNum,
        isSelectCheck
    ) {

        var timeText: String? = null
            get() {
                if (field == null) {
                    val sdf = SimpleDateFormat("mm:ss", Locale.KOREA)
                    field = sdf.format(Date(duration))
                }
                return field
            }
    }
}