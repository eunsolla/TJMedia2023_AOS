package com.verse.app.utility.manager

import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 피드 댓글 개수 매니저 클래스
 *
 * Created by juhongmin on 2023/07/03
 */
object CommentCountManager {
    // key: 현재 앱에서 팔로우 이벤트 처리한 피드 코드, value: 댓글 개수

    private val feedMap: ConcurrentHashMap<String, Int> by lazy { ConcurrentHashMap() }

    /**
     * 피드 댓글 개수 처리하는 함수
     */
    fun setFeedCount(code: String, currentCnt: Int) {
        feedMap.put(code, currentCnt)
    }

    fun isFeedCounting(code: String): Boolean {
        return feedMap.containsKey(code)
    }

    fun getFeedCount(code: String): Int? {
        return feedMap.get(code)
    }
}
