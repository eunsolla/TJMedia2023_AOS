package com.verse.app.model.param

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/14
 */
class ChatRoomQueryMap : HashMap<String, Any>() {

    var pageNo: Int = 1
        set(value) {
            put("pageNum", value)
            field = value
        }

    init {
        pageNo = 1
    }
}