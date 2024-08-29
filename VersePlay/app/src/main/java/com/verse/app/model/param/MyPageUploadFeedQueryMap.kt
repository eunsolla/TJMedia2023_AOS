package com.verse.app.model.param

import com.verse.app.contants.FeedSubDataType
import com.verse.app.model.enums.SortEnum

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/27
 */
class MyPageUploadFeedQueryMap : HashMap<String, Any>() {
    var pageNo: Int = 1
        set(value) {
            put("pageNum", value)
            field = value
        }

    var sort: SortEnum = SortEnum.DESC
        set(value) {
            put("sortType", value.query)
            field = value
        }

    var type: FeedSubDataType = FeedSubDataType.C
        set(value) {
            put("dataType", value.code)
            field = value
        }

    var memberCode: String? = null
        set(value) {
            if (value == null) {
                remove("memCd")
            } else {
                put("memCd", value)
            }
            field = value
        }

    init {
        pageNo = 1
        sort = SortEnum.DESC
    }
}