package com.verse.app.ui.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.verse.app.R
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.enums.SearchType
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.param.SearchQueryMap
import com.verse.app.model.search.SearchResultList
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 검색 결과 > [인기] ViewModel
 *
 * Created by juhongmin on 2023/05/10
 */
@HiltViewModel
class SearchResultPopularViewModel @Inject constructor(
    private val apiService: ApiService,
    private val resProvider: ResourceProvider
) : FragmentViewModel() {

    private val _songList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val songList: ListLiveData<BaseModel> get() = _songList

    private val _relateFeedList: MutableLiveData<PagingData<FeedContentsData>> by lazy { MutableLiveData() }
    val relateFeedList: LiveData<PagingData<FeedContentsData>> get() = _relateFeedList

    private val _isAllEmpty: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val isAllEmpty: LiveData<Boolean> get() = _isAllEmpty
    private val _isContentsShowEvent: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val isContentsShowEvent: LiveData<Boolean> get() = _isContentsShowEvent
    private val _startMoveTabEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startMoveTabEvent: LiveData<Int> get() = _startMoveTabEvent

    // [s] Parameter
    val queryMap: SearchQueryMap by lazy { SearchQueryMap() }
    private val sortCacheEntity: SortCacheEntity by lazy { initSortEntity() }
    // [e] Parameter

    private val _startFeedDetailEvent: SingleLiveEvent<Triple<Int,String, PagingData<FeedContentsData>>> by lazy { SingleLiveEvent() }
    val startFeedDetailEvent: LiveData<Triple<Int, String, PagingData<FeedContentsData>>> get() = _startFeedDetailEvent

    fun start() {
        if (isInitViewCreated()) return

        _isContentsShowEvent.value = false
        val keyword = savedStateHandle.get<String>(ExtraCode.SEARCH_KEYWORD) ?: ""
        queryMap.type = SearchType.POPULAR
        queryMap.keyword = keyword
        queryMap.pageNo = 1
        reqSearch()
    }

    private fun reqSearch() {
        apiService.requestSearch(queryMap)
            .doLoading()
            .map { getSongAndRelateList(it.result.searchResult) }
            .applyApiScheduler()
            .request(success = {
                handleOnSuccess(it.first, it.second)
            }, failure = { handleOnError(it) })
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
        queryMap.pageNo = 1
        reqSearch()
    }

    private fun getSongAndRelateList(result: SearchResultList): Pair<List<BaseModel>, List<FeedContentsData>> {
        val songList = result.popSongList
        val relateFeedList = result.relateFeedLIst
        return songList to relateFeedList
    }

    private fun handleOnSuccess(
        songList: List<BaseModel>,
        relatedFeedList: List<FeedContentsData>
    ) {
        if (isContentsShowEvent.value == false) {
            _isContentsShowEvent.value = true
        }
        _songList.clear()
        _songList.addAll(songList)
        _relateFeedList.value = PagingData.from(relatedFeedList)
        _isAllEmpty.value = songList.isEmpty() && relatedFeedList.isEmpty()
        setInitViewCreated()
    }

    private fun handleOnError(err: Throwable) {
        if (isContentsShowEvent.value == false) {
            _isContentsShowEvent.value = true
        }
        DLogger.d("ERROR $err")
    }


    fun onMoveMrTab() {
        _startMoveTabEvent.value = 2
    }

    fun onMoveVideoTab() {
        _startMoveTabEvent.value = 1
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
        val pagingData = relateFeedList.value ?: return
        _startFeedDetailEvent.value = Triple(pos,data.feedMngCd, pagingData)
    }
}