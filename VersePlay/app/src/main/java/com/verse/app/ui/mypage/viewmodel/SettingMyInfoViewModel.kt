package com.verse.app.ui.mypage.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.Config
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.ResourcePathType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.gallery.ui.GalleryImageEditBottomSheetDialog
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.common.GetResourcePathInfo
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.mypage.activity.SessionActivity
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingMyInfoViewModel @Inject constructor(
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val apiService: ApiService,
    val resourceProvider: ResourceProvider,
    val fileProvider: FileProvider,
    private val loginManager: LoginManager

) : ActivityViewModel(),
    GalleryBottomSheetDialog.Listener,
    GalleryImageEditBottomSheetDialog.Listener {

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기
    val changeProfile: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                  // 프사 변경
    val changeBackgroundProfile: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }        // 배사 변경
    val changeMyEmail: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                  // 이메일 변경
    val changeMyBio: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                    // 상태메시지 변경
    val changeMyLink: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                   // 외부 링크 변경
    val changePrivateAccount: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }           // 비공개 계정 설정
    val recommendMyProfile: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }             // 내 계정을 다른 사람에게 추천
    val startClickChecked: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    val _profileImg: MutableLiveData<String> by lazy { MutableLiveData() }
    val profileImg: LiveData<String> get() = _profileImg                                              // 프사

    val _bgProfileImg: MutableLiveData<String> by lazy { MutableLiveData() }
    val bgProfileImg: LiveData<String> get() = _bgProfileImg                                          // 배사

    val isChangeProfile: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                     // 사진 바꾸면 저장 버튼 활성화 시키는 변수
    val changeProfileYn: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }                  // 프사 변경시 Y
    val changeBackgroundProfileYn: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }        // 배사 변경시 Y

    val myBio: NonNullLiveData<String> by lazy { NonNullLiveData("") }                              // 상태매세지
    val myEmail: NonNullLiveData<String> by lazy { NonNullLiveData("") }                            // 이메일
    val myLinks: NonNullLiveData<String> by lazy { NonNullLiveData("") }                            // 프로필 링크

    //[s]토글
    val isPrivateAccount: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                // 비공개계정여부
    val isRecommendMyProfile: SingleLiveEvent<String> by lazy { SingleLiveEvent() }            // 내 계정 다른 계정에게 추천여부

    var privateYn: Boolean = false
    var recUserYn: Boolean = false
    //[e]토글

    var clickCheckedType: String = ""
    var clickProfileType: Int = 0

    lateinit var uploadSettingBody: UploadSettingBody

    val startOneButtonPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }      // popup
    val startChangePopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }      // popup
    var fgProhibitYn: Boolean = false

    val startEditFrProfile: SingleLiveEvent<Uri> by lazy { SingleLiveEvent() }      // Edit FR Profile
    val startEditBgProfile: SingleLiveEvent<Uri> by lazy { SingleLiveEvent() }      // Edit BG Profile

    fun back() {
        backpress.call()
    }

    fun clickChangeProfile(tp: Int) {
        when (tp) {
            1 -> {
                changeProfile.call()
            }

            2 -> {
                changeBackgroundProfile.call()
            }
        }
    }

    fun changeMyEmail() {
        changeMyEmail.call()
    }

    fun changeMyBio() {
        changeMyBio.call()
    }

    fun changeMyLink() {
        changeMyLink.call()
    }

    /**
     * 비공개계정 on -> 내계정을 다른사람에게추천 off, 친구만메세지받기 on
     * 내 계정을 다른 사람에게 추천 on-> if(비공개계정이 on인경우) {비공계계정 off로 변경}
     */
    enum class AccountStatus {
        CHANGE_ACCOUNT_PRIVACY, RECOMMEND_MY_PROFILE,
    }

    /**
     * 허용범위 / 노출 설정
     */
    fun clickChecked(tp: Int) {
        when (tp) {
            1 -> {
                clickCheckedType = AccountStatus.CHANGE_ACCOUNT_PRIVACY.name
            }

            2 -> {
                clickCheckedType = AccountStatus.RECOMMEND_MY_PROFILE.name
            }
        }
        startClickChecked.call()
    }

    fun changePrvAccYN(changeP: String) {
        /**
         * 비공개계정여부 api
         */
        apiService.updateSettings(
            UploadSettingBody(
                prvAccYn = changeP
            )
        ).doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    // preference 변경
                    isPrivateAccount.value = changeP
                    changePrivateAccount.call()
                }
            }, {
                DLogger.d("Error 비공개 => ${it.message}")
            })
    }

    fun changeRecUserYn(changeR: String) {
        /**
         * 내 계정 추천 여부 api
         */
        apiService.updateSettings(
            UploadSettingBody(
                recUserYn = changeR
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    // preference 변경
                    isRecommendMyProfile.value = changeR
                    recommendMyProfile.call()
                }
            }, {
                DLogger.d("Error 추천 => ${it.message}")
            })
    }

    fun changeAllAccountState(changeP: String, changeR: String) {
        /**
         * 비공개계정여부 api
         */
        val prv = apiService.updateSettings(UploadSettingBody(prvAccYn = changeP))

        /**
         * 내 계정 추천 여부 api
         */
        val user = apiService.updateSettings(UploadSettingBody(recUserYn = changeR))

        val list = mutableListOf<Pair<Int, Single<BaseResponse>>>()
        list.add(0 to prv)
        list.add(1 to user)

        Observable.fromIterable(list)
            .concatMap { target ->
                target.second.delay(300, TimeUnit.MILLISECONDS).map { target.first to it }
                    .toObservable()
            }
            .doLoading()
            .applyApiScheduler()
            .request({ response ->
                if (response.first == 0) {
                    // preference 변경
                    isPrivateAccount.value = changeP
                    changePrivateAccount.call()
                } else if (response.first == 1) {
                    // preference 변경
                    isRecommendMyProfile.value = changeR
                    recommendMyProfile.call()
                }
                onLoadingDismiss()

            }, {
                onLoadingDismiss()
                DLogger.d("changeAllAccountState error  ${it}")
            })

    }

    /**
     * 프로필 사진만 업로드 api
     */
    fun uploadProfile(realUri: String) {
        apiService.getResourcePath(
            resType = ResourcePathType.PROFILE.code
        )
            .map {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    uploadSettingBody = UploadSettingBody(
                        pfFrImgPath = it.result.pfFrImgPath,
                        pfBgImgPath = it.result.pfBgImgPath
                    )
                    it.result

                } else {
                    throw IllegalArgumentException("IllegalArgumentException")
                }
            }
            .flatMap { handleProfile(realUri, it) }
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                DLogger.d("requestCameraPictureUpload 업로드 완료 이미지 경로 => ${it.first} / ${it.second}")

                // 배경사진 변경 플래그 true일경우 배경사진 업로드 false일경우 바로 업데이트
                if (changeBackgroundProfileYn.value == true) {
                    uploadBgProfile(bgProfileImg.value.toString())
                } else {
                    updateSettings()
                }
            }, failure = {

            })
    }

    /**
     * 배경 사진 업로드 api
     */
    fun uploadBgProfile(realUri: String) {
        apiService.getResourcePath(
            resType = ResourcePathType.PROFILE.code
        )
            .map {
                if (it.status == HttpStatusType.SUCCESS.status) {

                    uploadSettingBody = UploadSettingBody(
                        pfFrImgPath = it.result.pfFrImgPath,
                        pfBgImgPath = it.result.pfBgImgPath
                    )
                    it.result

                } else {
                    throw IllegalArgumentException("IllegalArgumentException")
                }
            }
            .flatMap { handleBgProfile(realUri, it) }
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                updateSettings()
            }, failure = {

            })

    }

    private fun handleProfile(
        realUri: String,
        data: GetResourcePathInfo
    ): Single<Pair<String, String>> {
        val uploadPath = data.pfFrImgPath
        val imageUri = Uri.parse(Config.BASE_FILE_URL.plus(uploadPath))

        return fileProvider.requestCameraPictureUpload(realUri, uploadPath)
            .map { Config.BASE_FILE_URL.plus(imageUri.path) to it }
    }

    private fun handleBgProfile(
        realUri: String,
        data: GetResourcePathInfo
    ): Single<Pair<String, String>> {
        val uploadPath = data.pfBgImgPath
        val imageUri = Uri.parse(Config.BASE_FILE_URL.plus(uploadPath))
        return fileProvider.requestCameraPictureUpload(realUri, uploadPath)
            .map { Config.BASE_FILE_URL.plus(imageUri.path) to it }
    }

    /**
     * 데이터 저장
     */
    fun onComplete() {
        if (changeProfileYn.value == true) {
            uploadProfile(profileImg.value.toString())
        } else if (changeBackgroundProfileYn.value == true) {
            uploadBgProfile(bgProfileImg.value.toString())
        }
    }

    fun updateSettings() {
        apiService.updateSettings(

            if (profileImg.value.isNullOrEmpty() && bgProfileImg.value.isNullOrEmpty()) {
                UploadSettingBody(
                    pfFrImgPath = " ",
                    pfBgImgPath = " "
                )

            } else if (bgProfileImg.value.isNullOrEmpty()) {
                UploadSettingBody(
                    pfFrImgPath = uploadSettingBody.pfFrImgPath,
                    pfBgImgPath = " "
                )

            } else if (profileImg.value.isNullOrEmpty()) {
                UploadSettingBody(
                    pfFrImgPath = " ",
                    pfBgImgPath = uploadSettingBody.pfBgImgPath
                )

            } else {
                UploadSettingBody(
                    pfFrImgPath = uploadSettingBody.pfFrImgPath,
                    pfBgImgPath = uploadSettingBody.pfBgImgPath
                )
            }

        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    reqRefreshMyProfile()
                }
            }, {
                DLogger.d("Error  UploadSetting=> ${it.message}")
            })
    }

    /**
     * setting preference 값 갱신 처리하기 위해 api 호출
     */
    fun reqRefreshMyProfile() {
        apiService.fetchMyProfileInfo()
            .map { it.result }
            .applyApiScheduler()
            .request(success = {
                RxBus.publish(RxBusEvent.MyPageRefreshEvent(it))

                if (changeProfileYn.value == true) {
                    RxBus.publish(RxBusEvent.ProfileRefreshEvent(profileImg = profileImg.value.toString(), profileType = true))
                }

                if (changeBackgroundProfileYn.value == true) {
                    RxBus.publish(RxBusEvent.ProfileRefreshEvent(bgProfileImg = bgProfileImg.value.toString(), bgProfileType = true))
                }
//                startOneButtonPopup.call()
                backpress.call()
            })
    }

    /**
     * 탈퇴 api
     */
    fun secession() {
        val page = ActivityResult(
            targetActivity = SessionActivity::class,
        )
        moveToPage(page)
    }

    override fun onGalleryConfirm(imageList: List<String>) {
        val str = imageList[0]
        val uri = Uri.parse(str)
        if (clickProfileType == 1) {
            startEditFrProfile.value = uri
        } else {
            startEditBgProfile.value = uri
        }
    }

    override fun onGalleryDismiss() {}

    fun onProfileConfirm(imageUri: Uri) {
        if (clickProfileType == 1) {
            _profileImg.value = imageUri.toString()
            DLogger.d("_profileImg ==> ${imageUri}")
            changeProfileYn.value = true
        } else {
            _bgProfileImg.value = imageUri.toString()
            DLogger.d("_bgProfileImg ==> ${imageUri}")
            changeBackgroundProfileYn.value = true
        }

        isChangeProfile.call()
    }

    override fun onEditConfirm(imagePath: String) {
        val uri = Uri.parse(imagePath)
        onProfileConfirm(uri)
    }
}