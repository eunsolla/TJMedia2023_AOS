package com.verse.app.usecase

import com.verse.app.model.base.BaseResponse
import com.verse.app.model.param.FollowBody
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.manager.UserFollowManager
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Description : 유저 팔로우 추가, 삭제 처리 하는 UseCase
 *
 * Created by juhongmin on 2023/07/02
 */
class PostFollowUseCase @Inject constructor(
    private val apiService: ApiService
) {
    operator fun invoke(body: FollowBody): Single<BaseResponse> {
        return apiService.updateFollow(body)
            .map {
                if (body.followYn == "Y") {
                    UserFollowManager.add(body.userCode)
                } else {
                    UserFollowManager.remove(body.userCode)
                }
                it
            }
    }
}