package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetTermsListData
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsViewModel  @Inject constructor(
    val apiService: ApiService,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,
    savedStateHandle: SavedStateHandle,
) : ActivityViewModel(){

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                             // 뒤로가기

    // 정책
    private val _termsData: ListLiveData<GetTermsListData> by lazy { ListLiveData() }
    val termsData: ListLiveData<GetTermsListData> get() = _termsData

    // 세부정책 타이틀
    private val _subTerms: MutableLiveData<String> by lazy { MutableLiveData() }
    val subTerms: MutableLiveData<String> get() = _subTerms

    // 세부공지사항 컨텐츠
    private val _subTermsContents: MutableLiveData<String> by lazy { MutableLiveData() }
    val subTermsContents: MutableLiveData<String> get() = _subTermsContents

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * API - 정책 목록
     */
    /**
     * 정책 리스트 데이터
     */
    fun requestTermList(){
        apiService.getTermList()
            .doLoading()
            .applyApiScheduler()
            .request ({
                    res ->
                DLogger.d("Success Report-> ${res}")
                _termsData.value = res.result.dataList
            }, {
                DLogger.d("Error Report-> ${it.message}")
            })
    }

    /**
     * 정책 선택 -> 세부 정책으로 이동
     */
    fun onMoveToSubTerms(data: GetTermsListData) {
        requestDetailTerms(data.bctgMngCd)
    }

    /**
     * 정책 세부 화면
     */
    fun requestDetailTerms(subNotiCodee: String) {
        apiService.getDetailTermsInfo(
            ctgCode = subNotiCodee,
            termsTpCd = "",
        )
            .doLoading()
            .applyApiScheduler()
            .request ({
                res ->
            DLogger.d("Success Terms Detail-> ${res}")
            _subTerms.value = res.result.bctgMngNm
            _subTermsContents.value = res.result.termsContent
        }, {
            DLogger.d("Error requestDetailTerms Report-> ${it.message}")
        })
    }
}