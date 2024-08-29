package com.verse.app.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.BaseActFeedViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.MainStructureType
import com.verse.app.contants.NaviType
import com.verse.app.extension.SimpleDisposableSubscriber
import com.verse.app.extension.applyApiScheduler
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.exo.ExoProvider
import com.verse.app.utility.exo.ExoStyledPlayerView
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-01-01
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val accountPref: AccountPref,
    val exoProvider: ExoProvider,
) : BaseActFeedViewModel(),
    GalleryBottomSheetDialog.Listener {

    //백버튼
    private val backButtonSubject: Subject<Long> = BehaviorSubject.createDefault(0L).toSerialized()
    private val _startRefresh: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startRefresh: LiveData<Unit> get() = _startRefresh

    //인트로 종료
    private val _startMain: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val startMain: LiveData<Boolean> get() = _startMain

    //종료 여부
    val finish = SingleLiveEvent<Boolean>()

    //[s]메인============================================================================================================
    //메인 가로 Fragment ViewPager (Feed / User Feed) - Horizontal
    val mainStructureList: ListLiveData<MainStructureType> by lazy {
        ListLiveData<MainStructureType>().apply {
            add(MainStructureType.MAIN_FEED)
            add(MainStructureType.USER_FEED)
        }
    }
    val vpMainState: MutableLiveData<Int> by lazy { MutableLiveData() }                             //Main ViewPager 상태
    val vpMainPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }           //Main viewPager 현재 position
    val isVpMainAni: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }    //Main ViewPager Ani 여부
    val vpMainSwipeState: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }    //Main ViewPager 드래그 여부

    //메인 갱신은 메인 탭에서만..
    private val _isSwipeEnable: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val isSwipeEnable: LiveData<Boolean> get() = _isSwipeEnable


    //[e]메인============================================================================================================

    //노래 콘텐츠 업로드 이동
    val moveToSingContentsUpload: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    //앨범 동영상 업로드 이동
    private val _moveToAlbumContentsUpload: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val moveToAlbumContentsUpload: LiveData<Unit> get() = _moveToAlbumContentsUpload
    private val _showAlbumContentsUpload: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val showAlbumContentsUpload: LiveData<Unit> get() = _showAlbumContentsUpload

    //앨범 업로드 이동
    private val _startVideoUpload: SingleLiveEvent<List<String>> by lazy { SingleLiveEvent() }
    val startVideoUpload: LiveData<List<String>> get() = _startVideoUpload

    val _isAd: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }    //현재 컨텐츠가 광고인지

    //앨범 동영상 업로드 이동
    private val _curNaviPage: SingleLiveEvent<RxBusEvent.NaviEvent> by lazy { SingleLiveEvent() }
    val curNaviPage: LiveData<RxBusEvent.NaviEvent> get() = _curNaviPage

    private val _prevNaviPage: SingleLiveEvent<NaviType> by lazy { SingleLiveEvent() }
    val prevNaviPage: LiveData<NaviType> get() = _prevNaviPage

    private val _isLoading: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isExoPlaying: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isExoPlaying: NonNullLiveData<Boolean> get() = _isExoPlaying

    private val _isShowSingDialog: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val isShowSingDialog: NonNullLiveData<Boolean> get() = _isShowSingDialog

    fun initStart() {
        backButtonSubject.toFlowable(BackpressureStrategy.BUFFER)
            .observeOn(AndroidSchedulers.mainThread())
            .buffer(2, 1)
            .map { it[0] to it[1] }
            .subscribeWith(object : SimpleDisposableSubscriber<Pair<Long, Long>>() {
                override fun onNext(t: Pair<Long, Long>) {
                    finish.value = t.second - t.first < 2000
                }
            }).addTo(compositeDisposable)

        setInitViewCreated()

//        viewModelScope.launch {
//            var cnt = 0
//            while (cnt < 3) {
//                for (idx in 0 until 1000) {
//                    val progress = idx.toFloat() / 10.0F
//                    RxBus.publish(UploadProgressAudio(progress))
//                    delay(10)
//                }
//                RxBus.publish(UploadProgressAudio())
//                delay(5_000)
//                cnt++
//            }
//        }
    }

    fun start() {
        setMainLoading(true)
        _startMain.value = true
    }

    /**
     * 하단 네비 동작
     */
    fun startInitNaviListen() {
        // 이벤트 여러번 호출시 맨 마지막 이벤트을 받아서 처리
        RxBus.listen(RxBusEvent.NaviEvent::class.java)
            .debounce(100, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .subscribe({
                if (_isLoading.value) {
                    return@subscribe
                }
                DLogger.d("메인 네비> 현재=>${_curNaviPage.value} / 이동=>${it.type}")
                _isExoPlaying.value = exoProvider.isPlaying()
                _curNaviPage.value = it
            }, {}).addTo(compositeDisposable)
    }

    /**
     * 메인 준비 여부
     */
    fun setMainLoading(state: Boolean) {

        if (state) {
            onLoadingShow()
        } else {
            onLoadingDismiss()
        }
        setEnableRefresh(!state)

        if (_isLoading.value != state) {
            _isLoading.value = state
        }
    }

    /**
     * 하단 이전 네비
     */
    fun setPrevNaviPage(type: NaviType) {
        _prevNaviPage.value = type
    }

    fun onStartRefresh() {
        _startRefresh.call()
    }


    fun setIsAd(state: Boolean) {
        _isAd.value = state
    }

    /**
     * Main ViewPager 상태 - Horizontal
     */
    fun pageMainState(state: Int) {
        vpMainState.value = state
    }

    /**
     * Main ViewPAger Swipe 가능 여부
     */
    fun setEnableMainViewpager(state: Boolean) {

        if (vpMainSwipeState.value == state) {
            return
        }

        if (loginManager.isLogin() && !_isAd.value) {
            vpMainSwipeState.value = state
        } else {
            vpMainSwipeState.value = false
        }
    }

    /**
     * 노래 콘텐츠 업로드 이동
     */
    fun moveToSingContentsUploadPage() {
        moveToSingContentsUpload.call()
    }

    fun moveToAlbumContentsUploadPage() {
        _moveToAlbumContentsUpload.call()
    }

    /**
     * 앨범 동영상 업로드 이동
     */
    fun showAlbumContentsUploadPage() {
        _showAlbumContentsUpload.call()
    }


    fun onBackPressed() {
        backButtonSubject.onNext(System.currentTimeMillis())
    }

    fun exoPlayerLock(isLock: Boolean) {
        exoProvider.setExoLock(isLock)
    }

    /**
     *  swipe refresh 상태
     */
    fun setEnableRefresh(state: Boolean) {
        _isSwipeEnable.value = state
    }

    override fun setPlayerView(playerView: ExoStyledPlayerView) {
        super.setPlayerView(playerView)
        playerView?.let {
            //0번째 선택된 포지션이면 플레이
            if (it.getFeedItem().position == vpCurPosition.value) {
                DLogger.d(" setPlayerView=> ${_exoPageType.value} / ${it.getFeedItem().position} /  ${vpCurPosition.value}")
                onExoPlay()
            }
        }
    }

    override fun onBack() {
        close.call()
    }

    override fun onGalleryConfirm(imageList: List<String>) {
        setShowSingDialog(false)
        if (imageList.isEmpty()) return
        DLogger.d("result Gallery Confirm=> ${imageList?.size}")
        _startVideoUpload.value = imageList
    }

    override fun onGalleryDismiss() {
        setShowSingDialog(false)
        if(_isExoPlaying.value){
            exoPlayerLock(false)
            exoProvider.onResume()
        }
    }

    fun updateNightTimePushAgree(isOnSwitch: Boolean) {
        var updateValue: String

        if (isOnSwitch) {
            updateValue = AppData.Y_VALUE
        } else {
            updateValue = AppData.N_VALUE
        }

        apiService.updateSettings(
            UploadSettingBody(
                alRecTimeYn = updateValue
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    DLogger.d("updateNightTimePushAgree Success")
                } else {
                    DLogger.d("updateNightTimePushAgree Fail")
                }
            }, {
                DLogger.d("updateNightTimePushAgree Error")
            })
    }

    fun setShowSingDialog(state: Boolean) {
        _isShowSingDialog.value = state
    }

}