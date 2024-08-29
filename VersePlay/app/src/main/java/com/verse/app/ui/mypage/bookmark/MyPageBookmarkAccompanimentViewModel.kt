package com.verse.app.ui.mypage.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.FeedSubDataType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.feed.CommonAccompanimentData
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.param.MyPageAccompanimentQueryMap
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * Description : 마이페이지 > 즐겨찾기 > [반주음]
 *
 * Created by juhongmin on 2023/05/30
 */
@HiltViewModel
class MyPageBookmarkAccompanimentViewModel @Inject constructor(
    private val loginManager: LoginManager,
    private val repository: Repository,
    private val apiService: ApiService
) : FragmentViewModel(),
    FilterDialogFragment.Listener {

    private val _isRefresh: MutableLiveData<Boolean> by lazy { MutableLiveData() }               // 필터 검색
    val isRefresh: LiveData<Boolean> get() = _isRefresh


    private val _pagedList: MutableLiveData<PagingData<CommonAccompanimentData>> by lazy { MutableLiveData() }
    val pagedList: LiveData<PagingData<CommonAccompanimentData>> get() = _pagedList
    private val queryMap: MyPageAccompanimentQueryMap by lazy {
        MyPageAccompanimentQueryMap().apply {
            type = FeedSubDataType.S
        }
    }

    var memberCode: String? = null

    fun start() {
        if (isInitViewCreated()) return

        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        memberCode = intentModel?.memberCode
        if (!memberCode.isNullOrEmpty()) {
            if (loginManager.getUserLoginData().memCd != memberCode) {
                queryMap.memberCode = memberCode
            }
        }

        reqList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun reqList(){
        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        val isLoadingShow = intentModel?.isShowLoading ?: true

        repository.fetchMyPageAccompaniment(queryMap, this, isLoadingShow, _isRefresh.value)
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
        if (isLoadingShow) {
            onLoadingShow()
        }
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
