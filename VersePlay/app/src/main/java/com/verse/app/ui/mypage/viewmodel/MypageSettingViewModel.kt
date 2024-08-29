package com.verse.app.ui.mypage.viewmodel

import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.mypage.activity.*
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.provider.SingPathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MypageSettingViewModel @Inject constructor(
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
    val apiService: ApiService,
    val singPathProvider: SingPathProvider,
    val resourceProvider: ResourceProvider

    ) : ActivityViewModel() {
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기

    val setting_security: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }               // 보안
    val setting_email: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                  // 이메일 수신설정
    val setting_service_lan_country: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }    // 서비스 지역 언어 설정
    val setting_faq: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                    // faq
    val user_guide: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                     // 이용 가이드
    val open_source_license: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }            // 오픈 소스 라이선스
    val isEmailYn: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }                // 이메일 여부
    var EmailYn = ""

    val logout: MutableLiveData<String> by lazy { MutableLiveData() }                   // 로그아웃

    val startMain = SingleLiveEvent<Unit>()
    val startOneButtonPopup: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }          // popup
    val startTwoButtonPopup: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    // 오픈소스타이틀
    private val _openSourceLicenseTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val openSourceLicenseTitle: MutableLiveData<String> get() = _openSourceLicenseTitle

    // 오픈소스내용
    private val _openSourceLicenseContents: MutableLiveData<String> by lazy { MutableLiveData() }
    val openSourceLicenseContents: MutableLiveData<String> get() = _openSourceLicenseContents

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    fun goSettingDetail(int: Int){
        when(int){
            /**
             * 개인정보 변경
             */
            1 -> {
                val page = ActivityResult(
                    targetActivity = SettingMyInfoActivity::class,)
                moveToPage(page)
            }

            /**
             * 친구 관리(차단한 계정)
             */
            2 ->{
                val page = ActivityResult(
                    targetActivity = ManageMyFollowerActivity::class,)
                moveToPage(page)
            }

            /**
             * 친구초대 리워드
             */
            3 ->{
                val page = ActivityResult(
                    targetActivity = InviteFriendActivity::class,)
                moveToPage(page)
            }

            /**
             * 멤버십 관리
             */
            4 ->{
                val page = ActivityResult(
                    targetActivity = MemberShipActivity::class,)
                moveToPage(page)
            }

            /**
             * 보안
             */
            5 ->{
                setting_security.call()
            }

            /**
             * 푸시 알림 설정
             */
            6 ->{
                val page = ActivityResult(
                    targetActivity = SettingPushNotiActivity::class,)
                moveToPage(page)
            }

            /**
             * 서비스 지역 언어 설정
             */
            7 ->{
                setting_service_lan_country.call()
            }

            /**
             * 재생 설정
             */
            8 ->{
                val page = ActivityResult(
                    targetActivity = SettingPlayerActivity::class,)
                moveToPage(page)
            }

            /**
             * faq
             */
            9 ->{
                setting_faq.call()
            }

            /**
             * 1:1 문의 (의견보내기)
             */
            10 ->{
                val page = ActivityResult(
                    targetActivity = QNAActivity::class,)
                moveToPage(page)
            }

            /**
             * 이용 가이드
             */
            11 ->{
                user_guide.call()
            }

            /**
             * 공지사항
             */
            12 ->{
                val page = ActivityResult(
                    targetActivity = NoticeActivity::class,)
                moveToPage(page)
            }

            /**
             * 약관 및 정책
             */
            13 ->{
                val page = ActivityResult(
                    targetActivity = TermsActivity::class,)
                moveToPage(page)
            }

            /**
             * 오픈 소스 라이선스
             */
            14 ->{
                // 오픈소스 라이선스일경우 api 호출
                open_source_license.call()
            }

            /**
             * 쿠폰 번호 입력
             */
            15 ->{
                val page = ActivityResult(
                    targetActivity = SettingCouponActivity::class,)
                moveToPage(page)
            }

        }
    }

    /**
     * 이메일 수신설정 선택
     */
    fun goSettingEmail(v: AppCompatImageView) {
        v.isSelected = !v.isSelected

        EmailYn = if (v.isSelected) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        apiService.updateSettings(
            UploadSettingBody(
                alRecEmailYn = EmailYn
            ))
            .applyApiScheduler()
            .doLoading()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    isEmailYn.value = EmailYn.equals(AppData.Y_VALUE)
                    // preference 변경
                    setting_email.call()
                }
            }, {
                DLogger.d("Error 비공개 => ${it.message}")
            })
    }

    /**
     * 로그아웃 선택
     */
    fun logOut() {
        logout.postValue(accountPref.getAuthTypeCd())
    }

    /**
     * 캐시 삭제 선택 시 팝업 노출
     */
    fun cachePopup(){
        startTwoButtonPopup.call()
    }

    /**
     * ? 선택 시 팝업 노출
     */
    fun cacheoneButtonPopup(){
        startOneButtonPopup.call()
    }

}