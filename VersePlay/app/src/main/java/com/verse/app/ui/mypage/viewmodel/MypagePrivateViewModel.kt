package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.FeedSubDataType
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.SortType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.PrivateFeedData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.exo.ExoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class MypagePrivateViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val exoManager: ExoManager,
    val loginManager: LoginManager
) : ActivityViewModel() {

    private val _startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinish: LiveData<Unit> get() = _startFinish

    val vpPageState: MutableLiveData<Int> by lazy { MutableLiveData() }                             // ViewPager 상태
    val vpPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }                  // ViewPager 현재 position

    private val _privateFeedSortType: MutableLiveData<SortType> by lazy { MutableLiveData(SortType.NONE) }
    val privateFeedSortType: LiveData<SortType> get() = _privateFeedSortType

    private val _privateCollectionSortType: MutableLiveData<SortType> by lazy { MutableLiveData(SortType.NONE) }
    val privateCollectionSortType: LiveData<SortType> get() = _privateCollectionSortType

    private val _startFilter: MutableLiveData<SortType> by lazy { MutableLiveData() }
    val startFilter: LiveData<SortType> get() = _startFilter

    private val _startFeedDetailEvent: SingleLiveEvent<Triple<Int, String, PagingData<FeedContentsData>>> by lazy { SingleLiveEvent() }
    val startFeedDetailEvent: LiveData<Triple<Int, String, PagingData<FeedContentsData>>> get() = _startFeedDetailEvent

    private val _privateFeedData: ListLiveData<PrivateFeedData> by lazy { ListLiveData() }
    val privateFeedData: ListLiveData<PrivateFeedData> get() = _privateFeedData

    private val _refresh: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val refresh: SingleLiveEvent<Int> get() = _refresh


    fun start() {

        val tmpList = mutableListOf<PrivateFeedData>()

        tmpList.add(PrivateFeedData().apply {
            itemViewType = ListPagedItemType.ITEM_PRIVATE_SONG_BOX
        })

        tmpList.add(PrivateFeedData().apply {
            itemViewType = ListPagedItemType.ITEM_PRIVATE_SONG_BOX
        })

        _privateFeedData.addAll(tmpList)

        fetchMyPagePrivateFeed(SortType.DESC)
        fetchMyPagePrivateCollection(SortType.DESC)
    }

    /**
     * 뒤로 선택
     */
    fun onFinish() {
        _startFinish.call()
    }

    /**
     * 필터 적용
     */
    fun setFilterType(type: SortType) {
        if (vpPosition.value == 0) {
            _privateFeedSortType.value = type
            fetchMyPagePrivateFeed(type)
        } else {
            _privateCollectionSortType.value = type
            fetchMyPagePrivateCollection(type)
        }
    }

    /**
     * 탭 클릭
     */
    fun onTabClick(pos: Int) {
        vpPosition.value = pos
    }

    /**
     * 탭 클릭
     */
    fun onFilterClick() {
        _startFilter.value = if (vpPosition.value == 0) {
            _privateFeedSortType.value
        } else {
            _privateCollectionSortType.value
        }
    }

    /**
     * ViewPager 상태 - Horizontal
     */
    fun privatePageState(state: Int) {
        vpPageState.value = state
    }

    /**
     * 공연 영상 필터 적용
     */
    private fun fetchMyPagePrivateFeed(sortType: SortType) {
        repository.fetchMypagePrivate(FeedSubDataType.C, sortType.name,this)
            .applyApiScheduler()
            .cachedIn(viewModelScope)
            .request({ response ->
                _privateFeedData.value[0].dataList = response
                _refresh.value = 0
            })
    }

    /**
     * 개인 소장 필터 적용
     */
    private fun fetchMyPagePrivateCollection(sortType: SortType) {
        repository.fetchMypagePrivate(FeedSubDataType.P, sortType.name,this)
            .applyApiScheduler()
            .cachedIn(viewModelScope)
            .request({ response ->
                _privateFeedData.value[1].dataList = response
                _refresh.value = 1
            })
    }

    fun onMoveToFeed(pos: Int, data: FeedContentsData) {
        val pagingData = if (vpPosition.value == 0) {
            _privateFeedData.value[0].dataList ?: return
        } else {
            _privateFeedData.value[1].dataList ?: return
        }

        pagingData?.let {
            _startFeedDetailEvent.value = Triple(pos, data.feedMngCd, it)
        }
    }

}