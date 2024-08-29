package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 피드 > 사용자 좋아요 추가, 취소 괸라하는 매니저 클래스
 *
 * Created by juhongmin on 2023/07/02
 */
object UserFeedLikeManager {
    // key: 현재 앱에서 팔로우 이벤트 처리한 사용자 아이디, value: true 좋아요 추가, false 좋아요 해제
    private val dataMap: ConcurrentHashMap<String, Pair<Boolean, Int>> by lazy { ConcurrentHashMap() }

    /**
     * 좋아요 추가
     */
    fun add(code: String, currentCnt: Int) {
        dataMap[code] = true to currentCnt.plus(1)
    }

    /**
     * 좋아요 취소
     */
    fun remove(code: String, currentCnt: Int) {
        dataMap[code] = false to Math.max(currentCnt.minus(1), 0)
    }

    fun set(code: String, cnt: Int, isLike: Boolean) {
        dataMap[code] = isLike to cnt
    }

    /**
     * 현재 앱에서 팔로 이벤트를 했는지와 좋아요 추가 or 해제 했는지 체크 하는 함수
     * @return null 앱내에서 팔로우 이벤트 한 사용자 X, true 앱내에서 좋아요 추가, false 앱내에서 좋아요 해제
     */
    fun getLikeInfo(code: String): Pair<Boolean, Int>? {
        return dataMap[code]
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        dataMap.clear()
    }
}
