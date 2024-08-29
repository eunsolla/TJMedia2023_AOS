package com.verse.app.ui.mypage.viewmodel

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.EtcTermsType
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetDetailTermsInfoResponseData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val apiService: ApiService,
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val resourceProvider: ResourceProvider,
    val fileProvider: FileProvider,
    val loginManager: LoginManager,
    ) : ActivityViewModel() {
    val secession: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 회원 탈퇴
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기
    val showTwoButtonDialogPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val showOneButtonDialogPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startLoadEtcPage: SingleLiveEvent<GetDetailTermsInfoResponseData> by lazy { SingleLiveEvent() }

    fun back() {
        backpress.call()
    }

    /**
     * 탈퇴 api
     */
    fun onSession() {
        showTwoButtonDialogPopup.call()
    }

    fun setSession(){
        apiService.requestWithdrawService()
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    showOneButtonDialogPopup.call()
                }
            }, {
                DLogger.d("Error requestWithdrawService=> ${it.message}")
            })
    }

    /**
     * 정책 페이시 상세 정보 조회
     */
    fun requestSession() {
        apiService.getDetailTermsInfo(
            ctgCode = "",
            termsTpCd = EtcTermsType.WITHDRAW.code
        )
            .doLoading()
            .applyApiScheduler()
            .request ({
                    res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    startLoadEtcPage.value = res.result
                }
            }, {

            })
    }
}