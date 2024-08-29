package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 피드 > 차단 괸라하는 매니저 클래스
 *
 * Created by jhlee on 2023/07/18
 */
object UserFeedBlockManager {
    // key: 현재 앱에서 차단 이벤트 처리한 피드 mngCd, value: true 차단 , false 차단 해제
    private val dataMap: ConcurrentHashMap<String, Boolean> by lazy { ConcurrentHashMap() }

    /**
     * 차단 추가
     */
    fun add(code: String) {
        dataMap[code] = true
    }

    /**
     * 차단 해제
     */
    fun remove(code: String) {
        dataMap[code] = false
    }

    /**
     * 현재 앱에서 차단 이벤트를 했는지와 차단 설정 or 해제 했는지 체크 하는 함수
     * @return null 앱내에서  차단 이벤트 한 피드, true 앱내에서 차단, false 앱내에서 차단 해제
     */
    fun isBlock(code: String): Boolean? {
        return dataMap[code]
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        dataMap.clear()
    }
}
