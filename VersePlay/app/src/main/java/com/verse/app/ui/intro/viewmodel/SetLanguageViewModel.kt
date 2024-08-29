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
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * Description : Setting Lan Viewmodel Class
 *
 * Created by esna on 2023-03-16
 */
@HiltViewModel
class SetLanguageViewModel @Inject constructor(
    private val apiService: ApiService,
    val repository: Repository,
    private val resProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val accountPref: AccountPref,
    ) : ActivityViewModel() {

    val languageList: MutableLiveData<List<GetNationLanguageListResponseData>> by lazy { MutableLiveData() }
    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                // 닫기
    val startOneButtonPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }      // popup
    val selectedItem: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                     // 저장
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기
    val refreshList: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    var isSetting: Boolean = false
    val changeLanguage: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    var originLanguage: String? = null

    fun getLangList() {
        apiService.getNationLanguageList(
            reqType = "L"
        )
            .compose(handleNetworkErrorRetry())
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    if (res.result.dataList.size > 0) {

                        res.result.dataList.forEach {
                            if (accountPref.getPreferenceLocaleLanguage() == it.code) {
                                if (isSetting) {
                                    it.isSelected = true
                                    selectedItem.value = it.code
                                }
                            }
                        }

                        languageList.value = res.result.dataList
                    }
                } else {
                    DLogger.d("getLanguageList fail response-> ${res.message}")
                }
            }, {
                DLogger.d("getLanguageList error response-> ${it.message}")
            })
    }

    fun selectedLanguage(position: Int) {
        DLogger.d("selected language code : ${languageList.value!!.get(position).code}")

        languageList.value?.forEach {
            it.isSelected = false
        }

        languageList.value?.get(position)?.let {
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
    fun onSaveLanguageInfo() {
        selectedItem.value?.let {
            apiService.updateSettings(
                UploadSettingBody(
                    svcLangCd = it
                )
            )
                .applyApiScheduler()
                .doLoading()
                .request({
                    if (it.status == HttpStatusType.SUCCESS.status) {
                        // preference 변경
                        DLogger.d("SUCCESS 언어설정 => ${selectedItem.value!!}")
                        if (selectedItem.value != null) {
                            accountPref.setPreferenceLocaleLanguage(selectedItem.value!!)
                            changeLanguage.call()
                        }
                    }
                }, {
                    DLogger.d("Error 언어설정 => ${it.message}")
                })
        }
    }

    /**
     * ? 선택 시 팝업 노출
     */
    fun popup() {
        startOneButtonPopup.call()
    }
}
