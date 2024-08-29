package com.verse.app.ui.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.R
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.param.SearchQueryMap
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * Description : 검색 결과 > [동영상] ViewModel
 *
 * Created by juhongmin on 2023/05/10
 */
@HiltViewModel
class SearchResultVideoViewModel @Inject constructor(
    private val repository: Repository,
    private val resProvider: ResourceProvider
) : FragmentViewModel() {

    private val _pagedList: MutableLiveData<PagingData<FeedContentsData>> by lazy { MutableLiveData() }
    val pagedList: LiveData<PagingData<FeedContentsData>> get() = _pagedList

    // [s] Parameters
    private val sortCacheEntity: SortCacheEntity by lazy { initSortEntity() }
    private val queryMap: SearchQueryMap by lazy { SearchQueryMap() }
    // [e] Parameters

    private val _startFeedDetailEvent: SingleLiveEvent<Triple<Int,String, PagingData<FeedContentsData>>> by lazy { SingleLiveEvent() }
    val startFeedDetailEvent: LiveData<Triple<Int, String, PagingData<FeedContentsData>>> get() = _startFeedDetailEvent

    fun start() {
        if (isInitViewCreated()) return

        val keyword = savedStateHandle.get<String>(ExtraCode.SEARCH_KEYWORD) ?: ""
        queryMap.keyword = keyword
        reqList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun reqList() {
        _pagedList.value = null
        repository.fetchFeedSearch(queryMap, this)
            .toFlowable(BackpressureStrategy.BUFFER)
            .cachedIn(viewModelScope)
            .compose(applyApiScheduler())
            .request(next = {
                _pagedList.value = it
                setInitViewCreated()
                onLoadingDismiss()
            }, complete = { onLoadingDismiss() }, error = {
                DLogger.d("ERROR $it")
                onLoadingDismiss()
            })
    }

    /**
     * 정렬 Dialog 및 현재 선택한 정렬 값 리턴 하는 함수
     * @see SortDialogType
     * @see SortCacheEntity
     *
     * @return 사용자 정렬, 정렬 데이터
     */
    fun getSortInfo(): Pair<SortDialogType, SortCacheEntity> {
        return SortDialogType.DEFAULT to sortCacheEntity
    }

    fun setSort(cacheEntity: SortCacheEntity) {
        sortCacheEntity.setSelectSort(cacheEntity.selectSortTxt, cacheEntity.selectSort)
        queryMap.sort = cacheEntity.selectSort
        reqList()
    }

    private fun initSortEntity(): SortCacheEntity {
        return SortCacheEntity().apply {
            setSelectSort(
                resProvider.getString(R.string.search_filter_order_by_latest),
                SortEnum.DESC
            )
            setDefSelectSort(
                resProvider.getString(R.string.search_filter_order_by_latest),
                SortEnum.DESC
            )
        }
    }

    fun onMoveToFeed(pos: Int, data: FeedContentsData) {
        DLogger.d("moveToFeedDetail $pos $data")
        val pagingData = pagedList.value ?: return
        _startFeedDetailEvent.value = Triple(pos,data.feedMngCd, pagingData)
    }
}
