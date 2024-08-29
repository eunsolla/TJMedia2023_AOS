package com.verse.app.ui.login.viewmodel

import com.verse.app.R
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.user.CheckRegNickNameBody
import com.verse.app.model.user.RegisterMemberBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NicknameViewModel @Inject constructor(
    private val apiService: ApiService,
    val accountPref: AccountPref,
    val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,
    private val resProvider: ResourceProvider
) : BaseViewModel() {
    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                // 닫기
    val startVerifyCheckNickName: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startVerifyCheckNickNameSuccess: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val startVerifyCheckNickNameFail: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val startJoinUser: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startHelp: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val memStCd = SingleLiveEvent<String>()

    val autoCreateNickName = SingleLiveEvent<String>()

    var nickName: String = ""
    var checkRegNickName: Boolean = false

    var fgProhibitYn: Boolean = false

    val showOneButtonDialogPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }

    /**
     * 닫기
     */
    fun close() {
        startFinish.call()
    }

    /**
     * 도움말
     */
    fun startHelp() {
        startHelp.call()
    }

    /**
     * (닉네임)증복확인
     */
    fun startVerifyCheckNickName() {
        startVerifyCheckNickName.call()
    }

    /**
     * 회원가입
     */
    fun joinUser() {
        startJoinUser.call()
    }

    /**
     * 닉네임 자동생성
     */
    fun requestCreateNickName() {
        apiService.requestCreateNickName()
            .doLoading()
            .applyApiScheduler()
            .request({ response ->
                DLogger.d("response requestCreateNickName ${response}")

                if (response.status == HttpStatusType.SUCCESS.status) {
                    autoCreateNickName.value = response.result.nickName
                }
            }, { error ->
                DLogger.d("Throwable ${error}")
            })
    }

    fun verifyCheckNickName(nickName: String) {
        if (!isNickNameValidate(nickName)) {
            startVerifyCheckNickNameFail.value =
                resProvider.getString(R.string.str_invalid_register_nick_name)
            return
        }
        this.nickName = nickName
        apiService.checkRegNickName(CheckRegNickNameBody(nickName))
            .doLoading()
            .applyApiScheduler()
            .request({ response ->
                DLogger.d("response checkRegNickName : $response")
                // 기존 로직그대로 가져오면서 조건문 개선
                // 중복확인 api ( checkRegNickName ) 에서 내려오는 fgProhibitYn 이 Y일때,
                // 기존에 api 내려오는 메시지를 노출해주던 부분에 빨간색으로 메시지 노출 되도록 수정해 주시면 될것 같습니다.
                if (response.resultCode == "LG010") {
                    startVerifyCheckNickNameSuccess.value = response.message
                } else if (response.resultCode == "PR001") {
                    showOneButtonDialogPopup.value = response.message
                } else if (response.fgProhibitYn == AppData.Y_VALUE) {
                    startVerifyCheckNickNameFail.value = response.message
                } else {
                    startVerifyCheckNickNameFail.value = response.message
                }

            }, { error ->
                DLogger.d("Throwable ${error}")
            })
    }

    private fun isNickNameValidate(str: String): Boolean {
        // 특수문자, 이모지, 공백 입력 안되게 처리
        return str.matches("^[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힣]*\$".toRegex())
    }

    fun registerMember(nickName: String, email: String) {
        var registerMemberBody = RegisterMemberBody()
        val versionName = deviceProvider.getVersionName()
        val deviceName = deviceProvider.getDeviceName()
        val androidVersion = deviceProvider.getAndroidVersion()
        val macAddress = deviceProvider.getMacAddress()

        registerMemberBody.registerMemberBody(
            nickName.trim(),
            accountPref.getAuthTypeCd(),
            email.trim(),
            versionName,
            deviceName,
            androidVersion,
            macAddress
        )

        DLogger.d(" registerMemberBody nickName: ${registerMemberBody.nickName}")
        DLogger.d(" registerMemberBody authType: ${registerMemberBody.authType}")
        DLogger.d(" registerMemberBody email: ${registerMemberBody.email}")
        DLogger.d(" registerMemberBody conVer: ${registerMemberBody.conVer}")
        DLogger.d(" registerMemberBody conModel: ${registerMemberBody.conModel}")
        DLogger.d(" registerMemberBody conOsVer: ${registerMemberBody.conOsVer}")
        DLogger.d(" registerMemberBody conIp: ${registerMemberBody.conIp}")

        apiService.registerMember(registerMemberBody)
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    loginManager.setUserLoginData(it.result)
                    memStCd.value = it.result.memStCd
                } else {
                    showOneButtonDialogPopup.value = it.message
                }
            }, {
                DLogger.d("registerMember Error")
            })
    }
}
