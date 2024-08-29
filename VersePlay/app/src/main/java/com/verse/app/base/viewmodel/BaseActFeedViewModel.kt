package com.verse.app.base.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.verse.app.contants.ExoPageType
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.exo.ExoManager
import com.verse.app.utility.exo.ExoStyledPlayerView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
open class BaseActFeedViewModel @Inject constructor() : ActivityViewModel() {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var deviceProvider: DeviceProvider

    @Inject
    lateinit var exoManager: ExoManager

    @Inject
    lateinit var loginManager: LoginManager

    //일반
    open val _feedList: ListLiveData<FeedContentsData> by lazy { ListLiveData() }
    val feedList: ListLiveData<FeedContentsData> get() = _feedList

    private val _close: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val close: SingleLiveEvent<Unit> get() = _close

    //페이징
    open val _feedListPaging: MutableLiveData<PagingData<FeedContentsData>> by lazy { MutableLiveData() }
    val feedListPaging: LiveData<PagingData<FeedContentsData>> get() = _feedListPaging

    //페이지 타입
    open val _exoPageType: NonNullLiveData<ExoPageType> by lazy { NonNullLiveData(ExoPageType.NONE) }
    val exoPageType: LiveData<ExoPageType> get() = _exoPageType

    open val _pageNo: NonNullLiveData<Int> by lazy { NonNullLiveData(1) }
    val pageNo: NonNullLiveData<Int> get() = _pageNo

    // VP
    private val _vpState: MutableLiveData<Int> by lazy { MutableLiveData() }
    val vpState: LiveData<Int> get() = _vpState

    private val _vpAni: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val vpAni: NonNullLiveData<Boolean> get() = _vpAni

    protected val _vpCurPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }
    val vpCurPosition: NonNullLiveData<Int> get() = _vpCurPosition

    protected val _vpPrePosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }
    val vpPrePosition: NonNullLiveData<Int> get() = _vpPrePosition


    protected val _startFeedDetailEvent: SingleLiveEvent<Triple<Int,String, PagingData<FeedContentsData>>> by lazy { SingleLiveEvent() }
    val startFeedDetailEvent: LiveData<Triple<Int,String, PagingData<FeedContentsData>>> get() = _startFeedDetailEvent

    /**
     * 추천 ViewPager 상태 - Vertical
     */
    fun pageState(state: Int) {
        _vpState.value = state
    }

    /**
     * PlayerView Set
     */
    override fun setPlayerView(playerView: ExoStyledPlayerView) {
        playerView?.let {
            exoManager?.setPlayerView(it, _exoPageType.value)
        }
    }

    /**
     * data clear
     */
    open fun <T : Any> clearVariable(liveData: ListLiveData<T>) {
        liveData.clear()
        _vpCurPosition.value = 0
        _vpPrePosition.value = 0
    }

    open fun onRefresh() {
        _feedList.clear()
        _feedListPaging.value = PagingData.empty()
        _vpCurPosition.value = 0
        _vpPrePosition.value = -1
        _pageNo.value = 1
        exoManager.onReset()
    }

    open fun onBack() {}

    /**
     * 재생 시작
     */
    open fun onExoPlay() {
        if (vpPrePosition.value != vpCurPosition.value) {
            exoManager.onStartPlayer(_vpCurPosition.value)
            _vpPrePosition.value = _vpCurPosition.value
        } else {
            onExoResume()
        }
    }

    /**
     * 화면 전환 후 resume 시 처음부터 재 시작
     */
    open fun onExoRePlay() {
        exoManager.onStartPlayer(_vpCurPosition.value)
    }

    /**
     * 정지 시점부터 재시작
     */
    open fun onExoResume() {
        exoManager.onResume()
    }

    /**
     * 정지
     */
    open fun onExoPause() {
        exoManager.onPause()
    }

    fun onFollowingMorePopupClose(view: View) {
        view.visibility = View.GONE
    }

    /**
     * 컨텐츠 상세 리스트 이동
     */
    override fun moveToFeedDetail(index: Int, mngCd :String ,dataList: PagingData<FeedContentsData>) {
        DLogger.d("mainAct moveToFeedDetail")
        _startFeedDetailEvent.value = Triple (index , mngCd , dataList)
    }


    override fun togglePlayAndPause() {
        if (exoManager.getCurrentPlayer() != null) {
            exoManager.getCurrentPlayer()?.player?.let {
                if (it.isPlaying) {
                    exoManager.onPause()
                } else {
                    exoManager.onResume()
                }
            }
        }
    }
}