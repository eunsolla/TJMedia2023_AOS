package com.verse.app.ui.feed.viewmodel

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.BaseActFeedViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.BookMarkType
import com.verse.app.contants.CollectionType
import com.verse.app.contants.ExoPageType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.SingType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.onMain
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.param.BookMarkBody
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.exo.ExoStyledPlayerView
import com.verse.app.utility.manager.UserSettingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Description : 피드 모아보기  ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class CollectionFeedViewModel @Inject constructor() : BaseActFeedViewModel() {

    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startBack: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val showErrorDialog: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val updateFavorite: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val showFilter: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }    // 필터 Show

    val currentSortType: NonNullLiveData<String> by lazy { NonNullLiveData(SortEnum.DESC.query) }
    val collectionContentsCount: MutableLiveData<Int> by lazy { MutableLiveData() }
    val isBookMark: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }

    val collectionType: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val _collectionParam: MutableLiveData<String> by lazy { MutableLiveData() } //tag
    val collectionParam: LiveData<String> get() = _collectionParam //tag

    private val _feedContentsDataParam: MutableLiveData<FeedContentsData> by lazy { MutableLiveData() }
    val feedContentsDataParam: LiveData<FeedContentsData> get() = _feedContentsDataParam

    val moveToSing: SingleLiveEvent<Triple<String, String, String>> by lazy { SingleLiveEvent() } //부르기 이동 param

    val startCheckPrivateAccount: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 비공개 계정 여부 확인

    fun start() {

        val type: String? = savedStateHandle[ExtraCode.COLLECTION_TYPE]

        DLogger.d("COLLECTION_TYPE : ${type}")

        if (type.isNullOrEmpty()) {
            showErrorDialog.call()
            return
        }

        //type
        collectionType.value = type


        if (type == CollectionType.TAG.code) {   //TAG

            val collectionParam: String? = savedStateHandle[ExtraCode.COLLECTION_PARAM]

            if (collectionParam.isNullOrEmpty()) {
                showErrorDialog.call()
                return
            }
            _collectionParam.value = collectionParam

        } else if (type == CollectionType.FEED.code) {   //FEED

            val feedContentsData: FeedContentsData? = savedStateHandle[ExtraCode.COLLECTION_FEED_PARAM]

            if (feedContentsData == null) {
                showErrorDialog.call()
                return
            }
            _feedContentsDataParam.value = feedContentsData
        }

        _exoPageType.value = ExoPageType.FEED_DETAIL

        //call
        requestCollectionList()
    }

    fun close() {
        startFinish.call()
    }

    /**
     * 모아보기 API
     */
    fun requestCollectionList() {
        onLoadingShow()
        if (collectionType.value == CollectionType.FEED.code) {
            _feedContentsDataParam.value?.let {
                //피드 컨텐츠
                repository.fetchCollectionFeedList(this, it.songMngCd, currentSortType.value)
                    .applyApiScheduler()
                    .cachedIn(viewModelScope)
                    .request({ response ->
                        _feedListPaging.value = response
                    })

            } ?: run {
                showErrorDialog.call()
            }
        } else {
            //태그 컨텐츠
            _collectionParam.value?.let {
                repository.fetchCollectionTagList(this, it, currentSortType.value)
                    .applyApiScheduler()
                    .cachedIn(viewModelScope)
                    .request({ response ->
                        _feedListPaging.value = response
                    })

            } ?: run {
                showErrorDialog.call()
            }
        }
    }

    /**
     * 반주음 즐겨찾기 추가
     */
    fun updateFavorite(view: View) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        var bookMarkYn = if (isBookMark.value) AppData.N_VALUE else AppData.Y_VALUE

        _feedContentsDataParam.value?.let {
            apiService.updateBookMark(
                BookMarkBody(
                    bookMarkType = BookMarkType.SONG.code,
                    bookMarkYn = bookMarkYn,
                    contentsCode = it.songMngCd
                ))
                .applyApiScheduler()
                .request({ res->
                    if (res.status == HttpStatusType.SUCCESS.status) {

                        if (UserSettingManager.isPrivateUser() && bookMarkYn == AppData.Y_VALUE) {
                            startCheckPrivateAccount.call()

                        } else {
                            isBookMark.value = !isBookMark.value
                        }

                    } else {
                        DLogger.d("fail bookmark")
                    }
                }, {error->
                    DLogger.d("Error bookmark=>${error.message}")
                })
        }
    }

    /**
     * 파트선택,부르기 페이지 이동
     */
    fun onMoveToSing(data: FeedContentsData) {
        data?.let {
            if (it.songMngCd.isNotEmpty()) {
                moveToSing.value = Triple(SingType.SOLO.code, it.songMngCd, "")
            }
        }
    }

    fun onFilterClick() {
        showFilter.call()
    }

    fun clickFeedItem(itemPosition: Int,feedMngCd:String) {
        _feedListPaging.value?.let {
            moveToFeedDetail(itemPosition,feedMngCd, it)
        }
    }

    override fun setPlayerView(playerView: ExoStyledPlayerView) {
        super.setPlayerView(playerView)

        if (playerView.getFeedItem().position == _vpCurPosition.value) {
            onMain {
                delay(300)
                exoManager.onStartPlayer(_vpCurPosition.value)
            }
        }
    }

    override fun onBack() {
        startBack.call()
    }
}