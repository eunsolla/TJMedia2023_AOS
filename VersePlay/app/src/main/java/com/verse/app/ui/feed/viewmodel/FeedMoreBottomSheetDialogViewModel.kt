package com.verse.app.ui.feed.viewmodel

import androidx.lifecycle.LiveData
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.BlockType
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.ReportType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.param.AvailCommentBody
import com.verse.app.model.param.BlockBody
import com.verse.app.model.param.FeedUpdateBody
import com.verse.app.model.param.UnInterestedBody
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :  메인 -> ... 더보기 ViewModel
 *
 * Created by
 */
@HiltViewModel
class FeedMoreBottomSheetDialogViewModel @Inject constructor(
    private val apiService: ApiService,
    private val loginManager: LoginManager
) : FragmentViewModel() {

    companion object {
        const val FEED_DATA = "FEED_DATA"
        const val IS_DETAIL = "IS_DETAIL"
    }

    private val _startCloseEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startCloseEvent: LiveData<Unit> get() = _startCloseEvent

    private val _startShare: SingleLiveEvent<FeedContentsData> by lazy { SingleLiveEvent() }
    val startShare: LiveData<FeedContentsData> get() = _startShare

    private val _startChangeContents: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startChangeContents: LiveData<Unit> get() = _startChangeContents

    private val _startReport: SingleLiveEvent<Pair<ReportType, String>> by lazy { SingleLiveEvent() }
    val startReport: LiveData<Pair<ReportType, String>> get() = _startReport

    private val _startLoginPage: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startLoginPage: LiveData<Unit> get() = _startLoginPage

    private val _startDeleteRefresh: SingleLiveEvent<Pair<FeedContentsData, Boolean>> by lazy { SingleLiveEvent() }
    val startDeleteRefresh: LiveData<Pair<FeedContentsData, Boolean>> get() = _startDeleteRefresh

    private val _startBlockPopup: SingleLiveEvent<Pair<String, Boolean>> by lazy { SingleLiveEvent() }   // 차단
    val startBlockPopup: LiveData<Pair<String, Boolean>> get() = _startBlockPopup

    private val _startCommentPopup: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }   // 댓글
    val startCommentPopup: LiveData<Boolean> get() = _startCommentPopup

    private val _startDeletePopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }   // 댓글
    val startDeletePopup: LiveData<Unit> get() = _startDeletePopup

    private val _callBack: SingleLiveEvent<Pair<Int, Boolean>> by lazy { SingleLiveEvent() }
    val callBack: LiveData<Pair<Int, Boolean>> get() = _callBack

    lateinit var feedContentsData: FeedContentsData
    var isDetail: Boolean = true

    //내 피드 여부
    private val _isMyFeed: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }   // popup
    val isMyFeed: LiveData<Boolean> get() = _isMyFeed

    private val _startMessage: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }   // popup
    val startMessage: LiveData<Int> get() = _startMessage

    private val _interestedPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }   // 댓글
    val interestedPopup: LiveData<Unit> get() = _interestedPopup


    fun onStart() {
        val feedData = savedStateHandle.get<FeedContentsData>(FEED_DATA) ?: FeedContentsData()
        val isDetail = savedStateHandle.get<Boolean>(IS_DETAIL) ?: true

        if (feedData.feedMngCd.isEmpty()) {
            onClose()
        }

        if (loginManager.isLogin()) {
            _isMyFeed.value = feedData.ownerMemCd == loginManager.getUserLoginData().memCd
        }

        feedContentsData = feedData
        this.isDetail = isDetail
        DLogger.d("더보기 상세 여부=> ${isDetail}")
    }

    /**
     * 공유
     */
    fun onShareContents() {
        _startShare.value = feedContentsData
    }

    /**
     * 신고
     */
    fun onReportContents() {
        _startReport.value = ReportType.FEED_CONTENTS to feedContentsData.feedMngCd
    }


    /**
     * 컨텐츠 노출 설정 변경
     */
    fun onUpdateOpenContents() {
        _startChangeContents.call()
    }

    /**
     * 댓글 허용/비허용
     */
    fun onUpdateStateComment() {
        _startCommentPopup.value = feedContentsData.isAcceptComment
    }

    /**
     * 컨텐츠 삭제
     */
    fun onDeleteContents(isDeleted: Boolean) {
        if (isDeleted) {
            return
        } else {
            _startDeletePopup.call()
        }
    }

    /**
     * 차단
     */
    fun onBlock() {
        if (!loginManager.isLogin()) {
            moveToLoginPage()
            return
        }

        _startBlockPopup.value = feedContentsData.feedMngCd to !feedContentsData.isBlock
    }

    /**
     * 관심없음
     */
    fun onUninterested() {
        if (!loginManager.isLogin()) {
            moveToLoginPage()
            return
        }

        requestUninterested()
    }


    /**
     * 컨텐츠 노출 설정 공개 or 친구공개 or 비공개
     */
    fun requestFeedVisibility(type: String) {
        apiService.requestFeedVisibility(
            FeedUpdateBody(
                feedMngCd = feedContentsData.feedMngCd,
                reqType = type
            )
        )
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    feedContentsData.exposCd = type
                    onClose()
                }
            }, {
                DLogger.d("Error requestFeedVisibility=>${it.message}")
            })
    }

    /**
     * 차단 API
     */
    fun requestBlock() {
        apiService.updateBlock(
            BlockBody(
                blockContentCode = feedContentsData.feedMngCd,
                blockYn = if (feedContentsData.isBlock) AppData.N_VALUE else AppData.Y_VALUE,
                blockType = BlockType.FEED.code
            ))
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    _callBack.value = 0 to !feedContentsData.isBlock
                }
            }, {
                DLogger.d("Error onBlock=>${it.message}")
            })
    }

    /**
     * 관심없음 API
     */
    private fun requestUninterested() {
        apiService.updateUninterested(
            UnInterestedBody(contentCode = feedContentsData.feedMngCd, uninterYn = AppData.Y_VALUE)
        )
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    _callBack.value = 1 to true
                }
            }, {
            })
    }

    /**
     * 댓글 허용/비허용
     */
    fun requestComment() {

        apiService.updateAvailComment(
            AvailCommentBody(
                feedMngCd = feedContentsData.feedMngCd,
                availComment = if (feedContentsData.isAcceptComment) AppData.N_VALUE else AppData.Y_VALUE
            )
        )
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    feedContentsData.isAcceptComment = !feedContentsData.isAcceptComment
                    _startCloseEvent.call()
                }
            }, {
                DLogger.d("Error updateAvailComment=>${it.message}")
            })
    }

    /**
     * 삭제
     */
    fun requestDeleteFeed() {
        apiService.deleteMyFeed(FeedUpdateBody(feedMngCd = feedContentsData.feedMngCd))
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    _startDeleteRefresh.value = feedContentsData to isDetail
                }
            }, {
                DLogger.d("Error onDeleteContents=>${it.message}")
            })
    }


    /**
     * 로그인 이동
     */
    private fun moveToLoginPage() {
        _startLoginPage.call()
    }


    /**
     * 종료
     */
    fun onClose() {
        _startCloseEvent.call()
    }
}
