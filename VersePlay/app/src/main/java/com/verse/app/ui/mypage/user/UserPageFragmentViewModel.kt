package com.verse.app.ui.mypage.user

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.BlockType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.UserType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.chat.ChatMessageIntentModel
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.mypage.FollowIntentModel
import com.verse.app.model.mypage.GetSingPassSeasonClearInfo
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.mypage.RecommendUserResponseInfo
import com.verse.app.model.param.BlockBody
import com.verse.app.model.param.FollowBody
import com.verse.app.model.user.UserData
import com.verse.app.model.weblink.WebLinkData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.ui.chat.message.ChatMessageActivity
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.mypage.activity.MypageFollowActivity
import com.verse.app.usecase.PostFollowUseCase
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.WebLinkProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Description : 마이페이지 > 다른 사용자 페이지
 *
 * Created by juhongmin on 2023/06/02
 */
@HiltViewModel
class UserPageFragmentViewModel @Inject constructor(
    private val resProvider: ResourceProvider,
    private val apiService: ApiService,
    val loginManager: LoginManager,
    val repository: Repository,
    private val webLinkProvider: WebLinkProvider,
    private val postFollowUseCase: PostFollowUseCase
) : FragmentViewModel() {

    // MainRightFragment 에서 마이페이지 중복 API 호출 방지하기 위한 값
    private val IS_MAIN_RIGHT_API_CALL = "IS_MAIN_RIGHT_API_CALL"

    val startOneButtonPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }      // popup
    val startCompleteButtonPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }      // popup

    private val _startBlockPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }   // 차단
    val startBlockPopup: LiveData<String> get() = _startBlockPopup

    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = resProvider.getString(R.string.videoupload_top_right_menu)))
            add(PagerTabItem(title = resProvider.getString(R.string.str_like)))
            add(PagerTabItem(title = resProvider.getString(R.string.str_favorite)))
        }
    }

    val tabPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }

    //====================================================================================================================
    val memberData: MutableLiveData<UserData> by lazy { MutableLiveData() }
    val profileImageUrl: LiveData<String> get() = Transformations.map(memberData) { it.pfFrImgPath }
    val profileBgImageUrl: LiveData<String> get() = Transformations.map(memberData) { it.pfBgImgPath }
    val memberName: LiveData<String> get() = Transformations.map(memberData) { it.memNk }

    val isInfluencer: LiveData<Boolean> get() = Transformations.map(memberData) { it.memTpCd == UserType.INFLUENCER_MEMBER.code }

