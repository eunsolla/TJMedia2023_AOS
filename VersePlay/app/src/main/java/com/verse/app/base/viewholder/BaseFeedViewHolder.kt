package com.verse.app.base.viewholder

import android.view.View
import android.widget.Toast
import com.verse.app.R
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.CollectionType
import com.verse.app.contants.CommentType
import com.verse.app.contants.DeleteRefreshFeedList
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.MediaType
import com.verse.app.contants.SingType
import com.verse.app.databinding.ItemFeedBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentActivity
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.param.AdHistoryBody
import com.verse.app.model.param.FeedUpdateBody
import com.verse.app.model.param.FollowBody
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialogfragment.FeedMoreDialog
import com.verse.app.usecase.PostFeedBookmarkUseCase
import com.verse.app.usecase.PostFeedLikeUseCase
import com.verse.app.usecase.PostFollowUseCase
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.manager.UserFeedBlockManager
import com.verse.app.utility.manager.UserFeedBookmarkManager
import com.verse.app.utility.manager.UserFeedDeleteManager
import com.verse.app.utility.manager.UserFeedInterestManager
import com.verse.app.utility.manager.UserFeedLikeManager
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.moveToCollectionFeed
import com.verse.app.utility.moveToComment
import com.verse.app.utility.moveToLoginAct
import com.verse.app.utility.moveToSingAct
import com.verse.app.utility.moveToUserPage
import com.verse.app.utility.moveToWebView
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Description : 피드 이벤트 공통 처리
 *
 * Created by jhlee
 */
