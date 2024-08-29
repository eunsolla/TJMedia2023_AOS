package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 사용자 팔로우 추가 / 해제시 관리하는 매니저 클래스
 *
 * Created by juhongmin on 2023/06/29
 */
object UserFollowManager {

    // key: 현재 앱에서 팔로우 이벤트 처리한 사용자 아이디, value: true 팔로우 추가, false 팔로우 해제
    private val userMap: ConcurrentHashMap<String, Boolean> by lazy { ConcurrentHashMap() }

    /**
     * 팔로우 추가
     */
    fun add(userCode: String) {
        userMap[userCode] = true
    }

    /**
     * 팔로우 해제
     */
    fun remove(userCode: String) {
        userMap[userCode] = false
    }

    /**
     * 현재 앱에서 팔로 이벤트를 했는지와 팔로우 추가 or 해제 했는지 체크 하는 함수
     * @return null 앱내에서 팔로우 이벤트 한 사용자 X, true 앱내에서 팔로우 추가, false 앱내에서 팔로우 해제
     */
    fun isFollow(userCode: String): Boolean? {
        return userMap[userCode]
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        userMap.clear()
    }
}
