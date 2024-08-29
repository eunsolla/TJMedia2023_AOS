package com.verse.app.utility.provider

import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.feed.FeedContentsResponse
import com.verse.app.utility.manager.CommentCountManager
import com.verse.app.utility.manager.UserFeedLikeManager
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/07/03
 */
interface FeedRefreshProvider {
    fun setUpdateFeed(list: List<FeedContentsData>)
    fun setUpdateFeed(res: FeedContentsResponse)
}

internal class FeedRefreshProviderImpl @Inject constructor(

) : FeedRefreshProvider {

    override fun setUpdateFeed(res: FeedContentsResponse) {
        try {
            setUpdateFeed(res.result.dataList)
        } catch (ex: Exception) {
        }
    }

    override fun setUpdateFeed(list: List<FeedContentsData>) {
        try {
            Executors.newCachedThreadPool().submit {
                list.forEach {
                    setFeedLike(it)
                    setFeedComment(it)
                }
            }
        } catch (ex: Exception) {
        }
    }

    private fun setFeedLike(data: FeedContentsData) {
        val likeInfo = UserFeedLikeManager.getLikeInfo(data.feedMngCd)
        if (likeInfo != null) {
            UserFeedLikeManager.set(data.feedMngCd, data.likeCnt, data.isLike)
        }
    }

    private fun setFeedComment(data: FeedContentsData) {
        if (CommentCountManager.isFeedCounting(data.feedMngCd)) {
            CommentCountManager.setFeedCount(data.feedMngCd, data.replyCount)
        }
    }
}