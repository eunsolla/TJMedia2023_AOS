package com.verse.app.model.lounge

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/20
 */
data class LoungeModifyImageData(
    val idx: Int,
    val imagePath: String
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_LOUNGE_MODIFY_IMAGE
    }

    override fun getClassName(): String {
        return "LoungeModifyImageData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeModifyImageData) {
            imagePath == diffUtil.imagePath
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeModifyImageData) {
            this == diffUtil
        } else {
            false
        }
    }
}