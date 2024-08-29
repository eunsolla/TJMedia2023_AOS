package com.verse.app.model.search

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description : 검새거 입력 > 인기 검색어 5개 단위씩 있는 UiModel
 *
 * Created by juhongmin on 2023/05/09
 */
data class PopularKeywordModel(
    val uid: Int,
    val list: List<PopKeywordData>
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_SEARCH_KEYWORD_POPULAR
    }

    override fun getClassName(): String {
        return "PopularKeywordModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is PopularKeywordModel) {
            uid == diffUtil.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is PopularKeywordModel) {
            uid == diffUtil.uid
        } else {
            false
        }
    }

    val oneTitle: String
        get() = list.getOrNull(0)?.popKeyword ?: ""
    val oneRank: String
        get() = list.getOrNull(0)?.popRank ?: ""
    val twoTitle: String
        get() = list.getOrNull(1)?.popKeyword ?: ""
    val twoRank: String
        get() = list.getOrNull(1)?.popRank ?: ""
    val threeTitle: String
        get() = list.getOrNull(2)?.popKeyword ?: ""
    val threeRank: String
        get() = list.getOrNull(2)?.popRank ?: ""
    val fourTitle: String
        get() = list.getOrNull(3)?.popKeyword ?: ""
    val fourRank: String
        get() = list.getOrNull(3)?.popRank ?: ""
    val fiveTitle: String
        get() = list.getOrNull(4)?.popKeyword ?: ""
    val fiveRank: String
        get() = list.getOrNull(4)?.popRank ?: ""

}