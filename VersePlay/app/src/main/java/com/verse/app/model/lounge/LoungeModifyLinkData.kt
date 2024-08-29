package com.verse.app.model.lounge

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.weblink.WebLinkData

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/20
 */
data class LoungeModifyLinkData(
    val item : WebLinkData
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_LOUNGE_MODIFY_LINK_URL
    }

    override fun getClassName(): String {
        return "LoungeModifyLinkData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeModifyLinkData) {
            item.url == diffUtil.item.url
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is LoungeModifyLinkData) {
            item == diffUtil.item
        } else {
            false
        }
    }
}