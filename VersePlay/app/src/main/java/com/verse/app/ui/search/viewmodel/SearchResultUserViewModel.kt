package com.verse.app.ui.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.R
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.contants.ExtraCode
import com.verse.app.livedata.ListLiveData
import com.verse.app.model.base.BaseModel
import com.verse.app.model.enums.SearchType
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.param.SearchQueryMap
import com.verse.app.model.search.SearchResultList
import com.verse.app.model.search.SearchResultUserData
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 검색 결과 > [사용자] ViewModel
 *
 * Created by juhongmin on 2023/05/10
 */
@HiltViewModel
class SearchResultUserViewModel @Inject constructor(
    private val apiService: ApiService,
    private val resProvider: ResourceProvider,
    val loginManager: LoginManager,
    private val repository: Repository
) : FragmentViewModel() {

    private val _pagedList: MutableLiveData<PagingData<SearchResultUserData>> by lazy { MutableLiveData() }
    val pagedList: LiveData<PagingData<SearchResultUserData>> get() = _pagedList

    private val _uiList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val uiList: ListLiveData<BaseModel> get() = _uiList

    private val _isEmpty: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    // [s] Parameters
    private val queryMap: SearchQueryMap by lazy { SearchQueryMap() }
    val pagingModel: PagingModel by lazy { PagingModel() }
    private val sortCacheEntity: SortCacheEntity by lazy { initSortEntity() }
    // [e] Parameters

    fun start() {
        if (isInitViewCreated()) return

        queryMap.sort = sortCacheEntity.selectSort
        queryMap.keyword = savedStateHandle.get<String>(ExtraCode.SEARCH_KEYWORD) ?: ""
        queryMap.type = SearchType.USER
        queryMap.pageNo = 1
        reqList()
    }

    /**
     * 정렬 Dialog 및 현재 선택한 정렬 값 리턴 하는 함수
     * @see SortDialogType
     * @see SortCacheEntity
     *
     * @return 사용자 정렬, 정렬 데이터
     */
    fun getSortInfo(): Pair<SortDialogType, SortCacheEntity> {
        return SortDialogType.USER_SEARCH to sortCacheEntity
    }

    fun setSort(cacheEntity: SortCacheEntity) {
        sortCacheEntity.setSelectSort(cacheEntity.selectSortTxt, cacheEntity.selectSort)
        queryMap.sort = cacheEntity.selectSort
        queryMap.pageNo = 1
        reqList()
    }

    private fun getUserList(result: SearchResultList): List<BaseModel> {
        return result.userList
    }

    private fun handleOnSuccess(list: List<BaseModel>) {
        if (queryMap.pageNo == 1) {
            _isEmpty.value = list.isEmpty()
            _uiList.clear()
            setInitViewCreated()
        }
        _uiList.addAll(list)
        queryMap.pageNo++
        pagingModel.isLoading = false
        pagingModel.isLast = list.isEmpty()

    }

    private fun handleOnError(err: Throwable) {
        if (queryMap.pageNo == 1) {
            _isEmpty.value = true
        }
        DLogger.d("ERROR $err")
    }

    fun onLoadPage() {
        reqList()
    }

    private fun reqList() {
        _pagedList.value = null
        repository.fetchUserSearch(queryMap, this)
            .cachedIn(viewModelScope)
            .request(next = {
                _pagedList.value = it
                setInitViewCreated()
                onLoadingDismiss()
            }, complete = { onLoadingDismiss() }, error = {
                DLogger.d("ERROR $it")
                onLoadingDismiss()
            })

        /* apiService.requestSearch(queryMap)
             .doOnSubscribe {
                 pagingModel.isLoading = true
                 if (queryMap.pageNo == 1) {
                     onLoadingShow()
                 }
             }
             .map { getUserList(it.result.searchResult) }
             .doLoading()
             .applyApiScheduler()
             .request(success = { handleOnSuccess(it) }, failure = { handleOnError(it) })*/
    }

    private fun initSortEntity(): SortCacheEntity {
        return SortCacheEntity().apply {
            setSelectSort(
                resProvider.getString(R.string.search_filter_order_by_contents),
                SortEnum.USER_CONTENTS
            )
            setDefSelectSort(
                resProvider.getString(R.string.search_filter_order_by_contents),
                SortEnum.USER_CONTENTS
            )
        }
    }

    /**
     * 목록에 본인 있으면 팔로우 버튼 gone
     */
    fun isMine(data: SearchResultUserData): Boolean {
        return data.memCd == loginManager.getUserLoginData().memCd
    }

}
