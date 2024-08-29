package com.verse.app.ui.community.event

import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.model.base.BaseModel
import com.verse.app.model.empty.EmptyData
import com.verse.app.model.param.EventQueryMap
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * Description : 커뮤니티 > 이벤트 > [진행]
 *
 * Created by juhongmin on 2023/05/17
 */
@HiltViewModel
class CommunityEventIngFragmentViewModel @Inject constructor(
    private val apiService: ApiService
) : FragmentViewModel() {

    private val _dataList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val dataList: ListLiveData<BaseModel> get() = _dataList

    // [s] Parameter
    val pagingModel: PagingModel by lazy { PagingModel() }
    private val queryMap: EventQueryMap by lazy { EventQueryMap() }
    // [e] Parameter

    fun start() {
        queryMap.isExpired = false
        apiService.fetchEvents(queryMap)
            .doLoading()
            .map { it.result.list }
            .applyApiScheduler()
            .request(success = { handleOnSuccess(it) }, failure = { handleOnError(it) })
    }

    private fun handleOnSuccess(list: List<BaseModel>) {
        if (queryMap.pageNo == 1) {
            if (list.isEmpty()) {
                _dataList.add(EmptyData())
            }
        }
        _dataList.addAll(list)
        queryMap.pageNo++
        pagingModel.isLoading = false
        pagingModel.isLast = list.isEmpty()
    }

    private fun handleOnError(err: Throwable) {
        if (queryMap.pageNo == 1) {
            _dataList.add(EmptyData())
        }
        DLogger.d("ERROR $err")
    }

    fun onLoadPage() {
        reqList(queryMap)
    }

    /**
     * 반주음 API 요청하는 함수
     */
    private fun reqList(queryMap: EventQueryMap) {
        apiService.fetchEvents(queryMap)
            .doOnSubscribe { pagingModel.isLoading = true }
            .map { it.result.list }
            .applyApiScheduler()
            .doOnSuccess { handleOnSuccess(it) }
            .subscribe().addTo(compositeDisposable)
    }
}