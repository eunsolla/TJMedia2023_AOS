package com.verse.app.model.param

import com.verse.app.model.enums.SortEnum

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
class EventQueryMap : HashMap<String, Any>() {

    var pageNo: Int = 1
        set(value) {
            put("pageNum", value)
            field = value
        }

    var sort: SortEnum = SortEnum.ASC
        set(value) {
            put("sortType", value.query)
            field = value
        }

    var isExpired: Boolean = false
        set(value) {
            put("evtStatus", if (value) "C" else "P")
            field = value
        }

    init {
        pageNo = 1
        sort = SortEnum.DESC
        isExpired = false
    }
}