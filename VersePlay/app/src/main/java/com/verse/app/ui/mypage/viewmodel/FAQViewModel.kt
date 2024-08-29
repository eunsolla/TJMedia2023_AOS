package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetFaqCategoryListData
import com.verse.app.model.mypage.GetFaqCategoryListLastData
import com.verse.app.model.mypage.GetFaqCategoryListSubData
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
    val apiService: ApiService,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,

    ) : ActivityViewModel() {

    val finish = SingleLiveEvent<Boolean>()
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기

    // faq 리스트 목록
    private val _faqList: ListLiveData<GetFaqCategoryListData> by lazy { ListLiveData() }
    val faqList: ListLiveData<GetFaqCategoryListData> get() = _faqList

    // faq 카테고리 -> 세부 목록
    private val _subFAQList: ListLiveData<GetFaqCategoryListSubData> by lazy { ListLiveData() }
    val subFAQList: ListLiveData<GetFaqCategoryListSubData> get() = _subFAQList

    // faq 카테고리 -> 세부 목록
    private val _lastFAQList: ListLiveData<GetFaqCategoryListLastData> by lazy { ListLiveData() }
    val lastFAQList: ListLiveData<GetFaqCategoryListLastData> get() = _lastFAQList

    private val _subFAQTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val subFAQTitle: MutableLiveData<String> get() = _subFAQTitle

    private val _subLastFAQTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val subLastFAQTitle: MutableLiveData<String> get() = _subLastFAQTitle

    private val _detailFAQTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val detailFAQTitle: MutableLiveData<String> get() = _detailFAQTitle

    private val _subFAQContents: MutableLiveData<String> by lazy { MutableLiveData() }
    val subFAQContents: MutableLiveData<String> get() = _subFAQContents

    // faq 세부 목록 -> 디테일 콘텐츠
    private val _detailFAQContents: ListLiveData<GetFaqCategoryListSubData> by lazy { ListLiveData() }
    val detailFAQContents: ListLiveData<GetFaqCategoryListSubData> get() = _detailFAQContents

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * API - FAQ 목록
     */
    fun requestFAQList() {
        apiService.getFaqCategoryList()
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                _faqList.value = res.result.dataList
            }, {
                DLogger.d("Error getFaqCategoryList ${it}")
            })
    }


    /**
     * FAQ 선택 -> 세부 FAQ 목록 이동
     */
    fun onMoveToSubFAQ(data: GetFaqCategoryListData) {
        _subFAQTitle.value = data.bctgMngNm
        _subFAQList.value = data.subDataList
    }


    /**
     * FAQ 선택 -> sub -> last
     */
    fun onMoveToLastDepthFAQ(data: GetFaqCategoryListSubData) {
        apiService.getFaqCategorySubList(
            bctgMngCd = data.bctgMngCd
        ).applyApiScheduler()
            .doLoading()
            .request({ res ->
                _subLastFAQTitle.value = res.result.bctgMngNm
                _lastFAQList.value = res.result.dataList
                DLogger.d("Success 세부 faq-> ${res}")
            }, {
                DLogger.d("Error Report-> ${it.message}")
            })
    }


    /**
     * 세부 FAQ 선택 -> 디테일 FAQ 화면 이동
     */
    fun onMoveToDetailFAQ(data: GetFaqCategoryListLastData) {
        _detailFAQTitle.value = data.faqTitle
        _subFAQContents.value = data.faqContents
    }

    /**
     * 세부 목록 -> 디테일 faq 이동
     */
    fun requestDetailFAQ(subNotiCodee: String) {
        apiService.getDetailFaqInfo(
            ctgCode = subNotiCodee
        ).applyApiScheduler()
            .doLoading()
            .request({ res ->
                _detailFAQTitle.value = res.result.faqTitle
                _subFAQContents.value = res.result.faqContent
                DLogger.d("Success 세부 faq-> ${res}")
            }, {
                DLogger.d("Error Report-> ${it.message}")
            })
    }
}