package com.verse.app.ui.login.viewmodel

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.EtcTermsType
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetDetailTermsInfoResponseData
import com.verse.app.model.user.CheckRegData
import com.verse.app.model.user.RequestTokenLoginBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    val accountPref: AccountPref,
    private val resource: ResourceProvider,
    val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,
) : ActivityViewModel() {

    val startMain = SingleLiveEvent<Unit>()
    val startMoveNickname = SingleLiveEvent<Unit>()
    val startSendFeedback = SingleLiveEvent<Unit>()
    val memStCd = SingleLiveEvent<String>()

    val startLoadEtcPage: SingleLiveEvent<GetDetailTermsInfoResponseData> by lazy { SingleLiveEvent() }

    val showOneButtonDialogPopup = SingleLiveEvent<String>()

    var checkRegData = CheckRegData()

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 뒤로가기

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * main 이동
     */
    fun startMain() {
        startMain.call()
    }

    fun requestTokenLogin(provider: LoginManager.LoginType, access_token: String) {

        val versionName = deviceProvider.getVersionName()
        val deviceName = deviceProvider.getDeviceName()
        val androidVersion = deviceProvider.getAndroidVersion()
        val macAddress = deviceProvider.getMacAddress()
        val pushToken = accountPref.getFcmPushToken()

        val paramBody = RequestTokenLoginBody(
            authType = provider.code,
            appVersion = versionName,
            deviceModel = deviceName,
            osVersion = androidVersion,
            conIp = macAddress,
            pushKey = pushToken,
            authToken = access_token)

        apiService.requestTokenLogin(paramBody)
            .doLoading()
            .applyApiScheduler()
            .request({ response ->
                if (response.httpStatus == HttpStatusType.SUCCESS.code) {

                    DLogger.d(" jwtToken : ${accountPref.getJWTToken()}")
                    DLogger.d(" authTpCd : ${accountPref.getAuthTypeCd()}")
                    DLogger.d(" pushToken : ${accountPref.getFcmPushToken()}")

                    // userStatus : US000 : 신규 / US001 : 정상 / US002 : 정지 / US003 : 휴면 / US004 : 탈퇴
                    loginManager.setUserLoginData(response.result)
                    memStCd.value = response.result.memStCd
                } else {
                    showOneButtonDialogPopup.value = response.message
                }
            }, { error ->
                DLogger.d("requestTokenLogin Error : " + error.message)
            })
    }

    /**
     * 정책 페이시 상세 정보 조회
     */
    fun requestDetailTerms(termsTpCd: EtcTermsType) {
        apiService.getDetailTermsInfo(
            ctgCode = "",
            termsTpCd = termsTpCd.code
        )
            .doLoading()
            .applyApiScheduler()
            .request ({
                    res ->
                DLogger.d("Success Terms Detail-> ${res}")
                if (res.status == HttpStatusType.SUCCESS.status) {
                    startLoadEtcPage.value = res.result
                }
            }, {
                DLogger.d("Error requestDetailTerms Report-> ${it.message}")
            })
    }
}