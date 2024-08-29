package com.verse.app.ui.mypage.viewmodel

import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingMyInfoDetailViewModel @Inject constructor(
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    private val resProvider: ResourceProvider,
    val apiService: ApiService,
    private val loginManager: LoginManager,
    ) : ActivityViewModel() {

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기
    val onComplete: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                     // 저장
    val onDelete: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 삭제버튼 클릭
    val onEmptyViewClick: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }               // view 클릭

    val editEmailText: NonNullLiveData<String> by lazy { NonNullLiveData("") }      // 이메일
    val editBioText: NonNullLiveData<String> by lazy { NonNullLiveData("") }        // 상메
    val editLinkText: NonNullLiveData<String> by lazy { NonNullLiveData("") }       // 프로필링크
    val startOneButtonPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }          // popup

    val startCheckProhibit: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }          // popup

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * 데이터 저장
     */
    fun onComplete(){
        // 사진 변경점이 있을 경우만 저장 버튼 활성화하도록 추후 변경 예정
        onComplete.call()
    }

    fun delete(){
        onDelete.call()
    }

    fun checkEmail(){
        if (!AppData.IS_MYPAGE_EDIT) return

        apiService.updateSettings(
            UploadSettingBody(
                memEmail = editEmailText.value.toString()
            )
        )
            .applyApiScheduler()
            .doLoading()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    reqRefreshMyProfile()
                } else if (it.status == HttpStatusType.FAIL.status){
                    showToastIntMsg.value = R.string.change_status_fail
                }
            }, {
                DLogger.d("Error 이메일 => ${it.message}")
            })
    }

    fun checkLinks(){
        if (!AppData.IS_MYPAGE_EDIT) return

        if (editLinkText.value.toString() != " " && !editLinkText.value.toString().startsWith("http://") && !editLinkText.value.toString().startsWith("https://")) {
            editLinkText.value = "http://" + editLinkText.value
        }

        apiService.updateSettings(
            UploadSettingBody(
                outLinkUrl = editLinkText.value.toString()
            ))
            .applyApiScheduler()
            .doLoading()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    reqRefreshMyProfile()
                } else if (it.status == HttpStatusType.FAIL.status){
                    showToastIntMsg.value = R.string.change_status_fail
                }
            }, {
                DLogger.d("Error 링크 => ${it.message}")
            })
    }

    fun checkBio(){
        if (!AppData.IS_MYPAGE_EDIT) return

        if (editBioText.value.isEmpty()){
            editBioText.value = " "
        }
        apiService.updateSettings(
            UploadSettingBody(
                instDesc = editBioText.value.toString()
            ))
            .applyApiScheduler()
            .doLoading()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    if (it.fgProhibitYn == AppData.Y_VALUE) {
                        startCheckProhibit.call()
                    } else {
                        reqRefreshMyProfile()
                    }
                } else if (it.status == HttpStatusType.FAIL.status){
                    showToastIntMsg.value = R.string.change_status_fail
                }
            }, {
                DLogger.d("Error 상메 => ${it.message}")
            })
    }

    /**
     * 마이페이지에서 갱신 처리하기 위해 api 호출
     */
    fun reqRefreshMyProfile() {
        apiService.fetchMyProfileInfo()
            .map { it.result }
            .applyApiScheduler()
            .request(success = {
                RxBus.publish(RxBusEvent.MyPageRefreshEvent(it))
                startOneButtonPopup.call()
            })
    }

    fun emptyViewClick(){
        onEmptyViewClick.call()
    }
}