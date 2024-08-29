package com.verse.app.usecase

import com.verse.app.contants.CommentType
import com.verse.app.model.comment.CommentRepCountData
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.manager.CommentCountManager
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Description : 댓글 개수 조회 하는 UseCase
 *
 * Created by juhongmin on 2023/07/03
 */
class GetCommentCountUseCase @Inject constructor(
    private val apiService: ApiService
) {

    operator fun invoke(
        reqType: CommentType,
        contentsCode: String
    ): Single<CommentRepCountData> {
        val queryMap = hashMapOf<String, String>()
        queryMap["reqType"] = reqType.code
        queryMap["contentsCode"] = contentsCode
        return apiService.fetchComRepCount(queryMap)
            .map { it.result }
            .map {
                setCommentCount(reqType, contentsCode, it)
                it
            }
    }

    private fun setCommentCount(
        reqType: CommentType,
        contentsCode: String,
        data: CommentRepCountData
    ) {
        if (reqType == CommentType.FEED) {
            CommentCountManager.setFeedCount(contentsCode, data.totalCount)
        }
    }
}