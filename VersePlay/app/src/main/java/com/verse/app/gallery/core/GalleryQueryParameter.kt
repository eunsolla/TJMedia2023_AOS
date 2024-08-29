package com.verse.app.gallery.core

import android.provider.MediaStore

/**
 * Description : Gallery Query Parameter
 * @see <a href="https://github.com/sieunju/gallery">참고</a>
 * Created by juhongmin on 2023/05/12
 */
@Suppress("unused")
class GalleryQueryParameter {
    var filterId: String = "" // bucket id
    var isAscOrder: Boolean = false // is Ascending order

    var isVideoType : Boolean = false

    val order: String
        get() = if (isAscOrder) {
            "ASC"
        } else {
            "DESC"
        }

    val selectionArgs: Array<String>?
        get() = if (isAll) null else arrayOf(filterId)

    val isAll: Boolean
        get() = filterId == "ALL" || filterId.isEmpty()

    private val columns: MutableSet<String> = mutableSetOf()

    /**
     * Cursor 에 조회 하고 싶은 Column 값들을 추가 하는 함수
     * @param column 기본적인 컬럼 값 말고 더 조회 하고 싶은 값
     * @see MediaStore.Images.Media._ID
     * @see MediaStore.Images.ImageColumns.ORIENTATION
     * @see MediaStore.Images.Media.BUCKET_ID
     */
    fun addColumns(column: String) {
        columns.add(column)
    }

    fun getColumns(): Array<String> = columns.toTypedArray()

    init {
        columns.add(MediaStore.Images.Media._ID)
        columns.add(MediaStore.Images.Media.BUCKET_ID)
        columns.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
    }
}