package com.verse.app.ui.comment

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.CommentType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.LikeType
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.ReportType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.multiNullCheck
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.comment.CommentData
import com.verse.app.model.comment.CommentReData
import com.verse.app.model.comment.CommentReportDialogItem
import com.verse.app.model.param.CommentDeleteBody
import com.verse.app.model.param.CommentParam
import com.verse.app.model.param.CommentUpdateParam
import com.verse.app.model.param.LikeBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.usecase.GetCommentCountUseCase
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 * Description : 댓글 ViewModel Class
 *
 * Created by jhlee on 2023-04-20
 */
@HiltViewModel
class CommentViewModel @Inject constructor(
    val apiService: ApiService,
    val accountPref: AccountPref,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val repository: Repository,
    val deviceProvider: DeviceProvider,
    private val getCommentCountUseCase: GetCommentCountUseCase
) : ActivityViewModel() {

    //val commentParam = savedStateHandle.getLiveData<Pair<CommentType, String>>(ExtraCode.COMMENT_TYPE)  //파라미터로 받은 값 .

    private val _commentParam: MutableLiveData<Pair<CommentType, String>> by lazy { MutableLiveData() }
    val commentParam: LiveData<Pair<CommentType, String>> get() = _commentParam

    //닫기
    private val _startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinish: SingleLiveEvent<Unit> get() = _startFinish

    private val _commentRepCount: MutableLiveData<Int> by lazy { MutableLiveData() } //총 수
    val commentRepCount: LiveData<Int> get() = _commentRepCount

    val isTop: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }//스크롤 top 여부

    private val _commentDataList: MutableLiveData<PagingData<CommentData>> by lazy { MutableLiveData() }
    val commentDataList: LiveData<PagingData<CommentData>> get() = _commentDataList
    private val _refreshAll: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val refreshAll: SingleLiveEvent<Unit> get() = _refreshAll

    val refresh: SingleLiveEvent<Pair<Int, CommentData>> by lazy { SingleLiveEvent() }
    val refreshReComment: SingleLiveEvent<Triple<Int, Int, CommentReData>> by lazy { SingleLiveEvent() }
    val deleteReComment: SingleLiveEvent<Triple<Int, Int, Int>> by lazy { SingleLiveEvent() }
    private val _showEditDialog: SingleLiveEvent<Triple<Boolean, String, String>> by lazy { SingleLiveEvent() } //입력창 show
    val showEditDialog: LiveData<Triple<Boolean, String, String>> get() = _showEditDialog
    val curComment: NonNullLiveData<String> by lazy { NonNullLiveData("") }       //입력값
    val showReportDialog: SingleLiveEvent<CommentReportDialogItem> by lazy { SingleLiveEvent() }
    val showCommentDeleteDialog: SingleLiveEvent<CommentReportDialogItem> by lazy { SingleLiveEvent() }

    val startCheckProhibit: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 금칙어 포함 여부 확인
    val startCheckPrivateAccount: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 비공개 계정 여부 확인
    private val _startUserDetailPage: MutableLiveData<String> by lazy { MutableLiveData() }
    val startUserDetailPage: LiveData<String> get() = _startUserDetailPage

    private val _isWriteEnable: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isWriteEnable: LiveData<Boolean> get() = _isWriteEnable


    /**
     * 코멘트 타입
     * first - 콘텐츠 유형(F:피드 / L:커뮤니티라운지 / V:커뮤니티투표)
     * second - mngCd
     */
    fun start() {
        val commentType: Pair<CommentType, String>? = savedStateHandle[ExtraCode.COMMENT_TYPE]

        commentType?.let {

            if (it.first == null || it.second.isEmpty()) {
                _startFinish.call()
            }

            _commentParam.value = commentType

            requestComment()

        } ?: run {
            _startFinish.call()
        }
    }

    /**
     * API - 댓글 목록
     */
    fun requestComment() {
        commentParam.value?.let {
            multiNullCheck(it.first, it.second) { type, conCode ->

                val count = getCommentCountUseCase(type, conCode).subscribeOn(Schedulers.io()).toObservable()

                val comments = repository.fetchCommentList(
                    CommentParam(commentType = type.code, contentsCode = conCode)
                ).cachedIn(viewModelScope).subscribeOn(Schedulers.io())

                Observable.zip(count, comments) { count, comments -> count to comments }
                    .doLoading()
                    .applyApiScheduler()
                    .request({ res ->
                        _commentRepCount.value = res.first.totalCount
                        _commentDataList.value = res.second
                    }, { error ->
                        DLogger.d("Error  ${error.message}")
                        //_startFinish.call()
                    })

            } ?: run {
                DLogger.d("comment param is null")
                //_startFinish.call()
            }

        } ?: run {
            DLogger.d("comment param is null")
            //_startFinish.call()
        }
    }

    /**
     * 답글 더보기
     */
    fun onShowReComments(view: View?, data: CommentData, position: Int, type: ListPagedItemType) {

        multiNullCheck(commentParam.value, data) { param, parentData ->

            if (!data.isShowReComments || (type == ListPagedItemType.ITEM_COMMENT_RE)) {

                var curPage = data.reComments?.pageNum

                val param = CommentParam().apply {
                    this.commentType = param.first.code
                    this.contentsCode = parentData.conMngCd
                    this.commentMngCd = parentData.comtMngCd
                    this.pageNum = curPage!!
                }.toMap()

                apiService.fetchReplyList(param)
                    .doLoading()
                    .applyApiScheduler()
                    .request({ res ->

                        data.reComments?.apply {

                            val curPageNum = res.result.pageNum
                            val pageSize = res.result.pageSize
                            val targetIndex = (curPageNum * pageSize)

                            if (!this.dataList.isNullOrEmpty()) {
                                dataList.last().isMore = false
                            }

                            res.result.dataList.forEachIndexed { index, commentReData ->
                                commentReData.isMore = index == targetIndex - 1
                                commentReData.parentCommentData = data
                                commentReData.parentPosition = position
                            }

//                            dataList.clear()
                            dataList.addAll(res.result.dataList)
                            pageNum = curPage?.plus(1)!!
                        }

                        if (type == ListPagedItemType.ITEM_COMMENT) {
                            data.isShowReComments = !data.isShowReComments
                        } else {
                            view?.visibility = View.GONE
                        }

                        refresh.value = position to data

                    }, { error ->
                        DLogger.d("Error  ${error.message}")
                    })

            } else {
                //상위 답글 보기 선택시에만 접기
                if (type == ListPagedItemType.ITEM_COMMENT) {
                    clearChildComment(data)
                    refresh.value = position to data
                }
            }
        }
    }

    private fun clearChildComment(commentData: CommentData) {
        commentData.reComments = null
        commentData.isShowReComments = !commentData.isShowReComments
    }

    /**
     * 댓글 입력 Fragment Show and Hide
     */
    fun onShowInputComment(state: Boolean) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        // 비공개 계정 댓글창 선택 막기
        UserSettingManager.getSettingInfo()?.let {
            if (state && it.prvAccYn == AppData.Y_VALUE) {
                startCheckPrivateAccount.call()
                return
            }
        }

        _showEditDialog.value = Triple(state, "", "")
    }

    /**
     * 답글 입력 Fragment Show and Hide
     */
    fun onShowInputComment(state: Boolean, commentData: CommentData?) {

        UserSettingManager.getSettingInfo()?.let { res ->
            if (res.prvAccYn == AppData.Y_VALUE) {
                startCheckPrivateAccount.call()
            } else {
                commentData?.let {
                    _showEditDialog.value = Triple(state, it.comtMngCd, it.writerMemNk)
                }
            }
        }

    }

    /**
     * 작성 코멘트 값 삭제
     */
    fun clearCommentValue() {
        curComment.value = ""
    }

    /**
     *  작성시 댓글인지 답글인지 구분
     */
    fun checkRequestCommentWrite() {

        UserSettingManager.getSettingInfo()?.let {
            if (it.prvAccYn == AppData.Y_VALUE) {
                startCheckPrivateAccount.call()
            } else {
                commentParam.value?.let { param ->

                    multiNullCheck(param.first, param.second) { type, conCode ->

                        if (!curComment.value.isNullOrEmpty()) {
                            showEditDialog.value?.let {

                                _isWriteEnable.value = false

                                if (it.second.isNullOrEmpty() && it.third.isNullOrEmpty()) {
                                    onInsertComment(type.code, conCode)
                                } else {
                                    onInsertCommentRe(type.code, conCode, it.second)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 댓글 작성
     */
    private fun onInsertComment(type: String, contentsCode: String) {
        val param = CommentUpdateParam(
            commentType = type,
            contentsCode = contentsCode,
            comment = curComment.value
        )

        apiService.updateComment(param)
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    // 비공개 계정일 경우 분기처리
                    if (res.fgProhibitYn == AppData.Y_VALUE) {
                        startCheckProhibit.call()
                    } else {
                        refreshUI()
                    }
                } else if (res.status == HttpStatusType.FAIL.status) {
                    startCheckPrivateAccount.call()
                }
                _isWriteEnable.value = true
            }, { error ->
                _isWriteEnable.value = true
                DLogger.d("Error  ${error.message}")
            })

    }

    fun refreshUI() {
        //목록 갱신
        _refreshAll.call()
        //닫기 , 초기화
        onShowInputComment(false)
    }

    /**
     * 답글 작성
     */
    private fun onInsertCommentRe(type: String, contentsCode: String, commentMngCd: String) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        val param = CommentUpdateParam(
            commentType = type,
            contentsCode = contentsCode,
            commentMngCd = commentMngCd,
            comment = curComment.value
        )

        apiService.updateReply(param)
            .applyApiScheduler()
            .request({ res ->

                if (res.status == HttpStatusType.SUCCESS.status) {
                    if (res.fgProhibitYn == AppData.Y_VALUE) {
                        startCheckProhibit.call()
                    } else {
                        refreshUI()
                    }

                } else if (res.status == HttpStatusType.FAIL.status) {
                    startCheckPrivateAccount.call()
                }
                _isWriteEnable.value = true
                DLogger.d("Success Comment=> ${res}")
            }, { error ->
                _isWriteEnable.value = true
                DLogger.d("Error  ${error.message}")
            })
    }

    /**
     * 답글 좋아요
     */
    fun onLikeReComment(position: Int, data: CommentReData) {
        var likeType: String? = null

        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        // 비공계 계정 like on 막기
        UserSettingManager.getSettingInfo()?.let {
            if (it.prvAccYn == AppData.Y_VALUE) {
                startCheckPrivateAccount.call()
                return
            }
        }

        val likeYn = if (!data.isLike) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        if (commentParam.value != null) {
            if (commentParam.value!!.first == CommentType.FEED) {
                likeType = LikeType.FEED_RE_COMMENT.code
            } else if (commentParam.value!!.first == CommentType.LOUNGE) {
                likeType = LikeType.LOUNGE_RE_COMMENT.code
            } else if (commentParam.value!!.first == CommentType.COMMUNITY_VOTE) {
                likeType = LikeType.VOTE_RE_COMMENT.code
            }

            if (likeType != null) {
                onLike(likeYn, likeType, data.replyMngCd) { result ->

                    DLogger.d("onLike Re Comment result=>${result}")

                    if (result) {
                        data.isLike = !data.isLike

                        if (data.isLike) {
                            data.likeCount = data.likeCount + 1
                        } else {
                            data.likeCount = data.likeCount - 1
                        }

                        if (data.parentCommentData != null) {
                            DLogger.d("onLike Re Comment result=>${data.parentCommentData} / ${data.parentPosition}")
                        }

                        DLogger.d("onLike Re Comment result=>${data.isLike} / ${data.fgLikeYn}")

                        data.parentCommentData?.let {
                            refreshReComment.value = Triple(data.parentPosition, position, data)
                        }
                    }
                }
            }
        }
    }

    /**
     * 댓글 좋아요
     */
    fun onLikeComment(position: Int, data: CommentData) {
        var likeType: String? = null

        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        // 비공계 계정 like on 막기
        UserSettingManager.getSettingInfo()?.let {
            if (it.prvAccYn == AppData.Y_VALUE) {
                startCheckPrivateAccount.call()
                return
            }
        }

        val likeYn = if (!data.isLike) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        if (commentParam.value != null) {
            if (commentParam.value!!.first == CommentType.FEED) {
                likeType = LikeType.FEED_COMMENT.code
            } else if (commentParam.value!!.first == CommentType.LOUNGE) {
                likeType = LikeType.LOUNGE_COMMENT.code
            } else if (commentParam.value!!.first == CommentType.COMMUNITY_VOTE) {
                likeType = LikeType.VOTE_COMMENT.code
            }

            if (likeType != null) {
                onLike(likeYn, likeType, data.comtMngCd) { result ->

                    DLogger.d("onLike Comment result=>${result}")

                    if (result) {
                        data.isLike = !data.isLike

                        if (data.isLike) {
                            data.likeCount = data.likeCount + 1
                        } else {
                            data.likeCount = data.likeCount - 1
                        }

                        refresh.value = position to data
                    }
                }
            }
        }
    }


    /**
     * API 좋아요
     */
    private fun onLike(
        likeYn: String,
        likeType: String,
        conMngCd: String,
        result: (Boolean) -> Unit
    ) {

        // 비공계 계정 like on 막기
        UserSettingManager.getSettingInfo()?.let {
            if (likeYn == AppData.Y_VALUE && it.prvAccYn == AppData.Y_VALUE) {
                startCheckPrivateAccount.call()

            } else {
                apiService.updateLike(
                    LikeBody(
                        likeType = likeType,
                        likeYn = likeYn,
                        contentsCode = conMngCd
                    )
                )
                    .applyApiScheduler()
                    .request({ res ->
                        if (res.status == HttpStatusType.SUCCESS.status) {
                            result.invoke(true)
                        } else {
                            DLogger.d("fail Like=>${it}")
                        }

                    }, {
                        DLogger.d("Error Like=>${it.message}")
                    })
            }
        }
    }


    /**
     * 댓글 신고 클릭
     */
    fun onCommentReport(data: CommentData) {
        var reportType: ReportType? = null

        commentParam.value?.let {
            if (it.first.code == CommentType.FEED.code) {
                reportType = ReportType.FEED_COMMENT
            } else if (it.first.code == CommentType.LOUNGE.code) {
                reportType = ReportType.LOUNGE_COMMENT
            } else if (it.first.code == CommentType.COMMUNITY_VOTE.code) {
                reportType = ReportType.VOTE_COMMENT
            }

            reportType?.let {
                setReportInfo(
                    reportType!!,
                    null,
                    data.comtMngCd,
                    data.writerMemNk,
                    data.profileImgPath
                )
            }
        }
    }

    /**
     * 답글 신고 클릭
     */
    fun onCommentReReport(position: Int, data: CommentReData) {
        data.curPosition = position

        var reportType: ReportType? = null

        commentParam.value?.let {
            if (it.first.code == CommentType.FEED.code) {
                reportType = ReportType.FEED_RE_COMMENT
            } else if (it.first.code == CommentType.LOUNGE.code) {
                reportType = ReportType.LOUNGE_RE_COMMENT
            } else if (it.first.code == CommentType.COMMUNITY_VOTE.code) {
                reportType = ReportType.VOTE_RE_COMMENT
            }

            reportType?.let {
                setReportInfo(
                    reportType!!,
                    data,
                    data.replyMngCd,
                    data.writerMemNk,
                    data.profileImgPath
                )
            }
        }
    }

    /**
     * 댓글/답글 신고 대상 Set
     */
    fun setReportInfo(
        reportType: ReportType,
        commentReData: CommentReData? = null,
        comtMngCd: String,
        writerMemNk: String,
        profileUrl: String
    ) {

        if (loginManager.isLogin()) {

            showReportDialog.value = CommentReportDialogItem().apply {
                this.reportType = reportType
                this.mngCd = comtMngCd
                this.commentReData = commentReData
                this.nickname = writerMemNk
                this.userProfileUrl = profileUrl
                this.isMine = loginManager.getUserLoginData().memNk == writerMemNk
            }

        }
    }

    /**
     * 댓글 삭제하기
     */
    fun onDeleteComment(reportItem: CommentReportDialogItem) {

        commentParam.value?.let {
            multiNullCheck(it.first, it.second) { type, conCode ->
                apiService.deleteComment(
                    CommentDeleteBody(
                        commentType = type.code,
                        contentsCode = conCode,
                        commentCd = reportItem.mngCd
                    )
                )
                    .applyApiScheduler()
                    .request({
                        if (it.status == HttpStatusType.SUCCESS.status) {
                            _refreshAll.call()
                        } else {
                            DLogger.d("fail Like=>${it}")
                        }
                    }, {
                        DLogger.d("Error Like=>${it.message}")
                    })
            }
        }
    }

    /**
     * 답글 삭제하기
     */
    fun onDeleteCommentRe(reportItem: CommentReportDialogItem) {
        commentParam.value?.let {
            multiNullCheck(
                it.first,
                it.second,
                reportItem.commentReData,
                reportItem.commentReData?.parentCommentData
            ) { type, conCode, curData, parentData ->

                apiService.deleteReply(
                    CommentDeleteBody(
                        commentType = type.code,
                        contentsCode = conCode,
                        commentMngCd = parentData.comtMngCd,
                        replyCd = reportItem.mngCd
                    )
                )
                    .applyApiScheduler()
                    .request({
                        if (it.status == HttpStatusType.SUCCESS.status) {

                            //상위 댓글
                            /*      parentData.replyCount = parentData.replyCount - 1
                        val childDataSize = parentData.reComments?.dataList?.size ?: -1
                        //답글 목록 삭제
                        clearChildComment(parentData)

                        deleteReComment.value = Triple(curData.parentPosition, curData.curPosition, childDataSize)

                        //카운트
                        requestCommentCount(type.code, conCode)

                        //갱신
                        refresh.value = curData.parentPosition to parentData

*/
                            _refreshAll.call()
                        } else {
                            DLogger.d("fail Like=>${it}")
                        }
                    }, {
                        DLogger.d("Error Like=>${it.message}")
                    })
            }
        }
    }

    /**
     * 유저 상세 페이지 이동
     */
    fun moveToUserDetail(userMemCd: String) {
        if (userMemCd.isEmpty()) return
        _startUserDetailPage.value = userMemCd
    }
}