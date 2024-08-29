package com.verse.app.utility.exo

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.verse.app.R
import com.verse.app.contants.ExoPageType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragment
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.onMain
import com.verse.app.model.feed.CurrentFeedData
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.community.CommunityFragment
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.main.fragment.MainFragment
import com.verse.app.ui.main.fragment.MainTabFragment
import com.verse.app.ui.mypage.my.MyPageFragment
import com.verse.app.ui.singpass.fragment.SingPassFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.ffmpegkit.Common
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.collections.set

/**
 * Custom StyledPlayerView
 * Created by jhlee on 2023-02-01
 */
class ExoManager @Inject constructor(
    @ApplicationContext
    val context: Context,
    val exoProvider: ExoProvider,
    val loginManager: LoginManager,
) : LifecycleOwner, LifecycleEventObserver, Player.Listener {

    companion object {
        private const val TAG = "ExoManager"
    }

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    private var mPlayersMap: MutableMap<Int, ExoStyledPlayerView> = mutableMapOf()
    private var mCurPlayerView: ExoStyledPlayerView? = null
    private var page: ExoPageType = ExoPageType.NONE
    private var exoStateListener: ExoStateListener? = null
    var isEditing: Boolean = false
    private val disposable: CompositeDisposable by lazy { CompositeDisposable() }
    interface ExoStateListener {
        fun onStateReady()
        fun onStateBuffering()
        fun onStateError()
    }

    /**
     * Set Player Map
     */
    fun setPlayerView(
        playerView: ExoStyledPlayerView,
        curPage: ExoPageType,
        targetPos : Int? = -1
    ) {

        this.page = curPage

        playerView.let { playerView ->

            if (mPlayersMap.containsKey(playerView.getFeedItem().position)) {
                mPlayersMap.remove(playerView.getFeedItem().position)
            }
            //Exo Put
            mPlayersMap[playerView.getFeedItem().position] = playerView

            if (playerView.getFeedItem().position == 0 && this.page != ExoPageType.FEED_DETAIL) {
                setCurrentPlayer(playerView)
            }

            exoProvider.setExoLock(false)

            if(this.page == ExoPageType.FEED_DETAIL && mPlayersMap.size == 1){
                targetPos?.let {
                    onStartPlayer(targetPos)
                }
            }
        }
    }

    fun setExoStateListener(listener: ExoStateListener) {
        exoStateListener = listener
    }

    /**
     * Set Player
     */
    private fun setCurrentPlayer(playerView: ExoStyledPlayerView?) {

        if (mCurPlayerView != null) {
            if (mCurPlayerView.hashCode() == playerView.hashCode()) return
        }

        mCurPlayerView = playerView

        //메인 추천시 bus
        if (page == ExoPageType.MAIN_RECOMMEND && loginManager.isLogin()) {
            playerView?.getFeedItem()?.feedContentsData?.let {
                DLogger.d("BUS 현재 피드 보냄=> 노래: ${it.songNm}  ,MEMCD: ${it.ownerMemCd} ")
                RxBus.publish(
                    RxBusEvent.VideoUserInfoEvent(
                        CurrentFeedData(
                            feedMngCd = it.feedMngCd,
                            ownerMemCd = it.ownerMemCd,
                            paTpCd = it.paTpCd,
                            ownerMemNk = it.ownerMemNk,
                            ownerFrImgPath = it.ownerFrImgPath,
                            ownerBgImgPath = it.ownerBgImgPath,
                        )
                    )
                )
            }
        }
    }


    /**
     * Start Play
     */
    fun onStartPlayer(pos: Int) {
        mPlayersMap[pos]?.let {
            DLogger.d(TAG, "### onStartPlayer  => ${pos} ${it.getFeedItem().feedContentsData?.songNm}/ ${exoProvider.getPage()} /  ${getPage()} ")
            onPlay(it)
        }
    }

    private fun onReStartPlayer(exoStyledPlayerView: ExoStyledPlayerView?) {
        exoStyledPlayerView?.let {
            DLogger.d(TAG, "### onReStartPlayer =>  ${it.getFeedItem().feedContentsData?.songNm} /  ${getPage()} ")
            onPlay(it)
        }
    }

    private fun onPlay(pv: ExoStyledPlayerView) {
        //삭제된 컨텐츠인 경우 플레이 하지않음. pause
        if (pv.getFeedItem().feedContentsData?.isDeleted == true || pv.getFeedItem().feedContentsData?.isSongDelYn == true) {
            togglePlayButton(pv, false)
            exoProvider.onPause()
            return
        }

        Single.just(pv)
            .map {
                setPageInfo(getPage(), it)
                it
            }
            .applyApiScheduler()
            .map {
                exoProvider.setPrepare(it)
                it
            }
            .doOnSuccess {
                exoProvider.onPlay(it)
                setCurrentPlayer(it)
                initFeedDetail(it)
            }.subscribe().addTo(disposable)
    }


    /**
     * 시작/재시작
     */
    fun onResume() {
        getCurrentPlayer()?.let { pv ->
            if (pv.getFeedItem().feedContentsData?.isDeleted == true || pv.getFeedItem().feedContentsData?.isSongDelYn == true) return

            DLogger.d(TAG, "### onResume=> ${page} /  ${pv.getFeedItem().feedContentsData?.songNm}")
            getCurrentPlayer()?.player?.let {
                setPageInfo(getPage(), pv)
                exoProvider.onResume(pv)
            } ?: run {
                onReStartPlayer(pv)
            }
        } ?: run {
            onReStartPlayer(getCurrentPlayer())
            DLogger.d(TAG, "### onResume else player null")
        }
    }

    /**
     * 정지
     */
    fun onPause() {
        getCurrentPlayer()?.let { pv ->
            exoProvider.onPause(pv)
        } ?: run {
            DLogger.d(TAG, "### onPause playerview is null")
        }
    }

    fun onStopPlayer() {
        getCurrentPlayer()?.let {
            DLogger.d(TAG, "### onStopPlayer ${page}")
            exoProvider.onStop(it)
        }
    }

    /**
     * 리소스 정리
     */
    fun onReset() {
        getCurrentPlayer()?.let { pv ->
            exoProvider.onStop(pv)
            mPlayersMap.clear()
            setCurrentPlayer(null)
        }
    }


    private fun initFeedDetail(pv: ExoStyledPlayerView) {

        if (getPage() != ExoPageType.FEED_DETAIL) {
            return
        }

        setFeedDetailViewState(false)

        pv.getFeedItem().sbBar?.let {
            it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (isEditing) {
                        getCurrentPlayer()?.let { exoView ->
                            exoView?.player?.let {
                                val duration = progress.toFloat() / 100.0 * it.contentDuration
                                setPlayingTime(exoView, duration.toInt())
                                exoProvider.onMoToSeek(duration.toInt())
                            }
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isEditing = true
                    onPause()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isEditing = false
                    onMain {
                        delay(500)
                        onResume()
                    }
                }
            })
        }

        exoProvider.setPlayingListener(object : ExoProviderProviderImpl.Listener {
            override fun onCurrentMs(ms: Long) {
                val totalTime = exoProvider.getContentDuration(pv)
                val progressValue = ms.toDouble() / totalTime.toDouble()

                if (pv.getFeedItem().totalTime?.text != Common.getTimeString(totalTime.toInt())) {
                    pv.getFeedItem().totalTime?.text = Common.getTimeString(totalTime.toInt())
                }
                setPlayingTime(pv, ms.toInt())
                setSeekBar(pv, (progressValue * 100).toInt())
            }

            override fun onSwitchingPlay(exoPlayerView: ExoStyledPlayerView) {
                setDefaultPlayingView(exoPlayerView)
            }
        })
    }

    /**
     * 피드 상세 준비 전 Seekbar 막음.
     */
    private fun setFeedDetailViewState(state: Boolean) {

        if (getPage() != ExoPageType.FEED_DETAIL) {
            return
        }
        getCurrentPlayer()?.let {
            it.getFeedItem().sbBar?.let {
                it.isEnabled = state
                it.isClickable = state
            }
        }
    }

    /**
     * 플레이어뷰 전환시 seekbar,playing default
     */
    fun setDefaultPlayingView(pv: ExoStyledPlayerView) {
        if (getPage() == ExoPageType.FEED_DETAIL) {
            setPlayingTime(pv, 0)
            setSeekBar(pv, 0)
        }
    }

    /**
     * 진행 시간
     */
    fun setPlayingTime(pv: ExoStyledPlayerView, ms: Int) {
        val text = Common.getTimeString(ms)
        if (pv.getFeedItem().playingTime?.text != text) {
            pv.getFeedItem().playingTime?.text = text
        }
    }

    /**
     * Set Seekbar progress
     */
    fun setSeekBar(pv: ExoStyledPlayerView, progressValue: Int) {
        if (pv.getFeedItem().sbBar?.progress != progressValue) {
            pv.getFeedItem().sbBar?.progress = progressValue
        }
    }


    /**
     * 뷰 애니메이션
     */
    private fun handleViewState(pv: ExoStyledPlayerView, isPlaying: Boolean) {
        toggleThumbnail(pv, isPlaying)
    }

    /**
     * LifeCycle Event
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        when (event) {
            Lifecycle.Event.ON_RESUME -> {

                if (getCurrentPlayer()?.getFragmentActivity() is MainActivity) {

                    val mainActivity = getCurrentPlayer()?.getFragmentActivity() as MainActivity

                    val fragment = mainActivity.supportFragmentManager.getFragment<MainFragment>()

                    val lastFragment = fragment?.childFragmentManager?.fragments?.last()

                    DLogger.d(TAG, "exoProvider.setExoLock  lastFragment =>  ${lastFragment}")

                    if (lastFragment is MyPageFragment || lastFragment is CommunityFragment) {
                        exoProvider.setExoLock(true)
                    } else {
                        if(lastFragment is SingPassFragment){
                            lastFragment.viewModel.singPassDataList.value?.let {
                                exoProvider.setExoLock(false)
                            }?:run {
                                exoProvider.setExoLock(true)
                            }
                        }else{

                            if(lastFragment is MainTabFragment){
                                getCurrentPlayer()?.let {
                                    setPageInfo(getPage(),it)
                                }
                            }
                            if(!mainActivity.viewModel.isShowSingDialog.value){
                                exoProvider.setExoLock(false)
                            }
                        }
                    }
                }

                DLogger.d(TAG, "onStateChanged ON_RESUME =>  ${page} /  ${exoProvider.getPage()}  / ${exoProvider.isExoLock()} / ${getCurrentPlayer()?.getFragmentActivity()}")

                if (exoProvider.isExoLock()) {
                    DLogger.d("ON_RESUME LOCK RETURN")
                    return
                }

                if (getPage() == exoProvider.getPage()) {
                    if (mPlayersMap.isNotEmpty()) {
                        onResume()
                    }
                }
//                else {
//                    onReStartPlayer(getCurrentPlayer())
//                }
            }

            Lifecycle.Event.ON_PAUSE -> {
                DLogger.d(TAG, "onStateChanged ON_PAUSE =>  ${page} ${exoProvider.getPage()} / ${exoProvider.isExoLock()}")
                onPause()
            }

            Lifecycle.Event.ON_DESTROY -> {
                onReset()
                closeDisposable()
            }

            Lifecycle.Event.ON_CREATE -> {}
            Lifecycle.Event.ON_START -> {}
            Lifecycle.Event.ON_STOP -> {}
            else -> {
            }
        }
    }


    override fun getLifecycle() = lifecycleRegistry

    override fun onPlaybackStateChanged(playbackState: Int) {

        when (playbackState) {
            //준비
            Player.STATE_BUFFERING -> {
                //setFeedDetailViewState(false)

                if (getPage() != exoProvider.getPage()) {
                    getCurrentPlayer()?.let {
                        it.getFeedItem()?.thumbnailView?.visibility = View.VISIBLE
                    }
                }
                DLogger.d(TAG, "#### STATE_BUFFERING   ${mCurPlayerView?.getFeedItem()?.feedContentsData?.songNm} / ${page}")
            }
            //준비완료
            Player.STATE_READY -> {
                setFeedDetailViewState(true)
                DLogger.d(TAG, "#### STATE_READY   ${mCurPlayerView?.getFeedItem()?.feedContentsData?.songNm} / ${page}")
            }
            //재생X
            Player.STATE_IDLE -> {
                //setFeedDetailViewState(false)
                //switchTargetView 시 썸네일
                DLogger.d(TAG, "#### STATE_IDLE   ${mCurPlayerView?.getFeedItem()?.feedContentsData?.songNm} / ${page}")
            }
            //재생 끝
            Player.STATE_ENDED -> {
                //  setFeedDetailViewState(false)
                DLogger.d(TAG, "#### STATE_ENDED   ${mCurPlayerView?.getFeedItem()?.feedContentsData?.songNm} / ${page}")
            }
        }
    }


    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (getPage() == exoProvider.getPage()) {
            getCurrentPlayer()?.let {
                DLogger.d(TAG, "onIsPlayingChanged: ${it.getFeedItem()?.feedContentsData?.songNm} / ${isPlaying} ${it.player?.currentPosition}  / ${page} / ${exoProvider.getPage()} ")
                handleViewState(it, isPlaying)
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (getPage() == exoProvider.getPage()) {
            getCurrentPlayer()?.let {
                DLogger.d(TAG, "onPlayWhenReadyChanged ${playWhenReady} /  ${it.getFeedItem()?.feedContentsData?.songNm}   / ${page} / ${exoProvider.getPage()} ")
                togglePlayButton(it, playWhenReady)
            }
        }
    }


    override fun onPlayerError(error: PlaybackException) {
        exoStateListener?.onStateError()
        DLogger.d(TAG, "onPlayerError: ${error.errorCode} ${error.message}")
    }


    /**
     * 썸네일 Show or Hide
     * 레코드판
     * 피드 중앙 플레이버튼
     * @param isShow true : 보이게 , false : 숨김
     */
    private fun toggleThumbnail(pv: ExoStyledPlayerView, state: Boolean) {

        pv?.let { exoView ->

            Single.just(state)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->

                    exoView.getFeedItem()?.let {

                        if (state) {

                            it.thumbnailView?.let { thumView ->
                                thumView.visibility = View.GONE
                            }

                            if(it.feedContentsData?.isFeedContents == true){
                                it.albumView?.let { albumView ->
                                    AnimationUtils.loadAnimation(albumView.context, R.anim.rotate)?.let {
                                        albumView.startAnimation(it)
                                    }
                                }
                            }

                            it.btBtnPlayView?.let {
                                it.isSelected = true
                            }

                        } else {
//                            it.thumbnailView?.let { thumView ->
//                                thumView.visibility = View.VISIBLE
//                            }

                            if(it.feedContentsData?.isFeedContents == true){
                                it.albumView?.let { albumView ->
                                    albumView.clearAnimation()
                                }
                            }

                            it.btBtnPlayView?.let {
                                it.isSelected = false
                            }
                        }
                    }
                }
        }
    }

    private fun toggleAniAlbum(view: ExoStyledPlayerView, state: Boolean) {
        if (state) {
            view.getFeedItem().albumView?.let { albumView ->
                AnimationUtils.loadAnimation(albumView.context, R.anim.rotate)?.let {
                    albumView.startAnimation(it)
                }
            }
        } else {
            view.getFeedItem().albumView?.let { albumView ->
                albumView.clearAnimation()
            }
        }
    }

    /**
     * 중앙 플레이 버튼
     * 피드 상세 하단 플레이 버튼
     */
    private fun togglePlayButton(view: ExoStyledPlayerView, state: Boolean) {
        if (state) {
            view.getFeedItem().btnPlayView?.let { btnPlayView ->
                btnPlayView.visibility = View.GONE
            }
            view.getFeedItem().btBtnPlayView?.let {
                it.isSelected = true
            }
        } else {

            view.getFeedItem().btnPlayView?.let { btnPlayView ->
                btnPlayView.visibility = View.VISIBLE
            }
            view.getFeedItem().btBtnPlayView?.let {
                it.isSelected = false
            }
        }
    }

    /**
     * 현재 PlayerView Get
     */
    fun getCurrentPlayer(): ExoStyledPlayerView? {
        return mCurPlayerView
    }

    /**
     * 현재 페이지
     */
    private fun getPage(): ExoPageType {
        return page
    }

    /**
     * exo set
     */
    private fun setPageInfo(curPage: ExoPageType, pv: ExoStyledPlayerView) {
        exoProvider.setPageInfo(curPage, this)
    }

    /**
     * 컨텐츠 캐싱
     */
    fun goPreLoad(dataList: MutableList<FeedContentsData>) {
        exoProvider.onPreLoadList(dataList)
    }

    /**
     * 컨텐츠 캐싱
     */
    fun goPreLoad(url: String) {
        exoProvider.onPreLoad(url)
    }


    /**
     *  컨텐츠 컈싱 List
     */
    fun goPreLoadURL(dataList: MutableList<String>) {
        exoProvider.onPreLoadURLList(dataList)
    }


    /**
     * 플레잉 여부
     */
    fun isPlaying(): Boolean {
        return exoProvider.isPlaying()
    }

    fun closeDisposable() {
        if (!disposable.isDisposed) {
            disposable?.dispose()
        }
    }

    fun getPlayerSize() : Int{
        return mPlayersMap.size
    }
}
