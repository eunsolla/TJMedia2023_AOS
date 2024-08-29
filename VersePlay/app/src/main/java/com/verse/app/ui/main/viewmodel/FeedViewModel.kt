package com.verse.app.ui.main.viewmodel

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.BaseFeedViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExoPageType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.RecommendUserData
import com.verse.app.model.param.FollowBody
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.usecase.PostFollowUseCase
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.FeedRefreshProvider
import com.verse.app.utility.exo.ExoStyledPlayerView
import com.verse.app.widget.views.ExoPagerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-05-20
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postFollowUseCase: PostFollowUseCase,
    private val feedRefreshProvider: FeedRefreshProvider
) : BaseFeedViewModel() {

    companion object {
        const val PAGE_COUNT = 20
    }

    val startOneButtonPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }      // popup

    //추천 피드 or 추천 유저 구분
    private val _isFollowFeed: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isFollowFeed: NonNullLiveData<Boolean> get() = _isFollowFeed

    //empty
    private val _isEmptyPage: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val isEmptyPage: LiveData<Boolean> get() = _isEmptyPage

    //추천 유저
    private val _recommendUser: ListLiveData<RecommendUserData> by lazy { ListLiveData() }
    val recommendUser: ListLiveData<RecommendUserData> get() = _recommendUser

    val _isFirstPlay: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }

    private val _isLastView: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isLastView: LiveData<Boolean> get() = _isLastView

    //추천 유저 팔로잉 처리 후 갱신
    private val _refreshPosition: SingleLiveEvent<Pair<Int, Any>> by lazy { SingleLiveEvent() } //refresh position
    val refreshPosition: SingleLiveEvent<Pair<Int, Any>> get() = _refreshPosition

    private val _startAgainFollowingFeed: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } //refresh position
    val startAgainFollowingFeed: SingleLiveEvent<Unit> get() = _startAgainFollowingFeed

    private val _pagePosition: MutableLiveData<Int> by lazy { MutableLiveData() }
    val pagePosition: LiveData<Int> get() = _pagePosition


    private val _loadFinished: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val loadFinished: LiveData<Unit> get() = _loadFinished


    fun start() {

        val exoPagerItem: ExoPagerItem = savedStateHandle[ExtraCode.EXO_PAGE_TYPE] ?: return

        val pageType: ExoPageType = exoPagerItem.type

        _pagePosition.value = exoPagerItem.pos
        _isFirstPlay.value = exoPagerItem.isFirstPlay
        _exoPageType.value = pageType

        pageType?.let {
            DLogger.d("pageType=> ${pageType}")
            request(it)
        }
    }

    /**
     * 팔로잉/추천탭 구분
     */
    fun request(pageType: ExoPageType) {
        when (pageType) {
            ExoPageType.MAIN_RECOMMEND -> fetchRecommend()
            ExoPageType.MAIN_FOLLOWING -> fetchFollowing()
            else -> {
                DLogger.d("EXO TYPE IS NONE")
            }
        }
    }


    /**
     * 메인 -> 피드
     */
    private fun fetchFollowing() {
        apiService.fetchFollowingFeed(_pageNo.value)
            .map {
                if (it.result.feedList.isNotEmpty()) {
                    goPreLoadContents(it.result.feedList)
                    feedRefreshProvider.setUpdateFeed(it.result.feedList)
                    if (it.result.feedList.size < it.result.pageSize) {
                        it.result.feedList.last().isFollowingContentsLast = true
                    }
                } else {
                    if (_feedList.value.isNotEmpty()) {
                        _feedList.value.last().isFollowingContentsLast = true
                    }
                }
                it
            }
            .applyApiScheduler()
            .request({ response ->
                if (response.status == HttpStatusType.SUCCESS.status) {
                    response.result.let { result ->
                        parsingFollowing(result.userList, result.feedList)
                        DLogger.d("fetchFollowing result size=> ${_recommendUser.size} /  ${_feedList.size} / ${_feedList.hashCode()}")
                    }
                } else {
                    DLogger.d("fail FollowingInfo")
                    _loadFinished.call()
                }

            }, {
                _loadFinished.call()
                DLogger.d("error FollowingInfo=>${it}")
            })
    }

    /**
     * Follow Set
     */
    private fun parsingFollowing(
        userList: MutableList<RecommendUserData>,
        feedList: MutableList<FeedContentsData>
    ) {
        if (_pageNo.value == 1) {
            _feedList.clear()
            _recommendUser.clear()
            _isFollowFeed.value = !feedList.isNullOrEmpty()
            _isEmptyPage.value = feedList.isEmpty() && userList.isEmpty()
        }

        if (_isEmptyPage.value == false) {
            if (!feedList.isNullOrEmpty()) {
                _feedList.addAll(feedList)
            } else {
                _loadFinished.call()
            }

            if (!userList.isNullOrEmpty()) {
                _recommendUser.addAll(userList)
                _loadFinished.call()
            }
        }

    }

    /**
     * 메인 -> 추천
     */
    private fun fetchRecommend() {

        apiService.fetchRecommendContents(_pageNo.value)
            .map {
                goPreLoadContents(it.result.dataList)
                it
            }
            .map {
                // 피드 자동 업데이트 정보 갱신 처리
                feedRefreshProvider.setUpdateFeed(it)
                it
            }
            .applyApiScheduler()
            .request({ response ->
                if (response.status == HttpStatusType.SUCCESS.status) {

                    if (_pageNo.value == 1) {
                        _isEmptyPage.value = false
                    }

                    if (!response.result.dataList.isNullOrEmpty()) {

                        _feedList.addAll(response.result.dataList)

                        if (_feedList.value.size <= 0) {
                            _isEmptyPage.value = true
                            _loadFinished.call()
                        }
                    }
                } else {
                    DLogger.d("fail fetchRecommend")
                    _loadFinished.call()
                }
            }, {
                _loadFinished.call()
                DLogger.d("error fetchRecommend=>${it}")
            })
    }


    /**
     * 팔로우 설정/해제
     */
    fun onFollow(pos: Int, data: RecommendUserData) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)
            return
        }

        postFollowUseCase(FollowBody(data.memCd, !data.isFollow))
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    if (it.status == HttpStatusType.SUCCESS.status) {
                        data.isFollow = !data.isFollow
                        _refreshPosition.value = pos to data
                    } else {
                        DLogger.d("failfollow ")
                    }
                } else if (it.status == HttpStatusType.FAIL.status) {
                    startOneButtonPopup.value = it.message
                }

            }, {
                DLogger.d("error follow ${it}")
            })
    }


    /**
     * 팔로잉 추천 유저 마지막
     */
    fun onFollowingMorePopupClose(view: View) {
        view.visibility = View.GONE
    }

    /**
     * 페이징 여부
     */
    fun checkPaging() {
        DLogger.d("checkPaging=> ${_feedList.value.size} / ${vpCurPosition.value} ")
        if (_feedList.value.size >= PAGE_COUNT && _feedList.value.size / 2 == vpCurPosition.value) {
            pageNo.value = pageNo.value.plus(1)
            request(_exoPageType.value)
        }
    }

    /**
     * 팔로잉 추천 마지막 뷰 띄움
     */
    fun checkLastView(state: Boolean) {
        if (_exoPageType.value == ExoPageType.MAIN_FOLLOWING && _feedList.value.isNotEmpty()) {
            if (state) {
                val isLast = _feedList.value[vpCurPosition.value].isFollowingContentsLast
                if (_isLastView.value == isLast) return
                _isLastView.value = isLast
            } else {
                if (_isLastView.value == true) {
                    _isLastView.value = false
                }
            }
        }
    }

    override fun setPlayerView(playerView: ExoStyledPlayerView) {
        super.setPlayerView(playerView)
        if (playerView.getFeedItem().position == 0 && _isFirstPlay.value) {
            _isFirstPlay.value = false
            DLogger.d("메인 네비!!  _exoPageType setPlayerView ${_exoPageType.value}")
            onExoPlay()
            _loadFinished.call()
        }
    }

    fun onAgainFollowingFeed() {
        _startAgainFollowingFeed.call()
    }

    fun resetFollowFeed() {
        _isLastView.value = false
        _isFirstPlay.value = true
    }

    fun resetRecommend() {
        _isFirstPlay.value = true
    }

    override fun onRefresh() {
        super.onRefresh()
        request(_exoPageType.value)
    }
}