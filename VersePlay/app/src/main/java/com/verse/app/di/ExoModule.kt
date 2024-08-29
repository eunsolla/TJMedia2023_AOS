package com.verse.app.di

import android.content.Context
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import javax.inject.Singleton


/**
 * ExoPlayer 모듈
 * Created by jhlee on 2023-02-01
 */
@InstallIn(SingletonComponent::class)
@Module
class ExoModule {

    companion object {
        //[s] cache
        const val VERSE_CACHE_DIR_NAME = "verseVideo" // 패키지 내부 캐시 폴더명
        const val EXO_PLAYER_VIDEO_CACHE_DURATION = 90 * 1024 * 1024  // 패키지 내부 캐시 폴더 사이즈 90MB
        const val CACHE_SIZE_EACH_VIDEO: Long =  50 * 1024 // 캐싱 값 50KB

        //[s] load control
        const val INDIVIDUAL_ALLOCATION_SIZE: Int = 16
        const val MIN_BUFFER_MS: Int = 3000
        const val MAX_BUFFER_MS: Int = 3000
        const val BUFFER_FOR_PLAYBACK_MS: Int = 1500
        const val BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER_MS: Int = 500
    }


    /**
     * @return an instance of [DefaultHttpDataSource]
     */
    @Provides
    fun provideHttpDataSourceFactory(): DefaultHttpDataSource.Factory {
        return DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
    }

    /**
     * @return an instance of [DefaultTrackSelector]
     */
    @Singleton
    @Provides
    fun provideDefaultTrackSelector(@ApplicationContext context: Context): DefaultTrackSelector {
        return DefaultTrackSelector(context)
    }

    /**
     * @return an instance of [DefaultRenderersFactory]
     */
    @Singleton
    @Provides
    fun provideDefaultRenderersFactory(@ApplicationContext context: Context): DefaultRenderersFactory {
        return DefaultRenderersFactory(context)
    }

    /**
     * @return an instance of [DefaultRenderersFactory]
     */
    @Singleton
    @Provides
    fun provideDefaultMediaSourceFactory(cacheFactory: DataSource.Factory): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(cacheFactory)
    }

    /**
     * @return an instance of [DefaultDataSourceFactory]
     */
    @Provides
    fun provideDefaultDataSourceFactory(@ApplicationContext context: Context): DefaultDataSource.Factory {
        return DefaultDataSource.Factory(context)
    }

    @Singleton
    @Provides
    fun provideCacheDataSourceFactory(
        simpleCache: SimpleCache,
        factory: DefaultHttpDataSource.Factory
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(factory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    /**
     * @return an instance of [DataSource.Factory]
     */
    @Singleton
    @Provides
    fun provideDataSourceFactory(
        simpleCache: SimpleCache,
        factory: DefaultHttpDataSource.Factory,
        cacheDataSlinkFactory: DataSink.Factory,
    ): DataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(factory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

//            .setCacheReadDataSourceFactory(FileDataSource.Factory())
//            .setCacheWriteDataSinkFactory(cacheDataSlinkFactory)
    }

    /**
     * 일반 미디어 파일 형식
     * 네트워크 연결 후 재 시작 setLoadErrorHandlingPolicy
     */
    @Singleton
    @Provides
    fun provideProgressiveMediaSourceFactory(factory: CacheDataSource.Factory): ProgressiveMediaSource.Factory {
        return ProgressiveMediaSource.Factory(factory)

        /*
                네트워크 연결 후 플레이 시 사용. 일단 주석..
                return ProgressiveMediaSource.Factory(factory).setLoadErrorHandlingPolicy(object : LoadErrorHandlingPolicy{
                    override fun getFallbackSelectionFor(fallbackOptions: LoadErrorHandlingPolicy.FallbackOptions, loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): LoadErrorHandlingPolicy.FallbackSelection? {
                        TODO("Not yet implemented")
                    }
                    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
                        return if (loadErrorInfo.exception is HttpDataSourceException) {
                            3000 // retry 3초
                        } else {
                            C.TIME_UNSET
                        }
                    }
                    override fun getMinimumLoadableRetryCount(dataType: Int): Int {
                        return Integer.MAX_VALUE
                    }
                })*/
    }


    /**
     * @return an instance of [LoadControl]
     */
    @Singleton
    @Provides
    fun provideLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, INDIVIDUAL_ALLOCATION_SIZE))
            .setBufferDurationsMs(
                MIN_BUFFER_MS,
                MAX_BUFFER_MS,
                BUFFER_FOR_PLAYBACK_MS,
                BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER_MS
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    /**
     * @return an instance of [AudioAttributes]
     */
    @Singleton
    @Provides
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder().apply {
            setUsage(C.USAGE_MEDIA)
            setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        }.build()
    }

    /**
     * @return an instance of [ExoPlayer]
     */
    @Singleton
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        mediaSourceFactory: DefaultMediaSourceFactory,
        trackSelector: DefaultTrackSelector,
        loadControl: LoadControl,
        audioAttributes: AudioAttributes,
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .setAudioAttributes(audioAttributes, true)
            .build()
    }


    /**
     * @return an instance of [LeastRecentlyUsedCacheEvictor]
     */
    @Singleton
    @Provides
    fun provideLeastRecentlyUsedCacheEvictor(): LeastRecentlyUsedCacheEvictor {
        return LeastRecentlyUsedCacheEvictor(EXO_PLAYER_VIDEO_CACHE_DURATION.toLong())
    }

    /**
     * @return an instance of [StandaloneDatabaseProvider]
     */
    @Singleton
    @Provides
    fun provideDatabaseProvider(@ApplicationContext context: Context): StandaloneDatabaseProvider {
        return StandaloneDatabaseProvider(context)
    }

    /**
     * @return an instance of [SimpleCache]
     */
    @Singleton
    @Provides
    fun provideSimpleCache(
        @ApplicationContext context: Context,
        evictor: LeastRecentlyUsedCacheEvictor,
        databaseProvider: StandaloneDatabaseProvider,
    ): SimpleCache {
        return SimpleCache(File(context.cacheDir, VERSE_CACHE_DIR_NAME), evictor, databaseProvider)
    }

    /**
     * @return an instance of [CacheDataSource]
     */
    @Provides
    fun provideCacheDataSource(
        simpleCache: SimpleCache,
        factory: DefaultHttpDataSource.Factory,
        cacheDataSink: CacheDataSink,
    ): CacheDataSource {
        return CacheDataSource(
            simpleCache, factory.createDataSource(), FileDataSource(), cacheDataSink,
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }

    /**
     * @return an instance of [CacheDataSink]
     */
    @Singleton
    @Provides
    fun provideCacheDataSink(simpleCache: SimpleCache): CacheDataSink {
        return CacheDataSink(simpleCache, EXO_PLAYER_VIDEO_CACHE_DURATION.toLong())
    }

    @Singleton
    @Provides
    fun provideCacheDataSinkFactory(simpleCache: SimpleCache): DataSink.Factory {
        return CacheDataSink.Factory()
            .setCache(simpleCache)
            .setFragmentSize(EXO_PLAYER_VIDEO_CACHE_DURATION.toLong())
    }


    /**
     * @return an instance of [CookieManager]
     */
    @Singleton
    @Provides
    fun provideCookieManager(): CookieManager {
        return CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
    }

    /**
     * @return an instance of [CookieHandler]
     */
    @Singleton
    @Provides
    fun provideCookieHandler(cookieManager: CookieManager): CookieHandler {
        return CookieHandler.getDefault().apply {
            if (this != cookieManager) {
                CookieHandler.setDefault(cookieManager)
            }
        }
    }
}