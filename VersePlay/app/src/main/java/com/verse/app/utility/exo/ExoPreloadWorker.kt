package com.verse.app.utility.exo

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.verse.app.contants.Config
import com.verse.app.di.ExoModule
import com.verse.app.utility.DLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ExoPlayer
 * Created by jhlee on 2023-02-01
 */
@HiltWorker
class ExoPreloadWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
) : Worker(context, workerParameters) {

    private var mCachingJob: Job? = null
    private lateinit var mHttpDataSourceFactory: HttpDataSource.Factory
    private lateinit var mDefaultDataSourceFactory: DefaultDataSource.Factory
    private lateinit var mCacheDataSource: CacheDataSource
    private var videoSize = 0
    private var curSize = 0

    interface PreLoadCallBack {
        fun complete()
    }

    enum class WorkType {
        OBTAIN, //한건 
        LIST //다건
    }

    companion object {

        private const val TAG = "ExoPreloadWorker"

        private const val FEED_URL = "feed_url"
        private lateinit var mCache: SimpleCache
        private lateinit var mWorkType: WorkType


        /**
         *@param urlParam : String
         */
        fun buildWorkRequest(
            urlParam: String,
            cache: SimpleCache,
        ): OneTimeWorkRequest {
            mCache = cache
            mWorkType = WorkType.OBTAIN
            val data = Data.Builder().putString(FEED_URL, urlParam).build()
            return OneTimeWorkRequestBuilder<ExoPreloadWorker>().apply { setInputData(data) }
                .build()
        }

        /**
         *@param urlListParam : List
         */
        fun buildWorkRequestList(
            urlListParam: Array<String>,
            cache: SimpleCache,
        ): OneTimeWorkRequest {
            mCache = cache
            mWorkType = WorkType.LIST
            val data = Data.Builder().putStringArray(FEED_URL, urlListParam).build()
            return OneTimeWorkRequestBuilder<ExoPreloadWorker>().apply { setInputData(data) }
                .addTag("PRELOAD")
                .build()
        }
    }

    override fun doWork(): Result {
        return try {

            when (mWorkType) {

                WorkType.OBTAIN -> {
                    var url = inputData.getString(FEED_URL)
                    url?.let {
                        videoSize = 1
                        preCacheVideo(url)
                    } ?: run {
                        Result.failure()
                    }
                }

                WorkType.LIST -> {

                    var urlList = inputData.getStringArray(FEED_URL)

                    urlList?.let {
                        videoSize = urlList.size
                        preCacheVideo(urlList)
                    } ?: run {
                        Result.failure()
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    /**
     *  Start Job (multi thread)
     */
    private fun preCacheVideo(videoUrl: String?) {

        mHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        mDefaultDataSourceFactory = DefaultDataSource.Factory(context, mHttpDataSourceFactory)

        mCacheDataSource = CacheDataSource.Factory()
            .setCache(mCache)
            .setUpstreamDataSourceFactory(mHttpDataSourceFactory)
            .createDataSource()

        val builder = DataSpec.Builder()
            .setUri(Uri.parse(videoUrl))
            .setLength(ExoModule.CACHE_SIZE_EACH_VIDEO)
            .setKey(videoUrl)

        val progressListener = CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
            val downloadPercentage: Double = (bytesCached * 100.0 / requestLength)
            DLogger.d(TAG, "Caching DownloadPercentage=> ${videoUrl} / ${downloadPercentage}");
            if (downloadPercentage.toInt() >= 100) {
                checkComplete()
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            cacheVideo(builder.build(), progressListener)
        }
    }

    /**
     *  Start Job (Single thread)
     */
    private fun preCacheVideo(dataList: Array<String>) {

        mHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        mDefaultDataSourceFactory = DefaultDataSource.Factory(context, mHttpDataSourceFactory)

        mCacheDataSource = CacheDataSource.Factory()
            .setCache(mCache)
            .setUpstreamDataSourceFactory(mHttpDataSourceFactory)
            .createDataSource()

        mCachingJob = CoroutineScope(Dispatchers.IO).launch {

            val progressListener = CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
                val downloadPercentage: Double = (bytesCached * 100.0 / requestLength)
                DLogger.d(TAG, "DownloadPercentage=>  ${downloadPercentage}");
                if (downloadPercentage.toInt() >= 100) {
                    checkComplete()
                }
            }

            dataList.forEach { videoUrl ->
                launch {
                    val builder = DataSpec.Builder()
                        .setUri(Uri.parse(videoUrl))
                        .setLength(ExoModule.CACHE_SIZE_EACH_VIDEO)
                        .setKey(videoUrl)
                    cacheVideo(builder.build(), progressListener)
                }.join()
            }
        }
    }

    /**
     * Run Cache
     */
    private suspend fun cacheVideo(mDataSpec: DataSpec, mProgressListener: CacheWriter.ProgressListener) {
        runCatching {
            CacheWriter(mCacheDataSource, mDataSpec, null, mProgressListener).cache()
        }.onFailure {
            checkComplete()
            it.printStackTrace()
            DLogger.d(TAG, "Caching onFailure=> ${mDataSpec.uri}");
        }
    }

    private fun checkComplete() {
        curSize++
        DLogger.d(TAG, "checkSize :  ${videoSize} /  ${curSize}");
        if (videoSize == curSize) {
//            preLoadCallBack?.complete()
        }
    }
}