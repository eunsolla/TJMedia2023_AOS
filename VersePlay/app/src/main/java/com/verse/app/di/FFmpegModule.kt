package com.verse.app.di

import com.verse.app.utility.*
import com.verse.app.utility.ffmpegkit.CommandProvider
import com.verse.app.utility.ffmpegkit.CommandProviderImpl
import com.verse.app.utility.ffmpegkit.FFmpegProvider
import com.verse.app.utility.ffmpegkit.FFmpegProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱  모듈
 * Created by jhlee on 2023-02-01
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class FFmpegModule {
    @Binds
    abstract fun bindFFmpegProvider(FFmpegProvider: FFmpegProviderImpl): FFmpegProvider

    @Singleton
    @Binds
    abstract fun bindCommandProvider(commandProvider: CommandProviderImpl): CommandProvider

}