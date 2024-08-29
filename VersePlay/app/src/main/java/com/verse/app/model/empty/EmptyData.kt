package com.verse.app.model.empty

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/27
 */
data class EmptyData(
    val uid: Long = System.currentTimeMillis()
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_EMPTY
    }

    override fun getClassName(): String {
        return "EmptyData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is EmptyData) {
            uid == diffUtil.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is EmptyData) {
            uid == diffUtil.uid
        } else {
            false
        }
    }
}
