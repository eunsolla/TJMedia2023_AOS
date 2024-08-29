package com.verse.app.model.param

/**
 * Description : 채팅 메시지 내역 QueryMap
 *
 * Created by juhongmin on 2023/06/14
 */
class ChatRoomMessageQueryMap : HashMap<String, Any>() {

    var pageNo: Int = 1
        set(value) {
            put("pageNum", value)
            field = value
        }

    var code: String = ""
        set(value) {
            put("chatRoomCd", value)
            field = value
        }

    init {
        pageNo = 1
    }
}
