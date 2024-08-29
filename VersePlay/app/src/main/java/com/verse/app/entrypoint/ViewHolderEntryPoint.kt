package com.verse.app.entrypoint

import com.verse.app.repository.http.ApiService
import com.verse.app.usecase.PostFeedBookmarkUseCase
import com.verse.app.usecase.PostFeedLikeUseCase
import com.verse.app.usecase.PostFollowUseCase
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Description : ViewHolder 에서 Inject 해서 사용할 Class 정의
 *
 * Created by juhongmin on 2023/05/10
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ViewHolderEntryPoint {
    fun deviceProvider(): DeviceProvider

    fun apiService(): ApiService

    fun loginManager(): LoginManager
    fun postFollowUseCase(): PostFollowUseCase
    fun postFeedLikeUseCase(): PostFeedLikeUseCase
    fun postFeedBookmarkUseCase(): PostFeedBookmarkUseCase

}
