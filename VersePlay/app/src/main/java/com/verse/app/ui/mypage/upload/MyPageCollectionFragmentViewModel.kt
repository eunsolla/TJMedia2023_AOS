package com.verse.app.ui.mypage.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.FeedSubDataType
import com.verse.app.contants.LoadingDialogState
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.param.MyPageUploadFeedQueryMap
import com.verse.app.repository.paging.Repository
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/29
 */
@HiltViewModel
class MyPageCollectionFragmentViewModel @Inject constructor(
    private val loginManager: LoginManager,
    private val repository: Repository
) : FragmentViewModel(), FilterDialogFragment.Listener {

    private val _isRefresh: MutableLiveData<Boolean> by lazy { MutableLiveData() }                // 필터 검색
    val isRefresh: LiveData<Boolean> get() = _isRefresh


    private val _pagedList: MutableLiveData<PagingData<FeedContentsData>> by lazy { MutableLiveData() }
    val pagedList: LiveData<PagingData<FeedContentsData>> get() = _pagedList
    private val queryMap: MyPageUploadFeedQueryMap by lazy {
        MyPageUploadFeedQueryMap().apply {
            type = FeedSubDataType.P
        }
    }

    private val _startFeedDetailEvent: SingleLiveEvent<Triple<Int, String, PagingData<FeedContentsData>>> by lazy { SingleLiveEvent() }
    val startFeedDetailEvent: LiveData<Triple<Int, String, PagingData<FeedContentsData>>> get() = _startFeedDetailEvent

    fun start() {
        if (isInitViewCreated()) return

        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        val memberCode = intentModel?.memberCode
        if (!memberCode.isNullOrEmpty()) {
            if (loginManager.getUserLoginData().memCd != memberCode) {
                queryMap.memberCode = memberCode
            }
        }

        reqList()
    }

    fun initMyPageRefresh() {
        RxBus.listen(RxBusEvent.MyPageUploadRefreshEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(it.type == VideoUploadPageType.ALBUM){
                    queryMap.pageNo = 1
                    reqList()
                }
            }.addTo(compositeDisposable)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun reqList(){
        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        val isLoadingShow = intentModel?.isShowLoading ?: true

        repository.fetchMyPageUploads(queryMap,this, isLoadingShow, _isRefresh.value)
            .toFlowable(BackpressureStrategy.BUFFER)
            .cachedIn(viewModelScope)
            .compose(applyApiScheduler())
            .request(
                next = {
                    _pagedList.value = it
                    setInitViewCreated()
                    setLoadingDismiss()
                },
                complete = { setLoadingDismiss() },
                error = { setLoadingDismiss() }
            )
    }

    /**
     * 특정 페이지에서 로딩바를 보여주지 않아야 하는 조건이 있어서 값에 따라 처리하는 함수
     */
    private fun handleLoadShow() {
        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        val isLoadingShow = intentModel?.isShowLoading ?: true
        DLogger.d("startLoadingDialog? ${startLoadingDialog.value}")
        if (startLoadingDialog.value == LoadingDialogState.SHOW) return

        if (isLoadingShow) {
            onLoadingShow()
        }
    }

    fun onMoveToFeed(pos: Int, data: FeedContentsData) {
        DLogger.d("DetailData Pos $pos $data")
        val pagingData = pagedList.value ?: return
        _startFeedDetailEvent.value = Triple(pos, data.feedMngCd, pagingData)
    }

    override fun onClick(which: Int) {
        var isFilterClick = true
        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        val isLoadingShow = intentModel?.isShowLoading ?: true

        DLogger.d("onSortListener $which")
        val changeSort = if (which == 1) SortEnum.DESC else SortEnum.ASC
        if (queryMap.sort != changeSort) {
            setIsRefresh(true)
            queryMap.sort = changeSort
            queryMap.pageNo = 1
            reqList()
        }
    }

    fun getCurrentSort(): SortEnum = queryMap.sort

    private fun setIsRefresh(state: Boolean) {
        _isRefresh.value = state
    }

    private fun setLoadingDismiss() {
        setIsRefresh(false)
        onLoadingDismiss()
    }

}
