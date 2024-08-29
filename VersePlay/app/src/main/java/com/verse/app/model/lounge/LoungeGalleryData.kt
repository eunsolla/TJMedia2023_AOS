package com.verse.app.model.lounge

import android.net.Uri
import com.verse.app.contants.Config
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description : 갤러리 이미지 데이터 모델
 *
 * Created by juhongmin on 2023/05/18
 */
data class LoungeGalleryData(
    val uid: Long,
    val localUri: String? = null,
    val imagePath: String? = null
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_LOUNGE_GALLERY
    }

    override fun getClassName(): String {
        return "LoungeGalleryModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeGalleryData) {
            uid == diffUtil.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeGalleryData) {
            this == diffUtil
        } else {
            false
        }
    }

    var imageUri: String? = null
        get() {
            if (field == null) {
                if (!localUri.isNullOrEmpty()) {
                    field = localUri
                } else if (!imagePath.isNullOrEmpty()) {
                    val uri = Uri.parse(Config.BASE_FILE_URL.plus(imagePath))
                    field = Config.BASE_FILE_URL.plus(uri.path)
                }
            }
            return field
        }
}