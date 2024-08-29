package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.contants.SortType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.AlrimListData
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MypageAlartViewModel @Inject constructor(
    val accountPref: AccountPref,
    val repository: Repository,
    val deviceProvider: DeviceProvider,
) : ActivityViewModel() {

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 뒤로가기

    // 알림 데이터
    private val _alrimData: MutableLiveData<PagingData<AlrimListData>> by lazy { MutableLiveData() }
    val alrimData: LiveData<PagingData<AlrimListData>> get() = _alrimData

    val moveToPage: SingleLiveEvent<AlrimListData> by lazy { SingleLiveEvent() }                       // 알림 목록 유형별 이동 처리
    val pagingModel: PagingModel by lazy { PagingModel() }
    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * 알림 목록 리스트 데이터
     */
    fun requestMyAlrimList() {
        repository.fetchMyAlrimList(SortType.DESC.name,this)
            .cachedIn(viewModelScope)
            .doLoading()
            .applyApiScheduler()
            .request(
                { res ->
                _alrimData.value = res
            }, {
                DLogger.d("Error ReportResponse ${it}")
            }
            )
    }

    /**
     * 클릭 시 해당 콘텐츠로 이동
     */
    fun moveToAlrim(position: Int, data: AlrimListData) {
        DLogger.d("moveToAlrim[${position}] : ${data.linkCd}")
        data.let {
            moveToPage.value = data
        }
    }
}