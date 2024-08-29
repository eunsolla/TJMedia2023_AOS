package com.verse.app.model.community

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/15
 */
data class CommunityMainBannerData(
    val commonCode: String? = null,
    val imagePath: String = "",
    val url: String? = null,
    val bannerCode: String? = null
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_COMMUNITY_MAIN_BANNER
    }

    override fun getClassName(): String {
        return "CommunityMainBannerData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityMainBannerData) {
            this == diffUtil
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityMainBannerData) {
            this == diffUtil
        } else {
            false
        }
    }
}