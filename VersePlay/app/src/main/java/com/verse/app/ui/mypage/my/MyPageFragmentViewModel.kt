package com.verse.app.ui.mypage.my

import android.graphics.drawable.Drawable
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.UserPurchaseLevel
import com.verse.app.contants.UserType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.mypage.FollowIntentModel
import com.verse.app.model.mypage.GetSettingInfoData
import com.verse.app.model.mypage.GetSingPassSeasonClearInfo
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.mypage.RecommendUserResponseInfo
import com.verse.app.model.user.UserData
import com.verse.app.model.weblink.WebLinkData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.chat.rooms.ChatRoomsActivity
import com.verse.app.ui.mypage.activity.MypageAlertActivity
import com.verse.app.ui.mypage.activity.MypageFollowActivity
import com.verse.app.ui.mypage.activity.SettingMyInfoActivity
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : 메인페이지 > 마이페이지
 *
 * Created by juhongmin on 2023/05/30
 */
@HiltViewModel
class MyPageFragmentViewModel @Inject constructor(
    private val resProvider: ResourceProvider,
    private val apiService: ApiService,
    private val loginManager: LoginManager,
    val accountPref: AccountPref,
    private val webLinkProvider: WebLinkProvider,
) : FragmentViewModel() {

    // MainRightFragment 에서 마이페이지 중복 API 호출 방지하기 위한 값
    private val IS_MAIN_RIGHT_API_CALL = "IS_MAIN_RIGHT_API_CALL"

    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = resProvider.getString(R.string.videoupload_top_right_menu)))
            add(PagerTabItem(title = resProvider.getString(R.string.str_like)))
            add(PagerTabItem(title = resProvider.getString(R.string.str_favorite)))
        }
    }

    val tabPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }

    private val memberData: MutableLiveData<UserData> by lazy { MutableLiveData() }

    val memberName: LiveData<String> get() = Transformations.map(memberData) { it.memNk }

    val isInfluencer: LiveData<Boolean> get() = Transformations.map(memberData) { it.memTpCd == UserType.INFLUENCER_MEMBER.code }
    val memberGradeResource: LiveData<Drawable>
        get() = Transformations.map(memberData) { getGradeImageResource(it.memGrCd) }
    val memberContentsCntTxt: LiveData<String> get() = Transformations.map(memberData) { it.uploadFeedCnt.toString() }
    val memberFollowerCntTxt: LiveData<String> get() = Transformations.map(memberData) { it.followerCnt.toString() }
    val memberFollowingCnTxt: LiveData<String> get() = Transformations.map(memberData) { it.followingCnt.toString() }

    private val _memberStatusMessage: MutableLiveData<String> by lazy { MutableLiveData() }
    val memberStatusMessage: LiveData<String> get() = _memberStatusMessage

    private val _profileImageUrl: MutableLiveData<String> by lazy { MutableLiveData() }
    val profileImageUrl: LiveData<String> get() = _profileImageUrl

    private val _profileBgImageUrl: MutableLiveData<String> by lazy { MutableLiveData() }
    val profileBgImageUrl: LiveData<String> get() = _profileBgImageUrl

    private val _outLink: MutableLiveData<String> by lazy { MutableLiveData() }
    val outLink: LiveData<String> get() = _outLink

    val moveToMyLink: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                         // 외부링크 이동

    val moveToProfileImageDetail: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                         // 프로필 이미지 상세

    private val _isMemberStatusLongMessageOpen: NonNullLiveData<Boolean> by lazy {
        NonNullLiveData(
            false
        )
    }
    val isMemberStatusLongMessageOpen: LiveData<Boolean> get() = _isMemberStatusLongMessageOpen

    private val _isRecommendUserVisible: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isRecommendUserVisible: LiveData<Boolean> get() = _isRecommendUserVisible

    private val _recommendUserList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val recommendUserList: ListLiveData<BaseModel> get() = _recommendUserList

    private val _singPassList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val singPassList: ListLiveData<BaseModel> get() = _singPassList

    private val _isLoading: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isLoading: LiveData<Boolean> get() = _isLoading
    val isNotify: LiveData<Boolean> get() = Transformations.map(memberData) { it.newAlrimYn == AppData.Y_VALUE }

    private val _startSettingsPageEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startSettingsPageEvent: LiveData<Unit> get() = _startSettingsPageEvent

    private val _startFocusingTabPositionEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startFocusingTabPositionEvent: LiveData<Int> get() = _startFocusingTabPositionEvent

    private val linkData: MutableLiveData<WebLinkData> by lazy { MutableLiveData() }
    val linkIconUrl: LiveData<String> get() = Transformations.map(linkData) { it.iconUrl }
    val showInvalidUrl: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 유효하지 않은 URL일경우

    fun start() {
        if (isInitViewCreated()) {
            return
        }
        startMyProfile()
    }

    private fun startMyProfile() {
        _isLoading.value = true
        val intentData = savedStateHandle.get<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA) ?: return
        Single.mergeDelayError(
            reqMyProfileInfo(intentData),
            reqRecommendUser(),
            reqSingPass(),
            reqSettingInfo()
        )
            .doOnSubscribe { handleLoadShow() }
            .compose(applyApiScheduler())
            .request(next = {
                handleOnSuccess(it)
                onLoadingDismiss()
            }, complete = {
                onLoadingDismiss()
                setInitViewCreated()
                DLogger.d("마이페이지 API 다 호출 합")
            }, error = {
                onLoadingDismiss()
            })
    }

    /**
     * 특정 페이지에서 다시 갱신 처리하는 함수
     */
    fun reqRefreshMyProfile() {
        val isApiCall = getIntentData(IS_MAIN_RIGHT_API_CALL, false)
        if (isApiCall) return
        apiService.fetchMyProfileInfo()
            .map { it.result }
            .applyApiScheduler()
            .request(success = {
                handleMemberProfile(it)
                setResultSaveData(IS_MAIN_RIGHT_API_CALL, true)
            })
    }

    /**
     * 특정 페이지에서 로딩바를 보여주지 않아야 하는 조건이 있어서 값에 따라 처리하는 함수
     */
    private fun handleLoadShow() {
        val intentModel = getIntentData<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)
        val isLoadingShow = intentModel?.isShowLoading ?: true
        if (isLoadingShow) {
            onLoadingShow()
        }
    }

    fun initMyPageRefresh() {
        RxBus.listen(RxBusEvent.MyPageRefreshEvent::class.java)
            .filter { isSameMyPage(it.memberData.memCd) }
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { handleSettingProfile(it.memberData) }
            .subscribe().addTo(compositeDisposable)
    }

    private fun isSameMyPage(diffMemberCode: String): Boolean {
        return loginManager.getUserLoginData().memCd == diffMemberCode
    }

    fun reqSettingInfo(): Single<GetSettingInfoData> {
        return apiService.getSettingInfo()
            .map { it.result }
            .onErrorReturn { GetSettingInfoData() }
            .subscribeOn(Schedulers.io())
    }

    private fun reqMyProfileInfo(intentModel: MyPageIntentModel): Single<UserData> {
        val api = if (intentModel.isMainRightFragmentType) {
            Single.just(intentModel).map { UserData(it) }
        } else {
            apiService.fetchMyProfileInfo().map { it.result }
        }
        return api.subscribeOn(Schedulers.io())
    }

    private fun reqRecommendUser(): Single<RecommendUserResponseInfo> {
        return apiService.getRecommendUser()
            .map { it.result }
            .onErrorReturn { RecommendUserResponseInfo() }
            .subscribeOn(Schedulers.io())
    }

    private fun reqSingPass(): Single<GetSingPassSeasonClearInfo> {
        return apiService.getSingPassSeasonClearInfo(pageNum = 1, sortType = SortEnum.DESC.query)
            .map { it.result }
            .onErrorReturn { GetSingPassSeasonClearInfo() }
            .subscribeOn(Schedulers.io())
    }

    private fun handleOnSuccess(res: Any) {
        DLogger.d("handleOnSuccess $res")
        when (res) {
            is UserData -> {
                if (res.memCd.isNotEmpty()) {
                    _isLoading.value = false
                    handleMemberProfile(res)
                }
            }

            is RecommendUserResponseInfo -> {
                handleRecommendUser(res)
            }

            is GetSingPassSeasonClearInfo -> {
                handleSingPass(res)
            }

            is GetSettingInfoData -> {
                handleSetting(res)
            }
        }
    }

    private fun handleMemberProfile(data: UserData) {
        memberData.value = data
        _memberStatusMessage.value = data.instDesc
        _outLink.value = data.outLinkUrl
        _profileImageUrl.value = data.pfFrImgPath
        _profileBgImageUrl.value = data.pfBgImgPath
        mylinkfavicon(outLink.value.toString())
    }

    /**
     * 프리퍼런스 저장용 핸들
     */
    private fun handleSettingProfile(data: UserData) {
        memberData.value = data
        _memberStatusMessage.value = data.instDesc
        _outLink.value = data.outLinkUrl
        mylinkfavicon(outLink.value.toString())

        UserSettingManager.getSettingInfo()?.let {
            it.pfFrImgPath = data.pfFrImgPath
            it.pfBgImgPath = data.pfBgImgPath
            it.memEmail = data.memEmail
            it.outLinkUrl = data.outLinkUrl
            it.instDesc = data.instDesc
        }
    }

    private fun handleSingPass(data: GetSingPassSeasonClearInfo) {
        _singPassList.clear()
        _singPassList.addAll(data.dataList)
    }

    private fun handleRecommendUser(data: RecommendUserResponseInfo) {
        _recommendUserList.clear()
        // datalist -> userlist로 변경
        _recommendUserList.addAll(data.userList)
    }

    private fun handleSetting(data: GetSettingInfoData) {
        UserSettingManager.setSettingInfo(data)
    }

    private fun getGradeImageResource(code: String): Drawable? {
        val id = when (code) {
            UserPurchaseLevel.USER.code -> R.drawable.user_lv_1
            UserPurchaseLevel.NORMAL.code -> R.drawable.common_lv_2
            UserPurchaseLevel.FRIEND.code -> R.drawable.buddy_lv_3
            UserPurchaseLevel.FAMILY.code -> R.drawable.family_lv_4
            UserPurchaseLevel.SHINE.code -> R.drawable.shining_lv_5
            else -> R.drawable.user_lv_1
        }
        return resProvider.getDrawable(id)
    }

    fun moveToNotifyPage() {
        // 알림 페이지 이동
        moveToPage(
            ActivityResult(
                targetActivity = MypageAlertActivity::class
            )
        )

        val member = memberData.value ?: return
        member.newAlrimYn = AppData.N_VALUE
        memberData.value = member
    }

    fun moveToSettingPage() {
        // 셋팅 페이지 이동
        _startSettingsPageEvent.call()
    }

    fun moveToProfileModify() {
        moveToPage(
            ActivityResult(
                targetActivity = SettingMyInfoActivity::class
            )
        )
    }

    /**
     * 업로드 탭으로 이동하는 함수
     */
    fun moveToContents() {
        _startFocusingTabPositionEvent.value = 0
    }

    /**
     * 팔로잉 페이지 이동
     */
    fun moveToFollowersPage() {
        val detail = memberData.value ?: return
        val page = ActivityResult(
            targetActivity = MypageFollowActivity::class,
            enterAni = R.anim.in_right_to_left,
            exitAni = R.anim.out_right_to_left,
            data = bundleOf(ExtraCode.FOLLOW_DATA to FollowIntentModel(detail, true, 0))
        )
        moveToPage(page)
    }

    /**
     * 팔로어 페이지 이동
     */
    fun moveToFollowingPage() {
        val detail = memberData.value ?: return
        val page = ActivityResult(
            targetActivity = MypageFollowActivity::class,
            enterAni = R.anim.in_right_to_left,
            exitAni = R.anim.out_right_to_left,
            data = bundleOf(ExtraCode.FOLLOW_DATA to FollowIntentModel(detail, true, 1))
        )
        moveToPage(page)
    }

    fun moveToMessage() {
        val page = ActivityResult(
            targetActivity = ChatRoomsActivity::class,
            enterAni = R.anim.in_right_to_left,
            exitAni = R.anim.out_right_to_left,
        )
        moveToPage(page)
    }

    fun moveToInstar() {
        moveToMyLink.call()
    }

    fun showAndHiddenRecommendUser() {
        _isRecommendUserVisible.value = isRecommendUserVisible.value != true
    }

    /**
     * 사용자 메시지 열기 / 닫힘
     */
    fun onMemberStatusMessageShowAndHidden() {
        val messageLength = memberStatusMessage.value?.length ?: 0
        if (messageLength > 8) {
            _isMemberStatusLongMessageOpen.value = isMemberStatusLongMessageOpen.value != true
        }
    }

    fun moveToProfileImageDetail() {
        memberData.value?.pfFrImgPath.let {
            moveToProfileImageDetail.value = it
        }
    }

    fun moveToProfileBgImageDetail() {
        memberData.value?.pfBgImgPath.let {
            moveToProfileImageDetail.value = it
        }
    }

    /**
     * 외부링크 파비콘 노출
     */
    fun mylinkfavicon(url: String) {
        DLogger.d("mylinkfavicon $url")
        webLinkProvider.getLinkMeta(url)
            .applyApiScheduler()
            .request(success = {
                linkData.value = it
            }, failure = {
                showInvalidUrl.call()
            })
    }
}
