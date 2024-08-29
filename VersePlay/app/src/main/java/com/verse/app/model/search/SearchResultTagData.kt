package com.verse.app.model.search

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SearchResultTagData(
    @SerialName("tagName")
    var tagName: String = "",
    @SerialName("relateFeedCount")
    var relateFeedCount: String = ""
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SearchResultTagData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultTagData) {
            this == diffUtil
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultTagData) {
            this == diffUtil
        } else {
            false
        }
    }
}