package com.verse.app.ui.intro.viewmodel

import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.handleNetworkErrorRetry
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetNationLanguageListResponseData
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Setting Country Viewmodel Class
 *
 * Created by esna on 2023-03-16
 */

@HiltViewModel
class SetCountryViewModel @Inject constructor(
    val accountPref: AccountPref,
    private val apiService: ApiService,
    val deviceProvider: DeviceProvider,
) : ActivityViewModel() {
    val nationList: MutableLiveData<List<GetNationLanguageListResponseData>> by lazy { MutableLiveData() }
    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                // 닫기
    val startOneButtonPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }      // popup
    val selectedItem: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                     // 저장
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기
    val refreshList: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    var isSetting: Boolean = false

    fun getCountryList() {
        apiService.getNationLanguageList(
            reqType = "N"
        )
            .compose(handleNetworkErrorRetry())
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    if (res.result.dataList.size > 0) {

                        res.result.dataList.forEach {
                            if (accountPref.getPreferenceLocaleCountry() == it.code) {
                                if (isSetting) {
                                    it.isSelected = true
                                    selectedItem.value = it.code
                                }
                            }
                        }

                        nationList.value = res.result.dataList
                    }
                } else {
                    DLogger.d("getNationList fail response-> ${res.message}")
                }
            }, {
                DLogger.d("getNationList error response-> ${it.message}")
            })
    }

    fun selectedNation(position: Int) {
        DLogger.d("selected nation code : ${nationList.value!!.get(position).code}")

        nationList.value?.forEach {
            it.isSelected = false
        }

        nationList.value?.get(position)?.let {
            it.isSelected = true
            selectedItem.value = it.code
        }

        refreshList.call()
    }

    /**
     * 닫기
     */
    fun close() {
        startFinish.call()
    }

    /**
     * 데이터 저장
     */
    fun onSaveNationInfo() {
        val selectedItem = selectedItem.value ?: return
        DLogger.d("onSaveNationInfo $selectedItem")
        apiService.updateSettings(
            UploadSettingBody(
                svcNtCd = selectedItem
            )
        )
            .applyApiScheduler()
            .doLoading()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    // preference 변경
                    DLogger.d("SUCCESS 국가설정 => ${selectedItem}")
                    accountPref.setPreferenceLocaleCountry(selectedItem)
                    startFinish.call()
                }
            }, {
                DLogger.d("Error 국가설정 => ${it.message}")
            })
    }

    /**
     * ? 선택 시 팝업 노출
     */
    fun popup() {
        startOneButtonPopup.call()
    }
}
