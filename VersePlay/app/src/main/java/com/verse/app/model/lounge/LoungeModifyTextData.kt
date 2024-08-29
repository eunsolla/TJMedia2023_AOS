package com.verse.app.model.lounge

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/20
 */
data class LoungeModifyTextData(
    val uid: Long = System.currentTimeMillis(),
    val contents: CharSequence = ""
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_LOUNGE_MODIFY_EDIT_TEXT
    }

    override fun getClassName(): String {
        return "LoungeModifyTextData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeModifyTextData) {
            uid == diffUtil.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeModifyTextData) {
            uid == diffUtil.uid
        } else {
            false
        }
    }


}