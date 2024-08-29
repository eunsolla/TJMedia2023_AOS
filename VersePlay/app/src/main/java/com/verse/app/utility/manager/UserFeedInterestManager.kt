package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 피드 > 관심 없음 괸라하는 매니저 클래스
 *
 * Created by jhlee on 2023/07/18
 */
object UserFeedInterestManager {
    // key: 현재 앱에서 관심없음 이벤트 처리한 사용자 아이디, value: true 관심 없음 설정 false 관심없ㄷ음 해제
    private val dataMap: ConcurrentHashMap<String, Boolean> by lazy { ConcurrentHashMap() }

    /**
     * 관심없음 추가
     */
    fun add(code: String) {
        dataMap[code] = true
    }

    /**
     * 관심없음 해제
     */
    fun remove(code: String) {
        dataMap[code] = false
    }

    /**
     * 현재 앱에서 관심없음 이벤트를 했는지와 관심없음 설정 or 해제 했는지 체크 하는 함수
     * @return null 앱내에서 관심없음 true 앱내에서 관심없음 설정, false 앱내에서 관심없음 해제
     */

    fun isNotInterested(code: String): Boolean? {
        return dataMap[code]
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        dataMap.clear()
    }
}
