package com.verse.app.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.contants.CollectionType
import com.verse.app.contants.CommentType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.contants.MainEntryType
import com.verse.app.contants.NaviType
import com.verse.app.contants.SingType
import com.verse.app.contants.UserStateType
import com.verse.app.extension.getActivity
import com.verse.app.extension.isServiceRunning
import com.verse.app.extension.startAct
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.sing.SingIntentModel
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.chat.rooms.ChatRoomsActivity
import com.verse.app.ui.comment.CommentActivity
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.feed.activity.CollectionFeedActivity
import com.verse.app.ui.feed.activity.FeedDetailActivity
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.login.activity.NicknameActivity
import com.verse.app.ui.mypage.activity.MemberShipActivity
import com.verse.app.ui.mypage.activity.MyPageRootActivity
import com.verse.app.ui.mypage.activity.QNAActivity
import com.verse.app.ui.sing.activity.SingActivity
import com.verse.app.ui.song.activity.SongMainActivity
import com.verse.app.ui.webview.WebViewActivity
import com.verse.app.utility.manager.LoginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Description : 공통 Activity 이동
 *
 * Created by jhlee on 2023-02-14
 */

/**
 * 부르기 이동
 *  @param singType : 부르기 타입 (필수 값 ) 파트 선택&부르기 페이지 이동 SOLO(PA001), DUET(PA002) ,BATTLE(PA004) / group normal 제외
 * @param songMngCd : 음원 관리 코드 (필수 값 )
 * @param feedMngCd : 피드 관리 코드(듀엣 및 배틀 참여 인 경우만 / Optional)
 */
inline fun Activity.moveToSingAct(singType: String, songMngCd: String, feedMngCd: String? = "", feedMdTpCd: String? = "") {

    DLogger.d("부르기 이동!!! ${singType} / ${songMngCd} / ${feedMngCd} / ${feedMdTpCd} / ${isServiceRunning(SongEncodeService::class.java)} / ${AppData.IS_ENCODE_ING}")

    //인코딩중이면 부르기 이동 X
    if (isServiceRunning(SongEncodeService::class.java) && AppData.IS_ENCODE_ING) {
        Toast.makeText(this, getString(R.string.encode_ing_msg), Toast.LENGTH_SHORT).show()
        return
    }

    val bundle = Bundle()
    bundle.putParcelable(ExtraCode.SING_INTENT_MODEL, SingIntentModel(singType, songMngCd, feedMngCd ?: "", feedMdTpCd ?: ""))

    startAct<SingActivity>(
        R.anim.slide_up,
        R.anim.slide_down
    ) {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(ExtraCode.SINGING_SING_DATA, bundle)
    }
}

/**
 * 로그인 이동
 */
inline fun Activity.moveToLoginAct() {
    startAct<LoginActivity>(
        R.anim.slide_up,
        R.anim.slide_down
    ) {
        Intent.FLAG_ACTIVITY_CLEAR_TOP to Intent.FLAG_ACTIVITY_NEW_TASK
    }
}

/**
 * 사용자 프로필 페이지 이동
 */
inline fun Activity.moveToUserPage(userMemCd: String) {
    startAct<MyPageRootActivity>(
        enterAni = R.anim.in_right_to_left,
        exitAni = R.anim.out_right_to_left,
    ) {
        putExtra(ExtraCode.MY_PAGE_DATA, MyPageIntentModel(userMemCd))
    }
}

/**
 * 로그인 회원 상태에 따른 이동 처리
 * @param memStcd : US000 : 신규 / US001 : 정상 / US002 : 정지 / US003 : 휴면 / US004 : 탈퇴
 */
inline fun Activity.moveToUserStAct(
    context: Context,
    accountPref: AccountPref,
    loginManager: LoginManager,
    isIntro: Boolean,
    memStcd: String,
    nickName: String,
    email: String
) {
    var needLogout: Boolean = false

    when (memStcd) {
        // 비회원
        UserStateType.NON_MEMBER.code -> {
            startAct<NicknameActivity>(
                enterAni = android.R.anim.fade_in, exitAni = android.R.anim.fade_out
            ) {
                putExtra(ExtraCode.TEMP_NICK_NAME, nickName)
                putExtra(ExtraCode.TEMP_EMAIL, email)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.getActivity()?.let {
                context.getActivity()!!.finish()
            }
        }

        // 정상회원
        UserStateType.MEMBER.code -> {
            RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))

            context.getActivity()?.let {
                context.getActivity()!!.finish()
            }
        }

        // 휴면회원
        UserStateType.DORMANT.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_dormant_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 탈퇴회원
        UserStateType.WITHDRAWAL.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.str_popup_session_re_join))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_re_join))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<NicknameActivity>(
                                enterAni = android.R.anim.fade_in, exitAni = android.R.anim.fade_out
                            ) {
                                putExtra(ExtraCode.TEMP_NICK_NAME, nickName)
                                putExtra(ExtraCode.TEMP_EMAIL, email)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                DLogger.d(nickName, "nickName")
                                DLogger.d(email, "email")
                                DLogger.d(accountPref.getAuthTypeCd(), "getAuthTypeCd")
                            }

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                }
                                // Login Activity일 경우 main으로 보내는 처리 x