//    val memberCodeResource: LiveData<Drawable>
//        get() = Transformations.map(memberData) { getGradeImageResource(it.memTpCd) } // 회원 유형 상태로 변경예정 (인플루언서 배지)

    val memberContentsCntTxt: LiveData<String> get() = Transformations.map(memberData) { it.uploadFeedCnt.toString() }
    val memberFollowingCnTxt: LiveData<String> get() = Transformations.map(memberData) { it.followingCnt.toString() }
    private val _memberStatusMessage: MutableLiveData<String> by lazy { MutableLiveData() }
    val memberStatusMessage: LiveData<String> get() = _memberStatusMessage

    private val _memberFollowerCntTxt: MutableLiveData<String> by lazy { MutableLiveData() }
    val memberFollowerCntTxt: LiveData<String> get() = _memberFollowerCntTxt

    val memberPrivateYN: LiveData<Boolean>
        get() = Transformations.map(memberData) {
            it.fgPrivateYn == AppData.Y_VALUE || it.fgBlockYn == AppData.Y_VALUE
        }

    val memberBlockYN: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }

    //====================================================================================================================

    val setTabPrivate: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val userStateText: MutableLiveData<String> by lazy { MutableLiveData() }
    val userStateSubText: MutableLiveData<String> by lazy { MutableLiveData() }
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

    private val _outLink: MutableLiveData<String> by lazy { MutableLiveData() }
    val outLink: LiveData<String> get() = _outLink
    val moveToMyLink: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    private val _startBackEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startBackEvent: LiveData<Unit> get() = _startBackEvent

    val moveToProfileImageDetail: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                         // 프로필 이미지 상세

    private var memberCode: String = ""

    private val _startFocusingTabPositionEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startFocusingTabPositionEvent: LiveData<Int> get() = _startFocusingTabPositionEvent
    private val _isFollowing: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isFollowing: LiveData<Boolean> get() = _isFollowing

    //====================================================================================================================
    private val linkData: MutableLiveData<WebLinkData> by lazy { MutableLiveData() }
    val linkIconUrl: LiveData<String> get() = Transformations.map(linkData) { it.iconUrl }
    val showInvalidUrl: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 유효하지 않은 URL일경우
    //====================================================================================================================

    val followText: LiveData<String>
        get() = Transformations.map(isFollowing) {
            if (it) {
                resProvider.getString(R.string.str_following)
            } else {
                resProvider.getString(R.string.str_follow)
            }
        }


    fun start() {
        if (isInitViewCreated()) {
            return
        }
        startMyProfile()
    }

    private fun startMyProfile() {
        val intentData = savedStateHandle.get<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA) ?: return
        memberCode = intentData.memberCode
        Single.mergeDelayError(
            reqMyProfileInfo(intentData),
            reqRecommendUser(),
            reqSingPass()
        )
            .doOnSubscribe { handleLoadShow() }
            .compose(applyApiScheduler())
            .request(next = {
                handleOnSuccess(it)
            },
                complete = {
                    setInitViewCreated()
                }, error = {
                    DLogger.d("ERROR $it")
                    onLoadingDismiss()
                })
    }

    /**
     * 특정 페이지에서 다시 갱신 처리하는 함수
     */
    fun reqRefreshUserProfile() {
        val isApiCall = getIntentData(IS_MAIN_RIGHT_API_CALL, false)
        if (isApiCall) return
        apiService.fetchUserProfileInfo(memberCode)
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

    private fun reqMyProfileInfo(intentModel: MyPageIntentModel): Single<UserData> {
        val api = if (intentModel.isMainRightFragmentType) {
            Single.just(intentModel).map { UserData(it) }
        } else {
            apiService.fetchUserProfileInfo(intentModel.memberCode).map { it.result }
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
                _isLoading.value = false
                handleMemberProfile(res)
                onLoadingDismiss()
            }

            is RecommendUserResponseInfo -> {
                handleRecommendUser(res)
            }

            is GetSingPassSeasonClearInfo -> {
                handleSingPass(res)
            }
        }
    }

    private fun handleMemberProfile(data: UserData) {
        memberData.value = data
        _memberStatusMessage.value = data.instDesc
        _memberFollowerCntTxt.value = data.followerCnt.toString()
        _outLink.value = data.outLinkUrl
        _isFollowing.value = data.fgFollowYn == AppData.Y_VALUE

        if (data.fgBlockYn == AppData.Y_VALUE) {
            setTabPrivate.value = true
            userStateText.value = resProvider.getString(R.string.contents_block_account)
            userStateSubText.value = resProvider.getString(R.string.contents_block_account_sub)

        } else if (data.fgPrivateYn == AppData.Y_VALUE) {
            setTabPrivate.value = true
            userStateText.value = resProvider.getString(R.string.contents_private_account)
            userStateSubText.value = resProvider.getString(R.string.contents_private_account_sub)
        } else {
            setTabPrivate.value = false
        }
        mylinkfavicon(outLink.value.toString())
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

//    private fun getGradeImageResource(code: String): Drawable? {
//        val id = when (code) {
//            UserPurchaseLevel.USER.code -> R.drawable.user_lv_1
//            UserPurchaseLevel.NORMAL.code -> R.drawable.common_lv_2
//            UserPurchaseLevel.FRIEND.code -> R.drawable.buddy_lv_3
//            UserPurchaseLevel.FAMILY.code -> R.drawable.family_lv_4
//            UserPurchaseLevel.SHINE.code -> R.drawable.shining_lv_5
//            else -> R.drawable.user_lv_1
//        }
//        return resProvider.getDrawable(id)
//    }

    /**
     * 업로드 탭으로 이동하는 함수
     */
    fun moveToContents() {
        if (memberData.value != null) {
            if (memberData.value!!.fgPrivateYn == AppData.Y_VALUE || memberData.value!!.fgBlockYn == AppData.Y_VALUE) {
                return
            } else {
                _startFocusingTabPositionEvent.value = 0
            }
        }
    }

    /**
     * 팔로잉 페이지 이동
     */
    fun moveToFollowersPage() {
        if (memberData.value != null){
            if (memberData.value!!.fgPrivateYn == AppData.Y_VALUE || memberData.value!!.fgBlockYn == AppData.Y_VALUE) return
        }
        val detail = memberData.value ?: return
        val page = ActivityResult(
            targetActivity = MypageFollowActivity::class,
            data = bundleOf(ExtraCode.FOLLOW_DATA to FollowIntentModel(detail, false, 0))
        )
        moveToPage(page)

    }

    /**
     * 팔로어 페이지 이동
     */
    fun moveToFollowingPage() {
        if (memberData.value != null) {
            if (memberData.value!!.fgPrivateYn == AppData.Y_VALUE || memberData.value!!.fgBlockYn == AppData.Y_VALUE) return
        }
        val detail = memberData.value ?: return
        val page = ActivityResult(
            targetActivity = MypageFollowActivity::class,
            data = bundleOf(ExtraCode.FOLLOW_DATA to FollowIntentModel(detail, false, 1))
        )
        moveToPage(page)
    }

    /**
     * 팔로잉 해제 or 추가 이벤트 함수
     */
    fun onFollowingEvent() {
        val detail = memberData.value ?: return
        val followYn = if (isFollowing.value == true) AppData.N_VALUE else AppData.Y_VALUE

        val body = FollowBody(
            followYn = followYn,
            userCode = detail.memCd
        )
        postFollowUseCase(body)
            .applyApiScheduler()
            .request(
                success = {
                    if (it.status == HttpStatusType.SUCCESS.status) {
                        detail.fgFollowYn = followYn

                        _isFollowing.value = detail.fgFollowYn == AppData.Y_VALUE

                        if (detail.fgFollowYn == AppData.Y_VALUE) {
                            if (detail.followerCnt == 0) {
                                detail.followerCnt = 1
                                _memberFollowerCntTxt.value = detail.followerCnt.toString()

                            } else {
                                detail.followerCnt = detail.followerCnt + 1
                                _memberFollowerCntTxt.value = detail.followerCnt.toString()
                            }

                        } else if (detail.fgFollowYn == AppData.N_VALUE && detail.followerCnt > 0) {
                            detail.followerCnt = detail.followerCnt - 1
                            _memberFollowerCntTxt.value = detail.followerCnt.toString()
                        }

                        RxBus.publish(RxBusEvent.MypageFollowRefreshEvent(detail.memCd))
                        reqRefreshMyProfile()

                    } else if (it.status == HttpStatusType.FAIL.status) {
                        startOneButtonPopup.value = it.message
                    }
                }
            )
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
            })
    }

    fun moveToMessage() {
        if (memberData.value == null) return

        if (memberData.value!!.fgBlockYn == AppData.Y_VALUE || memberBlockYN.value) {
            startOneButtonPopup.value = resProvider.getString(R.string.message_block_account)

        } else if (memberData.value!!.fgPrivateYn == AppData.Y_VALUE) {
            startOneButtonPopup.value = resProvider.getString(R.string.message_private_account)

        } else {
            val user = memberData.value ?: return
            val page = ActivityResult(
                targetActivity = ChatMessageActivity::class,
                data = bundleOf(ExtraCode.CHAT_MESSAGE_ROOM_DATA to ChatMessageIntentModel(user)),
                enterAni = R.anim.in_right_to_left,
                exitAni = R.anim.out_right_to_left,
            )
            moveToPage(page)
        }
    }

    fun moveToInstar() {
        moveToMyLink.call()
    }

    fun blockUser() {
        // 팝업 노출
        _startBlockPopup.value = memberData.value?.memNk
    }

    fun requestBlockUser() {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)
            return
        }

        memberData.value?.let {
            apiService.updateBlock(
                BlockBody(
                    blockContentCode = it.memCd,
                    blockYn = AppData.Y_VALUE,
                    blockType = BlockType.USER.code
                )
            )
                .applyApiScheduler()
                .doLoading()
                .request({ res ->
                    if (res.status == HttpStatusType.SUCCESS.status) {
                        setTabPrivate.value = true
                        userStateText.value = resProvider.getString(R.string.contents_block_account)
                        memberBlockYN.value = true
                        startCompleteButtonPopup.value =
                            resProvider.getString(R.string.str_block_user_complete)
                        RxBus.publish(RxBusEvent.MypageFollowRefreshEvent(loginManager.getUserLoginData().memCd))
                        reqRefreshMyProfile()
                    }
                }, {
                    DLogger.d("Error onBlock=>${it.message}")
                })
        }
    }

    fun onBack() {
        _startBackEvent.call()
    }

    fun showAndHiddenRecommendUser() {
        _isRecommendUserVisible.value = isRecommendUserVisible.value != true
    }

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
