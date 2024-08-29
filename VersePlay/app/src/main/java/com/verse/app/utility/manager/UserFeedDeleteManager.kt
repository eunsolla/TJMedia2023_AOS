package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 피드 > 삭제 괸라하는 매니저 클래스
 *
 * Created by jhlee on 2023/07/18
 */
object UserFeedDeleteManager {
    // key: 현재 앱에서 삭제 이벤트 처리한 피드 mngCd, value: true 삭제
    private val dataMap: ConcurrentHashMap<String, Boolean> by lazy { ConcurrentHashMap() }

    /**
     * 삭제
     */
    fun add(code: String) {
        dataMap[code] = true
    }

    /**
     * 현재 앱에서 삭제 이벤트를 했는지 여부
     * @return true = 삭제
     */
    fun isDeleted(code: String): Boolean? {
        return dataMap[code]
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        dataMap.clear()
    }
}