//                                else {
//                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
//                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 영구정지회원
        UserStateType.SUSPEND.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_suspend_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {

                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 1일정지회원
        UserStateType.ONE_DAY_SUSPEND.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_one_day_suspend_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 1개월정지회원
        UserStateType.ONE_MONTH_SUSPEND.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_one_month_suspend_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 3개월정지회원
        UserStateType.THREE_MONTH_SUSPEND.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_three_month_suspend_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 6개월정지회원
        UserStateType.SIX_MONTH_SUSPEND.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_six_month_suspend_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }

        // 12개월정지회원
        UserStateType.ONE_YEAR_SUSPEND.code -> {
            needLogout = true

            CommonDialog(context)
                .setContents(getString(R.string.member_status_one_year_suspend_notice))
                .setCancelable(false)
                .setIcon(AppData.POPUP_WARNING)
                .setPositiveButton(getString(R.string.str_counsel))
                .setNegativeButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (needLogout) {
                            loginManager.logout(context)
                        }

                        if (which == CommonDialog.POSITIVE) {
                            startAct<QNAActivity>()

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                if (isIntro) {
                                    RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                                } else {
                                    RxBus.publish(RxBusEvent.NaviEvent(type = NaviType.MAIN, isRefresh = true))
                                }
                            }
                        }
                        context.getActivity()?.let {
                            context.getActivity()!!.finish()
                        }
                    }
                }).show()
        }
    }
}

/**
 * 댓글 이동
 *  @param  type : 콘텐츠 유형(F:피드 / L:커뮤니티라운지 / V:커뮤니티투표)
 *   @param mngCd : 콘텐츠관리코드(피드관리코드/라운지관리코드/투표관리코드)
 */
inline fun Activity.moveToComment(commentParam: Pair<CommentType, String>) {
    startAct<CommentActivity>() {
        putExtra(ExtraCode.COMMENT_TYPE, commentParam)
    }
}

/**
 * 모아보기 이동
 */
inline fun Activity.moveToCollectionFeed(type: CollectionType, data: FeedContentsData) {
    startAct<CollectionFeedActivity>(
        enterAni = R.anim.in_right_to_left,
        exitAni = R.anim.out_right_to_left,
    ) {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(ExtraCode.COLLECTION_TYPE, type.code)
        putExtra(ExtraCode.COLLECTION_FEED_PARAM, data)
    }
}

/**
 * 웹뷰 이동
 */
inline fun Activity.moveToWebView(title: String, url: String) {
    startAct<WebViewActivity>(
        enterAni = R.anim.in_right_to_left,
        exitAni = R.anim.out_right_to_left,
    ) {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(WebViewActivity.WEB_VIEW_INTENT_DATA_TYPE, WebViewActivity.WEB_VIEW_INTENT_VALUE_URL)
        putExtra(WebViewActivity.WEB_VIEW_INTENT_TITLE, title)
        putExtra(WebViewActivity.WEB_VIEW_INTENT_URL, url)
    }
}

/**
 * 공유 링크 페이지 이동 처리
 */
inline fun FragmentActivity.moveToLinkPage(code: String, linkinfo: String = "") {
    DLogger.d("moveToLinkPage code=>${code}  ,linkinfo => ${linkinfo}")
    when (LinkMenuTypeCode.from(code)) {

        LinkMenuTypeCode.LINK_SONG -> {
            startAct<SongMainActivity>() {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        LinkMenuTypeCode.LINK_FEED_MAIN -> {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.MAIN))
        }

        LinkMenuTypeCode.LINK_SING_PASS -> {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.SING_PASS))
        }

        LinkMenuTypeCode.LINK_MY_PAGE -> {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.MY))
        }

        LinkMenuTypeCode.LINK_COMMUNITY_VOTE -> {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.COMMUNITY, false, 2))
        }

        LinkMenuTypeCode.LINK_URL,
        LinkMenuTypeCode.LINK_COMMUNITY_EVENT -> {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.COMMUNITY, false, 1))
        }

        LinkMenuTypeCode.LINK_COMMUNITY_LOUNGE -> {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.COMMUNITY, false, 0))
        }

        LinkMenuTypeCode.LINK_MEMBERSHIP -> {
            startAct<MemberShipActivity>()
        }

        LinkMenuTypeCode.LINK_FEED_CONTENTS -> {
            if (linkinfo.isEmpty()) return

            startAct<FeedDetailActivity> {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ExtraCode.FEED_MNG_CD, linkinfo)
            }
        }

        LinkMenuTypeCode.LINK_MESSAGE_ROOM -> {
            startAct<ChatRoomsActivity> {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        LinkMenuTypeCode.LINK_USER_MYPAGE -> {
            startAct<MyPageRootActivity> {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                // linkinfo에 내려오는 memcd로 이동
                putExtra(ExtraCode.MY_PAGE_DATA, MyPageIntentModel(linkinfo))
            }
        }

        else -> {
            DLogger.d("else moveToLinkPage $code")
        }
    }
}
