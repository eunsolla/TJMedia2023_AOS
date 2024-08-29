package com.verse.app.utility.manager

import com.verse.app.extension.toIntOrDef
import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 피드 상세 조회수 자동 갱신 처리하는 Manager Class
 *
 * Created by juhongmin on 2023/06/07
 */
object FeedContentsHitManager {
    private val feedHitCountMap: ConcurrentHashMap<String, Int> by lazy { ConcurrentHashMap() }

    fun addHitCount(code: String, hit: String) {
        // val hitCount = hit.toIntOrDef(0).plus(1)
        val hitCount = hit.toIntOrDef(0) // 히트 카운팅 주석 처리
        feedHitCountMap[code] = hitCount
    }

    fun isPrevHitCount(code: String): Boolean {
        return feedHitCountMap.containsKey(code)
    }

    fun getHitCount(code: String): Int {
        return feedHitCountMap[code] ?: -1
    }
}