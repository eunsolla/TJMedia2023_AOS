package com.verse.app.utility.exo

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.IllegalSeekPositionException
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.verse.app.R
import com.verse.app.contants.Config
import com.verse.app.contants.ExoPageType
import com.verse.app.contants.GlideCode
import com.verse.app.contants.SingType
import com.verse.app.di.ExoModule
import com.verse.app.extension.onIO
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.param.AdHistoryBody
import com.verse.app.model.param.FeedHistoryBody
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.bindingadapter.GlideBindingAdapter
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider


/**
 * Description : ExoProvider SDK Provider
 *
 * Created by jhlee on 2023-01-01
 */
interface ExoProvider {
    fun onPreLoad(url: String)
    fun onPreLoadList(dataList: MutableList<FeedContentsData>)
    fun onPreLoadDetailList(dataList: MutableList<FeedContentsData>)
    fun onPreLoadURLList(dataList: MutableList<String>)
    fun setPrepare(exoPlayerView: ExoStyledPlayerView)
    fun onPlay(exoPlayerView: ExoStyledPlayerView)
    fun onResume(exoPlayerView: ExoStyledPlayerView)
    fun onResume()
    fun onPause(exoPlayerView: ExoStyledPlayerView)
    fun onPause()
    fun onStop(exoPlayerView: ExoStyledPlayerView)
    fun setPageInfo(page: ExoPageType, playerStateListener: Player.Listener)
    fun setPlayingListener(listener: ExoProviderProviderImpl.Listener)
    fun releaseCache()
    fun getPage(): ExoPageType
    fun clearPage()
    fun isCached(url: String): Boolean
    fun setExoLock(state: Boolean)
    fun isExoLock(): Boolean
    fun getElapsedTime(): Int
    fun getContentDuration(exoPlayerView: ExoStyledPlayerView): Long
    fun onMoToSeek(pos: Int)
    fun isPlaying(): Boolean
}

class ExoProviderProviderImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val simpleCache: SimpleCache,
    private val dataSourceFactory: CacheDataSource.Factory,
    private val exoPlayer: Provider<ExoPlayer>,
    private val loginManager: LoginManager,
    private val apiService: ApiService,
    private val deviceProvider: DeviceProvider
) : ExoProvider, Player.Listener {

    companion object {
        private const val TAG = "ExoProvider"
        private const val FEED_HISTORY_SECONDS = 15
    }

    interface Listener {
        fun onCurrentMs(ms: Long)

        fun onSwitchingPlay(exoPlayerView: ExoStyledPlayerView)
    }

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    private val mExoPlayer: ExoPlayer by lazy {
        exoPlayer.get().apply {
            addListener(this@ExoProviderProviderImpl)
            playWhenReady = isAutoPlay
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    private var oldPlayerView: ExoStyledPlayerView? = null //현재 플레이어뷰
    private var isAutoPlay: Boolean = false
    private var isLock: Boolean = false
    private var page: ExoPageType = ExoPageType.NONE

    //재생 시간 체크
    private var trackProgressDisposable: Disposable? = null
    private var curTime = 0
    private var listener: Listener? = null
    private var mPlayerStateListener: Player.Listener? = null


    private val exceptionHandler = CoroutineExceptionHandler { ctx, throwable ->
        DLogger.d("Caught exception: $throwable")
    }

    private lateinit var exoJob: Job

    /**
     * URL 캐싱 상태 체크
     */
    override fun isCached(url: String): Boolean {
        return simpleCache.isCached(url, 0, ExoModule.CACHE_SIZE_EACH_VIDEO)
    }

    override fun onPreLoad(url: String) {
        onPreLoadURLList(mutableListOf(url))
      /*  url.let {
            //캐싱 처리
            if (!isCached(it)) {
                workManager.enqueue(ExoPreloadWorker.buildWorkRequest(it, simpleCache))
            } else {
                DLogger.d(TAG, "preCache 이미 캐싱 됨=> ${it}")
            }
        }*/
    }

    override fun onPreLoadList(dataList: MutableList<FeedContentsData>) {

        dataList?.let {

            val list = mutableListOf<String>()

            dataList?.forEach {

                if (it.highConPath.isNotEmpty()) {
                    val url = Config.BASE_FILE_URL + it.highConPath

                    val isCached = isCached(url)

                    if (!isCached) {
                        list.add(url)
                    } else {
                        // DLogger.d("이미 캐싱됨 ${it}")
                    }
                }
            }

            if (list.isNotEmpty()) {
                goPreLoad(list)
            }
        }
    }

    override fun onPreLoadDetailList(dataList: MutableList<FeedContentsData>) {
        dataList?.let {

            val list = mutableListOf<String>()

            dataList?.forEach {

                if (it.orgConPath.isNotEmpty()) {
                    val url = Config.BASE_FILE_URL + it.orgConPath

                    val isCached = isCached(url)

                    if (!isCached) {
                        list.add(url)
                    } else {
                        // DLogger.d("이미 캐싱됨 ${it}")
                    }
                }
            }

            if (list.isNotEmpty()) {
                goPreLoad(list)
            }
        }
    }

    override fun onPreLoadURLList(dataList: MutableList<String>) {

        dataList?.let {

            val list = mutableListOf<String>()

            dataList?.forEach {

                if (it.isNotEmpty()) {
                    val url = Config.BASE_FILE_URL + it

                    val isCached = isCached(url)

                    if (!isCached) {
                        list.add(url)
                    } else {
                        //DLogger.d("이미 캐싱됨 ${it}")
                    }
                }
            }
            if (list.isNotEmpty()) {
                goPreLoad(list)
            }
        }
    }


    val mPreLoadProgressListener = CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
        val downloadPercentage: Double = (bytesCached * 100.0 / requestLength)
        if (downloadPercentage.toInt() >= 100) {
            DLogger.d("DownloadPercentage 100 =>  ${downloadPercentage}");
        }
    }

    @SuppressLint("CheckResult")
    private fun goPreLoad(dataList: MutableList<String>) {
        onIO {
            dataList?.forEach { url ->
                launch {
                    val ds = DataSpec.Builder()
                        .setUri(Uri.parse(url))
                        .setLength(ExoModule.CACHE_SIZE_EACH_VIDEO)
                        .build()
                    runCatching {
                        DLogger.d("goPreLoad=> ${url}")
//                        CacheWriter(dataSourceFactory.createDataSource(), ds, null, mPreLoadProgressListener).cache()
                        CacheWriter(dataSourceFactory.createDataSource(), ds, null, null).cache()
                    }.onFailure {
//                        DLogger.d("goPreLoad error=> ${url}")
                    }
                }
            }
        }
    }

//    private fun goPreLoad(dataList: MutableList<String>) {
//        val array: Array<String> = dataList.toTypedArray()
//        val request = ExoPreloadWorker.buildWorkRequestList(array, simpleCache)
//        workManager.enqueue(request)
//    }

    /**
     *  재생 준비
     */
    override fun setPrepare(exoPlayerView: ExoStyledPlayerView) {

        exoPlayerView.getFeedItem()?.let { playerView ->
            if (playerView.playType == ExoStyledPlayerView.ExoPlayType.OBTAIN) {
                playerView.mediaSource?.let { mediaSource ->
                    mExoPlayer.setMediaSource(mediaSource,true)
                }
            } else {
                //다중 비디오 연속 플레이
                if (!playerView.mediaSourceList.isNullOrEmpty()) {

                    playerView.mediaSourceList?.let { mediaSourceList ->
                        try {
                            // 1 <= n  랜덤 재생
                            val range = (0 until mediaSourceList.size)
                            val index = range.random()
                            /*mediaSourceList.forEachIndexed{index, mediaSource ->
                                mediaSource.mediaItem.localConfiguration?.let {
                                    DLogger.d(TAG, "####mediaSourceList =>> ${it.uri}")
                                }?:run {
                                    DLogger.d(TAG, "####mediaSourceList =>> is null")
                                }
                            }*/

                            mExoPlayer.setMediaSources(mediaSourceList, index, 0)

                        } catch (e: IllegalSeekPositionException) {
                            DLogger.e("MediaSources Error ${e.printStackTrace()}")
                            mExoPlayer.setMediaSources(mediaSourceList, true)
                        }
                    }
                }
                mExoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            }

            mExoPlayer.prepare()
        }
    }

    /**
     * 재생 시작
     */
    override fun onPlay(exoPlayerView: ExoStyledPlayerView) {

        if (getPage() == ExoPageType.SING_ING) {
            mExoPlayer.volume = 0f
        } else {
            mExoPlayer.volume = 1.0f
        }

        exoPlayerView.let { curView ->
            getPrevPlayer()?.let { prevView ->
                DLogger.d(TAG, "onPlay ->  ${curView.hashCode()} / ${prevView.hashCode()}")
                if (curView.hashCode() != prevView.hashCode()) {
                    listener?.onSwitchingPlay(prevView)
                    switchingPlay(curView)
                } else {
                    curView.player?.playWhenReady = true
                }
            } ?: run {
                curView.player = mExoPlayer.apply {
                    playWhenReady = true
                }
            }
            setPrevPlayer(curView)
            startElapsedTime()
        }
    }

    override fun clearPage() {
        this.page = ExoPageType.NONE
    }

    override fun onMoToSeek(pos: Int) {
        mExoPlayer.seekTo(pos.toLong())
    }

    /**
     * 플레이어뷰 전환
     */
    private fun switchingPlay(playerView: ExoStyledPlayerView) {
        DLogger.d(TAG, "switchingPlay ${playerView.getFeedItem().feedContentsData?.songNm}/ ${getPrevPlayer()?.getFeedItem()?.feedContentsData?.songNm} ")
//        getPrevPlayer()?.player?.seekTo(0)
        StyledPlayerView.switchTargetView(mExoPlayer, getPrevPlayer(), playerView)
        playerView.player?.play()
    }

    /**
     * 재생 이력
     */
    private fun startElapsedTime() {

        if (trackProgressDisposable != null) {
            closeDisposable()
        }

        DLogger.d(TAG, "startElapsedTime-> ${page}")
        if (page != ExoPageType.MAIN_RECOMMEND && page != ExoPageType.MAIN_FOLLOWING && page != ExoPageType.FEED_DETAIL) {
            DLogger.d(TAG, "startElapsedTime return-> ${page}")
            return
        }

        getPrevPlayer()?.let { exoView ->

            //비 로그인이고 광고가 아니면 이력 적재 안 함
            //비 로그인 시 광고는 적재
            if (!loginManager.isLogin() && exoView.getFeedItem().feedContentsData?.paTpCd != SingType.AD.code) {
                return
            }

            trackProgressDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .map { curTime }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        if (mExoPlayer.isPlaying) {
                            curTime++
                            DLogger.d(TAG, "경과시간-> ${curTime}")
                            if (curTime == FEED_HISTORY_SECONDS) {
                                exoView.getFeedItem().feedContentsData?.let {
                                    if (it.paTpCd != SingType.AD.code) {
                                        //피드
                                        requestViewFeedContentsHistory(it.feedMngCd, curTime)
                                    } else {
                                        //광고
                                        requestViewAdContentsHistory(it.feedMngCd, curTime)
                                    }

                                    closeDisposable()
                                }
                            }
                        }
                    },
                    onError = { error -> DLogger.d(TAG, "error -> ${error}") }
                )
        }
    }

    /**
     * 다시 재생
     */
    override fun onResume(exoPlayerView: ExoStyledPlayerView) {
        DLogger.d(TAG, "onResume !! ${exoPlayerView.getFeedItem().feedContentsData?.songNm}")
        exoPlayerView.let {
            it.player?.let { player ->
                if (!player.playWhenReady) {
                    DLogger.d(TAG, "onResume !!")
                    player.playWhenReady = true
                } else {
                    DLogger.d(TAG, "onResume !! ${player.playWhenReady}")
                }
            } ?: run {
                DLogger.d(TAG, "onResume player is null")
            }
        }
    }

    override fun onResume() {
        getPrevPlayer()?.let {
            it.player?.let { player ->
                if (!player.playWhenReady) {
                    player.playWhenReady = true
                }
            }
        }
    }

    /**
     * 정지
     */
    override fun onPause(exoPlayerView: ExoStyledPlayerView) {
        exoPlayerView.let {
            it.player?.let { player ->
                if (player.playWhenReady) {
                    DLogger.d(TAG, "onPause !!")
                    player.playWhenReady = false
                }
            }
        }
    }

    override fun onPause() {
        getPrevPlayer()?.let {
            it.player?.let { player ->
                if (player.playWhenReady) {
                    DLogger.d(TAG, "onLock !!")
                    player.playWhenReady = false
                }
            }
        }
    }

    /**
     * 해제 및 제거 ( 싱글톤이라  단순 정지만 줌. )
     */
    override fun onStop(exoPlayerView: ExoStyledPlayerView) {
        exoPlayerView.let {
            it.player?.let { player ->
                DLogger.d(TAG, "onStop !!")
                player.seekTo(0)
                player.playWhenReady = false
            }
        }
    }

    override fun releaseCache() {
        simpleCache.release()
    }

    override fun getPage(): ExoPageType {
        return this.page
    }

    override fun setPageInfo(page: ExoPageType, playerStateListener: Player.Listener) {
        if (mPlayerStateListener != null) {
            mPlayerStateListener = null
        }
        this.page = page
        this.mPlayerStateListener = playerStateListener
        DLogger.d(TAG, "ExoProvider 현재 페이지=> ${page}")
    }

    override fun setPlayingListener(listener: Listener) {
        this.listener = listener
    }

    fun getPrevPlayer(): ExoStyledPlayerView? {
        return oldPlayerView
    }

    fun setPrevPlayer(curView: ExoStyledPlayerView) {
        DLogger.d(TAG, "setPrevPlayer ${curView.hashCode()}")

        oldPlayerView?.let {
            if (getPage() == ExoPageType.MAIN_SING_PASS) {
                setSingPassThumb(it)
            }
            it.getFeedItem()?.thumbnailView?.visibility = View.VISIBLE
        }

        oldPlayerView = curView
    }

    fun setSingPassThumb(oldPlayerView: ExoStyledPlayerView) {

        oldPlayerView?.let { exoView ->

            exoView.getFeedItem()?.mediaSourceList?.let { mediaSourceList ->
                try {
                    // 1 <= n  랜덤 재생
                    val range = (0 until mediaSourceList.size)
                    val index = range.random()

                    exoView.getFeedItem().thumbnailView?.let {
                        setThumbnail(it as ImageView, exoView.getFeedItem().genreRankingList[index].highConPath)
                    }

                } catch (e: IllegalSeekPositionException) {
                    DLogger.e("MediaSources Error ${e.printStackTrace()}")
                    mExoPlayer.setMediaSources(mediaSourceList, true)
                }
            }
        }
    }

    /**
     * play 되면 안되는 화면에서 재생 막음
     */
    override fun setExoLock(state: Boolean) {
        isLock = state

        if (state) {
            onPause()
        }
    }

    /**
     * lock 여부
     */
    override fun isExoLock(): Boolean {
        return isLock
    }

    override fun getElapsedTime(): Int {
        return curTime
    }

    /**
     * 씽패스 썸네일
     */
    private fun setThumbnail(view: ImageView, thumbnailPath: String) {
        GlideBindingAdapter.loadImage(view, thumbnailPath, ContextCompat.getDrawable(context, R.drawable.ic_verse_2), GlideCode.GLIDE_BLUR_RADIUS, GlideCode.GLIDE_BLUR_SAMPLING, Glide.with(view))
//        Glide.with(view.context)
//            .load(Config.BASE_FILE_URL + thumbnailPath)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
//            .into(view)
    }

    /**
     * 피드 재생 확인 이력 적재
     */
    private fun requestViewFeedContentsHistory(feedMngCd: String, time: Int) {
        apiService.insertViewFeedContentsHistory(FeedHistoryBody(feedCode = feedMngCd, viewTime = time))
            .subscribeOn(Schedulers.io())
            .doOnSuccess { res -> DLogger.d("피드 확인 이력 res-> ${res.httpStatus} ${res.status}") }
            .subscribe()
    }


    /**
     * 광고 확인 이력 적재
     */
    private fun requestViewAdContentsHistory(adMngCd: String, time: Int) {
        apiService.insertViewAdContents(AdHistoryBody(adMngCd = adMngCd, viewTime = time))
            .subscribeOn(Schedulers.io())
            .doOnSuccess { res -> DLogger.d("광고 확인 이력 res-> ${res.httpStatus} ${res.status}") }
            .subscribe()
    }

    /**
     * 이력 적재 clear
     */
    private fun closeDisposable() {
        curTime = 0
        trackProgressDisposable?.dispose()
        trackProgressDisposable = null
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
            if (player.isPlaying && getPage() == ExoPageType.FEED_DETAIL) {
                listener?.onCurrentMs(player.currentPosition)
                startLoop()
            } else {
                finishLoop()
            }
        }
    }

    private fun startLoop() {
        finishLoop()
        exoJob = Job()
        runLoop()
    }

    private fun finishLoop() {
        if (::exoJob.isInitialized) {
            exoJob.cancel()
        }
    }

    private fun runLoop() = CoroutineScope(Dispatchers.Main + exoJob).launch(exceptionHandler) {
        while (isActive) {
            getPrevPlayer()?.player?.let {
                listener?.onCurrentMs(it.currentPosition)
                delay(33)
                if (!it.isPlaying) {
                    this.cancel()
                }
            }
        }
    }

    override fun getContentDuration(exoPlayerView: ExoStyledPlayerView): Long {
        return exoPlayerView?.player?.contentDuration ?: 0L
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        mPlayerStateListener?.onPlaybackStateChanged(playbackState)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        mPlayerStateListener?.onIsPlayingChanged(isPlaying)

    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        mPlayerStateListener?.onPlayWhenReadyChanged(playWhenReady, reason)
    }

    override fun onPlayerError(error: PlaybackException) {
        mPlayerStateListener?.onPlayerError(error)
    }

    override fun isPlaying(): Boolean {
        if (mExoPlayer == null) {
            return false
        }
        return mExoPlayer.isPlaying
    }

}