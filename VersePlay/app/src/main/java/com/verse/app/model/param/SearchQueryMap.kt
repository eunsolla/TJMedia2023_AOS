package com.verse.app.model.param

import com.verse.app.model.enums.SearchType
import com.verse.app.model.enums.SortEnum

/**
 * Description : 검색 API 요청할 QueryMap
 *
 * Created by juhongmin on 2023/05/11
 */
class SearchQueryMap : HashMap<String, Any>() {

    var pageNo: Int = 1
        set(value) {
            put("pageNum", value)
            field = value
        }

    var type: SearchType = SearchType.POPULAR
        set(value) {
            put("searchType", value.query)
            field = value
        }

    var sort: SortEnum = SortEnum.DESC
        set(value) {
            put("sortType", value.query)
            field = value
        }

    var keyword: String? = null
        set(value) {
            remove("searchKeyword")
            if (!value.isNullOrEmpty()) {
                put("searchKeyword", value)
            }
            field = value
        }

    init {
        pageNo = 1
        sort = SortEnum.DESC
        type = SearchType.POPULAR
    }
}
