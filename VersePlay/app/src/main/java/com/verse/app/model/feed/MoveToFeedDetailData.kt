package com.verse.app.model.feed

import androidx.paging.PagingData

/**
 * Description : 피드 상세 이동시에 필요한 데이터 모델
 *
 * Created by juhongmin on 2023/05/29
 */
data class MoveToFeedDetailData(
    val startPos: Int,
    val pagingData: PagingData<FeedContentsData>
)