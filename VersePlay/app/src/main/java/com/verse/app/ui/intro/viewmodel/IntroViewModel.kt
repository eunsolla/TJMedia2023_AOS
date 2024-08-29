package com.verse.app.ui.intro.viewmodel

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.handleNetworkErrorRetry
import com.verse.app.extension.isUpdate
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.common.VersionData
import com.verse.app.model.user.RequestTokenLoginBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.provider.SingPathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Intro ViewModel Class
 *
 * Created by jhlee on 2023-01-01
 */
@HiltViewModel
class IntroViewModel @Inject constructor(
    val apiService: ApiService,
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
    val singPathProvider: SingPathProvider
) : ActivityViewModel() {

    val startCountryActivity = SingleLiveEvent<Unit>()
    val startMain = SingleLiveEvent<Unit>()
    val startOptionalUpdateDialog = SingleLiveEvent<VersionData>() // 선택 업데이트
    val startRequireUpdateDialog = SingleLiveEvent<VersionData>() // 강제 업데이트
    val startServiceCheckDialog = SingleLiveEvent<VersionData>() // 서버 점검 팝업
    val memStCd = SingleLiveEvent<String>()
    val startMoveNickname = SingleLiveEvent<Unit>()
    val startSendFeedback = SingleLiveEvent<Unit>()

    /**
     *  API - 버전 체크
     */
    fun startSplash() {
        //버전 체크
        apiService.fetchVersions()
            .compose(handleNetworkErrorRetry())
            .applyApiScheduler()
            .request({ res ->
                DLogger.d("fetchVersions=> ${res}")

                val currVersion = deviceProvider.getVersionName()

                DLogger.d("serverVersion : ${res.result.versionNm} / AppVersion : ${currVersion}")

                // 시스템 점검 여부 확인
                if (res.result.fgCheckServerYn == AppData.Y_VALUE) {
                    startServiceCheckDialog.value = res.result
                } else {
                    if (currVersion.isUpdate(res.result.versionNm)) {
                        if (res.result.updType.equals("S")) {
                            startOptionalUpdateDialog.value = res.result
                        } else {
                            // 최소 버전보다 낮은 경우
                            startRequireUpdateDialog.value = res.result
                        }
                    } else {
                        if (!accountPref.getAuthTypeCd().isEmpty() && !accountPref.getJWTToken().isEmpty()) {
                            val authTypeCd = accountPref.getAuthTypeCd() // sns 타입
                            autoTokenLogin(authTypeCd)
                        } else {
                            if (!accountPref.isIntroSettingPageShow()) {
                                startCountryActivity.call()
                            } else {
                                startMain.call()
                            }
                        }
                    }
                }
            }, { error ->
                DLogger.d("Throwable ${error}")
            })
    }

    //auto login
    fun autoTokenLogin(authTypeCd: String) {

        val versionName = deviceProvider.getVersionName()
        val deviceName = deviceProvider.getDeviceName()
        val androidVersion = deviceProvider.getAndroidVersion()
        val macAddress = deviceProvider.getMacAddress()
        val pushToken = accountPref.getFcmPushToken()

        val paramBody = RequestTokenLoginBody(
            authType = authTypeCd,
            appVersion = versionName,
            deviceModel = deviceName,
            osVersion = androidVersion,
            conIp = macAddress,
            pushKey = pushToken,
            authToken = "")

        apiService.requestTokenLogin(paramBody)
            .applyApiScheduler()
            .request({ response ->
                if (response.httpStatus == HttpStatusType.SUCCESS.code) {

                    DLogger.d(" jwtToken : ${accountPref.getJWTToken()}")
                    DLogger.d(" authTpCd : ${accountPref.getAuthTypeCd()}")
                    DLogger.d(" pushToken : ${accountPref.getFcmPushToken()}")

                    // userStatus : 비회원 : US000 / 정상 : US001 / 휴면 : US002 / 탈퇴 : US003 / 영구정지 : US004
                    // 1일정지 : US005 / 1개월정지 : US006 / 3개월정지 : US007 / 6개월정지 : US008 / 12개월정지 : US009
                    loginManager.setUserLoginData(response.result)
                    memStCd.value = response.result.memStCd
                    UserSettingManager.setPrivateYn(response.result.fgPrivateYn)
                } else {
                    if (response.message.isNullOrEmpty()) {
                        showNetworkErrorDialog(response.message)
                    }
                }
            }, { error ->
                DLogger.d("requestTokenLogin Error : " + error.message)
            })
    }
}