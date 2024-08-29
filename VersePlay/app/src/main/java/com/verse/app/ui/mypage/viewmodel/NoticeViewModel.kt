package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.SortType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.NoticeData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    val apiService: ApiService,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,
    val repository: Repository,
) : ActivityViewModel(){

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                             // 뒤로가기

    private val _noticeData: MutableLiveData<PagingData<NoticeData>> by lazy { MutableLiveData() }
    val noticeData: LiveData<PagingData<NoticeData>> get() = _noticeData

    private val _noticeSubData: ListLiveData<NoticeData> by lazy { ListLiveData() }
    val noticeSubData: ListLiveData<NoticeData> get() = _noticeSubData

    // 세부공지사항 컨텐츠
    private val _subContents: MutableLiveData<String> by lazy { MutableLiveData() }
    val subContents: MutableLiveData<String> get() = _subContents

    var subNotiCodee =""

    private val _subTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val subTitle: MutableLiveData<String> get() = _subTitle

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * API - 공지사항 목록
     */
    fun requestNotiList() {
        repository.fetchNoticeList(SortType.DESC.name,this)
            .cachedIn(viewModelScope)
            .applyApiScheduler()
            .request({ res ->
                _noticeData.value = res
            })
    }

    fun onMoveToSubNoti(data: NoticeData) {
        _subTitle.value = data.notTitle
        requestDetailNoti(data.notMngCd)
    }

    /**
     * 공지사항 세부 화면
     */
    fun requestDetailNoti(subNotiCodee: String) {
        apiService.getDetailNoticeInfo(
            notCode = subNotiCodee)
            .doLoading()
            .applyApiScheduler()
            .request ({
                res ->
            DLogger.d("Success Noti Detail-> ${res}")
            _subContents.value = res.result.notContent
        }, {
            DLogger.d("Error Report-> ${it.message}")
        })
    }
}