package com.verse.app.gallery.core

/**
 * Description : 갤러리 디렉토리 데이터 모델
 *
 * @see <a href="https://github.com/sieunju/gallery">참고</a>
 * Created by juhongmin on 2023/05/12
 */
data class GalleryFilterData(
    val bucketId: String,
    val bucketName: String,
    val photoUri: String,
    val count: Int
) {
    override fun equals(other: Any?): Boolean {
        return if (other is GalleryFilterData) {
            other.bucketId == bucketId
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = bucketId.hashCode()
        result = 31 * result + bucketName.hashCode()
        result = 31 * result + photoUri.hashCode()
        result = 31 * result + count
        return result
    }
}