package com.verse.app.usecase

import com.verse.app.contants.AppData
import com.verse.app.contants.BookMarkType
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.param.BookMarkBody
import com.verse.app.repository.http.ApiService
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Description : 피드 북마크용 UseCase
 *
 * Created by juhongmin on 2023/07/02
 */
class PostFeedBookmarkUseCase @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * @param isBookmark true: 북마크 추가, false: 북마크 삭제
     * @param mngCode 피드 매니지 코드
     */
    operator fun invoke(
        isBookmark: Boolean,
        mngCode: String
    ): Single<BaseResponse> {
        return apiService.updateBookMark(
            BookMarkBody(
                bookMarkType = BookMarkType.FEED.code,
                bookMarkYn = if (isBookmark) AppData.N_VALUE else AppData.Y_VALUE,
                contentsCode = mngCode
            )
        ).map {
//            if (isBookmark) {
//                UserFeedBookmarkManager.add(mngCode)
//            } else {
//                UserFeedBookmarkManager.remove(mngCode)
//            }
            it
        }
    }
}