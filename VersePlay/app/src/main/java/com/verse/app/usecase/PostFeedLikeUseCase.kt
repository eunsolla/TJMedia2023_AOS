package com.verse.app.usecase

import com.verse.app.contants.AppData
import com.verse.app.contants.LikeType
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.param.LikeBody
import com.verse.app.repository.http.ApiService
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Description : 피드 좋아요 전용 UseCase
 *
 * Created by juhongmin on 2023/07/02
 */
class PostFeedLikeUseCase @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * @param isLike true: 좋아요 추가, false: 좋아요 삭제
     * @param mngCode 피드 매니지 코드
     */
    operator fun invoke(
        isLike: Boolean,
        mngCode: String,
        currentCnt: Int
    ): Single<BaseResponse> {
        return apiService.updateLike(
            LikeBody(
                likeType = LikeType.FEED.code,
                likeYn = if (isLike) AppData.N_VALUE else AppData.Y_VALUE,
                contentsCode = mngCode
            )
        ).map {
//            if (isLike) {
//                UserFeedLikeManager.add(mngCode, currentCnt)
//            } else {
//                UserFeedLikeManager.remove(mngCode, currentCnt)
//            }
            it
        }
    }
}