package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 사용자 북마크 추가 / 해제시 관리하는 매니저 클래스
 *
 * Created by juhongmin on 2023/07/02
 */
object UserFeedBookmarkManager {
    // key: 현재 앱에서 팔로우 이벤트 처리한 사용자 아이디, value: true 팔로우 추가, false 팔로우 해제
    private val dataMap: ConcurrentHashMap<String, Boolean> by lazy { ConcurrentHashMap() }

    /**
     * 북마크 추가
     */
    fun add(code: String) {
        dataMap[code] = true
    }

    /**
     * 북마크 해제
     */
    fun remove(code: String) {
        dataMap[code] = false
    }

    /**
     * 현재 앱에서 팔로 이벤트를 했는지와 팔로우 추가 or 해제 했는지 체크 하는 함수
     * @return null 앱내에서 북마크 이벤트 한 사용자 X, true 앱내에서 북마크 추가, false 앱내에서 팔로우 해제
     */
    fun isBookmark(code: String): Boolean? {
        return dataMap[code]
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        dataMap.clear()
    }
}