package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.SortType
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetRecentLoginHistoryData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MypageSettingOnlyViewerViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val accountPref: AccountPref,

    ) : ActivityViewModel() {
    
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                             // 뒤로가기
    val showLoginLayout: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }           // 로그인 단말 레이아웃 visible
    val showCountryNLanLayout: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }     // 국가 언어 레이아웃 visible
    // 보안 -> 로그인 기기 검색
    private val _loginDeviceList: MutableLiveData<PagingData<GetRecentLoginHistoryData>> by lazy { MutableLiveData() }
    val loginDeviceList: MutableLiveData<PagingData<GetRecentLoginHistoryData>> get() = _loginDeviceList

    // 국가 언어 설정
    val setting_country: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                 // 국가선택
    val setting_lan: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                 // 언어선택


    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * 국가 선택
     */
    fun goSettingCountry() {
        setting_country.call()
    }

    /**
     * 언어 선택
     */
    fun goSettingLan() {
        setting_lan.call()
    }


    fun showLayout(v: Int) {
        if (v == 1) {
            showLoginLayout.postValue(true)
            requestLoginDeviceData()
        } else if (v == 2) {
            showCountryNLanLayout.postValue(true)
        }
    }

    /**
     * 로그인 기기 리스트 데이터
     */
    private fun requestLoginDeviceData(){
        repository.fetchLoginDevice(SortType.DESC.name,this)
            .cachedIn(viewModelScope)
            .request({ response ->
                _loginDeviceList.value = response
            })
    }

}