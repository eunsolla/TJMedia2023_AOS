package com.verse.app.model.search

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

data class RecentKeywordData(
    val idx: Int,
    val recentKeyword: String,
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "RecentKeywordData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is RecentKeywordData) {
            this == diffUtil
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is RecentKeywordData) {
            this == diffUtil
        } else {
            false
        }
    }
}