open class BaseFeedViewHolder<DATA>(
    private val itemBinding: ItemFeedBinding,
) : BaseViewHolder<DATA>(itemBinding) {

    private val loginManager: LoginManager by lazy { entryPoint.loginManager() }
    private val apiService: ApiService by lazy { entryPoint.apiService() }
    private val postFollowUseCase: PostFollowUseCase by lazy { entryPoint.postFollowUseCase() }
    private val postFeedLikeUseCase: PostFeedLikeUseCase by lazy { entryPoint.postFeedLikeUseCase() }
    private val postFeedBookmarkUseCase: PostFeedBookmarkUseCase by lazy { entryPoint.postFeedBookmarkUseCase() }
    private var disposable: Disposable? = null

    /**
     * 피드 갱신 처리하는 함수
     */
    open fun onRefreshFeed() {}
    open fun onRefreshLike() {}
    open fun onRefreshBookMark() {}
    open fun onRefreshDeleted(state:Boolean) {}

    /**
     * 로그인 체크
     */
    fun isLogin(): Boolean {
        return loginManager.isLogin()
    }

    /**
     * 로그인 체크
     */
    fun isMyFeed(targetMemCd: String): Boolean {
        return loginManager.getUserLoginData().memCd == targetMemCd
    }

    /**
     * 좋아요
     */
    fun handleLike(data: FeedContentsData) {

        // 로그인 상태 확인
        if (!isLogin()) {
            moveToLogin()
            return
        }

        if (isMyFeed(data.ownerMemCd)) {
            itemView.getFragmentActivity()?.let {
                showToastMessage(it.getString(R.string.str_can_not_like_my_feed))
            }
            return
        }

        if (UserSettingManager.isPrivateUser() && !data.isLike) {
            itemView.getFragmentActivity()?.let { act ->
                CommonDialog(act)
                    .setContents(R.string.comment_private_account_popup)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }
            return
        }

        if (disposable != null) {
            closeDisposable()
        }

        // API 호출하기전에 미리 UI 업데이트 처리, 박이사님 요청
        if (data.isLike) {
            UserFeedLikeManager.remove(data.feedMngCd, data.likeCnt)
        } else {
            UserFeedLikeManager.add(data.feedMngCd, data.likeCnt)
        }

        onRefreshLike()

        disposable = postFeedLikeUseCase(!data.isLike, data.feedMngCd, data.likeCount)
            .applyApiScheduler()
            .doOnSuccess {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    // onRefreshFeed()
                } else if (it.status == HttpStatusType.FAIL.status) {
                    itemView.getFragmentActivity()?.let { act ->
                        CommonDialog(act)
                            .setContents(it.message)
                            .setPositiveButton(R.string.str_confirm)
                            .show()
                    }
                }

            }.subscribe()
    }


    /**
     * 북마크
     */
    fun handleBookMark(data: FeedContentsData) {

        if (!isLogin()) {
            moveToLogin()
            return
        }

        if (isMyFeed(data.ownerMemCd)) {
            itemView.getFragmentActivity()?.let {
                showToastMessage(it.getString(R.string.str_can_not_like_my_book_mark))
            }
            return
        }

        if (UserSettingManager.isPrivateUser() && !data.isBookMark) {
            itemView.getFragmentActivity()?.let { act ->
                CommonDialog(act)
                    .setContents(R.string.comment_private_account_popup)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }
            return
        }

        if (disposable != null) {
            closeDisposable()
        }

        // API 호출하기전에 미리 UI 업데이트 처리, 박이사님 요청
        if (data.isBookMark) {
            UserFeedBookmarkManager.remove(data.feedMngCd)
        } else {
            UserFeedBookmarkManager.add(data.feedMngCd)
        }

        onRefreshBookMark()

        disposable = postFeedBookmarkUseCase(!data.isBookMark, data.feedMngCd)
            .applyApiScheduler()
            .doOnSuccess {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    // onRefreshFeed()
                } else if (it.status == HttpStatusType.FAIL.status) {
                    itemView.getFragmentActivity()?.let { act ->
                        CommonDialog(act)
                            .setContents(it.message)
                            .setPositiveButton(R.string.str_confirm)
                            .show()
                    }
                }
            }.subscribe()

    }

    /**
     * 듀엣/배틀 피드 팔로잉/팔로우
     */
    fun handleDuetFeedFollow(view: View, isApart: Boolean, data: FeedContentsData) {

        if (!isLogin()) {
            moveToLogin()
            return
        }

        // API 호출하기전에 미리 UI 업데이트 처리, 박이사님 요청
//        UserFollowManager.add(if (isApart) data.partAmemCd else data.partBmemCd)
//        onRefreshFeed()

        if (disposable != null) {
            closeDisposable()
        }

        disposable = postFollowUseCase(
            FollowBody(
                if (isApart) data.partAmemCd else data.partBmemCd,
                true
            )
        ).applyApiScheduler().doOnSuccess {
            if (it.status == HttpStatusType.SUCCESS.status) {
                onRefreshFeed()
            } else if (it.status == HttpStatusType.FAIL.status) {
                itemView.getFragmentActivity()?.let { act ->
                    CommonDialog(act)
                        .setContents(it.message)
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
        }.subscribe()
    }

    fun handleFeedFollow(view: View, data: FeedContentsData) {

        if (!isLogin()) {
            moveToLogin()
            return
        }

        if (loginManager.getUserLoginData().memCd == data.ownerMemCd) {
            return
        }

        // API 호출하기전에 미리 UI 업데이트 처리, 박이사님 요청
//        UserFollowManager.add(data.ownerMemCd)
//        onRefreshFeed()

        disposable = postFollowUseCase(FollowBody(data.ownerMemCd, true))
            .applyApiScheduler()
            .doOnSuccess {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    onRefreshFeed()
                } else if (it.status == HttpStatusType.FAIL.status) {
                    itemView.getFragmentActivity()?.let { act ->
                        CommonDialog(act)
                            .setContents(it.message)
                            .setPositiveButton(R.string.str_confirm)
                            .show()
                    }
                }

            }.subscribe()
    }

    fun handleUserDetail(view: View, userMemCd: String) {
        if (!isLogin()) {
            moveToLogin()
            return
        }

        moveToUserProfile(userMemCd)
    }

    /**
     * play ans pause
     */
    fun togglePlayAndPause(vm: BaseViewModel) {
        vm.togglePlayAndPause()
    }

    fun requestDeleteFeed(data: FeedContentsData, isDetail: Boolean) {

        itemView.getFragmentActivity()?.let {
            CommonDialog(it)
                .setContents(R.string.popup_feed_delete)
                .setCancelable(true)
                .setPositiveButton(R.string.str_delete)
                .setNegativeButton(R.string.str_cancel)
                .setListener(
                    object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == 1) {
                                disposable = apiService.deleteMyFeed(FeedUpdateBody(feedMngCd = data.feedMngCd))
                                        .applyApiScheduler()
                                        .doOnSuccess {
                                            if (it.status == HttpStatusType.SUCCESS.status) {
                                                onRefreshDeleted(true)
                                                refreshFeed(data, isDetail)
                                            } else {
                                                showToastMessage(it.message)
                                            }
                                        }.doOnError {
                                            showToastMessage(R.string.network_status_rs002)
                                        }.subscribe()
                            }
                        }
                    })
                .show()
        }
    }

    /**
     * 더보기
     */
    fun showMore(data: FeedContentsData, isDetail: Boolean, vm: BaseViewModel) {
        FeedMoreDialog()
            .setFeedInfo(data, isDetail)
            .setListener(
                object : FeedMoreDialog.Listener {
                    override fun onSuccess(pair: Pair<Int, Boolean>) {
                        //0 차단
                        //1 관심없음
//                        if (pair.first == 0) {
//                            data.isBlock = !data.isBlock
//                        } else {
//                            data.isNotInterested = !data.isNotInterested
//                        }
                        //차단,관심 없음 선택 시 피드 이동

                        if (pair.first == 0) {
                            if (data.isBlock) {
                                UserFeedBlockManager.remove(data.feedMngCd)
                            } else {
                                UserFeedBlockManager.add(data.feedMngCd)

                                itemView.getFragmentActivity()?.let {
                                    showToastMessage(it.getString(R.string.str_block_feed_msg))
                                }
                            }
                        } else {
                            if (data.isNotInterested) {
                                UserFeedInterestManager.remove(data.feedMngCd)
                            } else {
                                UserFeedInterestManager.add(data.feedMngCd)

                                itemView.getFragmentActivity()?.let {
                                    showToastMessage(it.getString(R.string.str_not_interested_feed))
                                }
                            }
                        }

                        onRefreshFeed()

                        if (pair.second) {
                            vm.moveToNextFeed()
                        }
                    }

                    override fun onDeleted() {
                        UserFeedDeleteManager.add(data.feedMngCd)
                        onRefreshFeed()
                        itemBinding.invalidateAll()
                        DLogger.d("삭제 업데이트 00 ${data.isDeleted}")
                    }
                })
            .show(itemView.getFragmentActivity()!!.supportFragmentManager)
    }

    /**
     * 댓글 이동
     * isAcceptReply true : 댓글 허용 , false 팝업
     */
    fun moveToComment(data: FeedContentsData) {

        itemView.getFragmentActivity()?.let {
            if (data.isAcceptComment) {
                itemView.getFragmentActivity()?.moveToComment(CommentType.FEED to data.feedMngCd)
            } else {
                CommonDialog(it)
                    .setContents(it.resources.getString(R.string.str_dialog_comment_un_acceptable))
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }
        }
    }

    /**
     * 모아보기 이동
     */
    fun moveToCollection(data: FeedContentsData) {
        itemView.getFragmentActivity()?.moveToCollectionFeed(CollectionType.FEED, data)
    }

    /**
     * 부르기 이동
     */
    fun moveToSing(data: FeedContentsData) {

        // 로그인 상태 확인
        if (!isLogin()) {
            moveToLogin()
            return
        }

        //듀엣 배틀인경우 참여 가능한지 체크
        //내 듀엣 배틀 참여 불가
        if (data.paTpCd.uppercase() == SingType.DUET.code || data.paTpCd.uppercase() == SingType.BATTLE.code) {

            //2023-11-27 바누바 제거. 비디오 타입 참가 불가
            if(data.mdTpCd ==  MediaType.VIDEO.code){
                return
            }

            if (!data.isJoin) {
                DLogger.d("종료된 배틀 ${data.orgFeedMngCd} / ${data.stBattleDt} / ${data.fnBattleDt} / ${data.baStCd} / ${data.isJoin}")
                return
            }

            if (loginManager.getUserLoginData().memCd == data.ownerMemCd) {
                return
            }
        }

        itemView.getFragmentActivity()?.moveToSingAct(data.paTpCd, data.songMngCd, data.feedMngCd, data.mdTpCd)
    }

    /**
     * 광고 웹뷰 이동
     */
    fun moveToADWabView(data: FeedContentsData) {
        if (data.feedMngCd.isEmpty() || data.adConUrl.isEmpty()) return
        apiService.insertViewClickAdContents(
            AdHistoryBody(
                adMngCd = data.feedMngCd,
                viewTime = 0,
                adConUrl = data.adConUrl
            )
        )
            .subscribeOn(Schedulers.io())
            .doOnSuccess { res -> DLogger.d("광고 클릭 이력 res-> ${res.httpStatus} ${res.status}") }
            .subscribe()

        itemView.getFragmentActivity()?.moveToWebView("", data.adConUrl)
    }

    /**
     * 메인 피드 탭 갱신
     */
    private fun refreshFeed(data: FeedContentsData) {
        DLogger.d("FeedRefreshEvent  홀더에서 ")
        RxBus.publish(
            RxBusEvent.FeedRefreshEvent(
                feedContentData = data,
                deleteRefreshFeedList = DeleteRefreshFeedList.MAIN
            )
        )
    }

    /**
     * 메인 피드 탭 갱신
     */
    private fun refreshFeed(data: FeedContentsData, isDetail: Boolean) {
        if (!isDetail) {
            refreshFeed(data)
        } else {
            DLogger.d("FeedRefreshEvent dialog 홀더에서 ")
            RxBus.publish(
                RxBusEvent.FeedRefreshEvent(
                    feedContentData = data,
                    deleteRefreshFeedList = DeleteRefreshFeedList.DETAIL
                )
            )
        }
    }

    /**
     * 로그인 이동
     */
    private fun moveToLogin() {
        itemView.getFragmentActivity()?.moveToLoginAct()
    }

    /**
     * 유저 프로필 페이지 이동
     */
    private fun moveToUserProfile(userMemCd: String) {
        itemView.getFragmentActivity()?.moveToUserPage(userMemCd)
    }

    private fun showToastMessage(msg: String) {
        itemView.getFragmentActivity()?.let {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToastMessage(res: Int) {
        itemView.getFragmentActivity()?.let {
            showToastMessage(it.resources.getString(res))
        }
    }

    /**
     * Rx 작업 하던거 취소하는 함수
     */
    fun closeDisposable() {
        disposable?.dispose()
        disposable = null
    }

}