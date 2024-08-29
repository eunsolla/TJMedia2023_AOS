package com.verse.app.ui.mypage.viewmodel

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.PushType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetSettingInfoData
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingPushNotiViewModel @Inject constructor(
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val apiService: ApiService,
    val loginManager: LoginManager
) : ActivityViewModel() {
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val isPushBtnAllOn: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                 // 모든 버튼이 다 눌렸는지 판단값
    val isNotificationOn: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 알림 권한 허용/비허용 여부
    val showDialog: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val accountPrefOff: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 알림 권한 비허용 시 pref n으로 바꾸게끔 하는 플래그 값

    val all_push: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }         //모든알림
    val push_uploading: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }   // 업로드 진행
    val push_upload_fail: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 업로드 실패
    val push_upload_complete: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 업로드 완료
    val push_dormant_user: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 휴면 알림
    val push_stop_user: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 정지 알림
    val push_marketing: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }  // 마케팅 수신 재동의
    val push_new_event: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 새로운 이벤트 알림
    val push_vote: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 투표 시작 마감 알림
    val push_season: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 시즌 시작 종료 알림
    val push_follow_me: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 다른 회원이 나를 팔로우
    val like_my_contents: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 다른 회원이 내 콘텐츠에 좋아요
    val like_my_comments: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 다른 회원이 내 게시물에 좋아요
    val like_my_post: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 다른 회원이 내 댓글 답글에 좋ㅇ요
    val complete_my_duet: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 다른 회원이 나의 듀엣 참여 완료
    val complete_my_battle: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 다른 회원이 나의 배틀 참여 완료
    val my_follower_new_contents: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 나의 팔로우 회원 새 콘텐츠 게시
    val my_follower_new_post: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 나의 팔로우 회원 세 글 게시
    val receive_my_follower: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 친구에게만 메시지 받기
    val get_message: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 메시지 도착
    val manner_mode_description: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) } // 야간 푸시 알림 동의

    //[s] 푸시 알림 플래그
    var alRecAllYnF = "" //모든푸쉬알림수신여부
    var alRecUplPrgYnF = "" //업로드진행중알림수신여부
    var alRecUplFailYnF = "" //업로드실패알림수신여부
    var alRecUplComYnF = "" //업로드완료알림수신여부
    var alRecDorYnF = "" //회원계정휴면알림수신여부
    var alRecSuspYnF = "" //회원계정정지알림수신여부
    var alRecMarketYnF = "" //서비스및마케팅수신동의알림수신여부
    var alRecNorEvtYnF = "" //새로운이벤트알림수신여부
    var alRecFnVoteYnF = "" //투표시작/마감알림수신여부
    var alRecSeasonYnF = "" //씽패스시즌시작/마감알림수신여부
    var alRecAllFlowYnF = "" //모든팔로우알림수신여부
    var alRecAllFeedLikeYnF = "" // 모든피드좋아요알림수신여부
    var alRecLoungeLikeYnF = ""     //다른 회원이 내게시글에 좋아요 알림 수신 여부
    var alRecAllLikeRepYnF = "" //모든내댓글(답글)좋아요알림수신여부
    var alRecDuetComYnF = "" //다른 회원이 나의 듀엣 참여 완료
    var alRecBattleComYnF = "" //다른 회원이 나의 배틀 참여 완료
    var alRecFollowFeedYnF = "" //나의팔로우회원새콘텐츠게시알림수신여부
    var alRecFollowConYnF = "" //나의팔로우회원새글게시알림수신여부
    var alRecDmYnF = ""         // 친구만 메세지 받기
    var alRecAllDmYnF = "" //모든실시간채팅메시지알림수신여부
    var alRecTimeYnF = "" //이벤트알림가능시간설정여부
    //[e] 푸시 알림 플래그


    fun back() {
        backpress.call()
    }

    fun onClickPush(type: PushType) {
        if (!isNotificationOn.value) {
            showDialog.call()
            return
        }

        when (type) {
            PushType.PUSH_ALLOW_ALL -> {
                all_push.value = !all_push.value

                // 전부 켜짐
                if (all_push.value) {
                    all_push.value = true
                    push_uploading.value = true
                    push_upload_fail.value = true
                    push_upload_complete.value = true
                    push_dormant_user.value = true
                    push_stop_user.value = true
                    push_marketing.value = true
                    push_new_event.value = true
                    push_vote.value = true
                    push_season.value = true
                    push_follow_me.value = true
                    like_my_contents.value = true
                    like_my_comments.value = true
                    like_my_post.value = true
                    complete_my_duet.value = true
                    complete_my_battle.value = true
                    my_follower_new_contents.value = true
                    my_follower_new_post.value = true
                    receive_my_follower.value = true
                    get_message.value = true
                    manner_mode_description.value = true

                } else {
                    all_push.value = false
                    push_uploading.value = false
                    push_upload_fail.value = false
                    push_upload_complete.value = false
                    push_dormant_user.value = false
                    push_stop_user.value = false
                    push_marketing.value = false
                    push_new_event.value = false
                    push_vote.value = false
                    push_season.value = false
                    push_follow_me.value = false
                    like_my_contents.value = false
                    like_my_comments.value = false
                    like_my_post.value = false
                    complete_my_duet.value = false
                    complete_my_battle.value = false
                    my_follower_new_contents.value = false
                    my_follower_new_post.value = false
                    receive_my_follower.value = false
                    get_message.value = false
                    manner_mode_description.value = false
                }

            }

            PushType.PUSH_UPLOADING -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_uploading.value = !push_uploading.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_UPLOAD_FAIL -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_upload_fail.value = !push_upload_fail.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_UPLOAD_COMPLETE -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_upload_complete.value = !push_upload_complete.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_UPLOAD_RELOAD -> {
            }

            PushType.PUSH_DORMANT_USER -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_dormant_user.value = !push_dormant_user.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_STOP_USER -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_stop_user.value = !push_stop_user.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_MARKETING_ALLOW -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_marketing.value = !push_marketing.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_NEW_EVENT -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_new_event.value = !push_new_event.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_VOTE -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_vote.value = !push_vote.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_SEASON -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_season.value = !push_season.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_FOLLOW_ME -> {
                if (all_push.value) {
                    all_push.value = false
                }
                push_follow_me.value = !push_follow_me.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_LIKE_MY_CONTENTS -> {
                if (all_push.value) {
                    all_push.value = false
                }
                like_my_contents.value = !like_my_contents.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_LIKE_MY_POSTS -> {
                if (all_push.value) {
                    all_push.value = false
                }
                like_my_post.value = !like_my_post.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_LIKE_MY_COMMENT -> {
                if (all_push.value) {
                    all_push.value = false
                }
                like_my_comments.value = !like_my_comments.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_COMPLETE_MY_DUET -> {
                if (all_push.value) {
                    all_push.value = false
                }
                complete_my_duet.value = !complete_my_duet.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_COMPLETE_MY_BATTLE -> {
                if (all_push.value) {
                    all_push.value = false
                }
                complete_my_battle.value = !complete_my_battle.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_MY_FOLLOWERS_NEW_CONTENTS -> {
                if (all_push.value) {
                    all_push.value = false
                }
                my_follower_new_contents.value = !my_follower_new_contents.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_MY_FOLLOWERS_NEW_POSTS -> {
                if (all_push.value) {
                    all_push.value = false
                }
                my_follower_new_post.value = !my_follower_new_post.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_RECEIVE_MY_FOLLOWER -> {
                if (all_push.value) {
                    all_push.value = false
                }
                receive_my_follower.value = !receive_my_follower.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_GET_DM -> {
                if (all_push.value) {
                    all_push.value = false
                }
                if (!get_message.value && !receive_my_follower.value) {
                    receive_my_follower.value = true
                }
                get_message.value = !get_message.value
                isPushBtnAllOn.call()
            }

            PushType.PUSH_MANNER_MODE_ALLOW -> {
                if (all_push.value) {
                    all_push.value = false
                }
                manner_mode_description.value = !manner_mode_description.value
                isPushBtnAllOn.call()
            }
        }
    }

    fun setPushFlag() {
        alRecAllYnF = if (all_push.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecUplPrgYnF = if (push_uploading.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecUplFailYnF = if (push_upload_fail.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecUplComYnF = if (push_upload_complete.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecDorYnF = if (push_dormant_user.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecSuspYnF = if (push_stop_user.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecMarketYnF = if (push_marketing.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecNorEvtYnF = if (push_new_event.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecFnVoteYnF = if (push_vote.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecSeasonYnF = if (push_season.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecAllFlowYnF = if (push_follow_me.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecAllFeedLikeYnF = if (like_my_contents.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecLoungeLikeYnF = if (like_my_post.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecAllLikeRepYnF = if (like_my_comments.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecBattleComYnF = if (complete_my_battle.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecDuetComYnF = if (complete_my_duet.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecFollowFeedYnF = if (my_follower_new_contents.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecFollowConYnF = if (my_follower_new_post.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecAllDmYnF = if (get_message.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecDmYnF = if (receive_my_follower.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        alRecTimeYnF = if (manner_mode_description.value) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }
    }

    fun onComplete() {
        setPushFlag()
        setPushAPI()
    }

    fun setPushAPI() {
        apiService.updateSettings(
            UploadSettingBody(
                alRecAllYn = alRecAllYnF,
                alRecUplPrgYn = alRecUplPrgYnF,
                alRecUplFailYn = alRecUplFailYnF,
                alRecUplComYn = alRecUplComYnF,
                alRecDorYn = alRecDorYnF,
                alRecSuspYn = alRecSuspYnF,
                alRecMarketYn = alRecMarketYnF,
                alRecNorEvtYn = alRecNorEvtYnF,
                alRecFnVoteYn = alRecFnVoteYnF,
                alRecSeasonYn = alRecSeasonYnF,
                alRecAllFlowYn = alRecAllFlowYnF,
                alRecAllFeedLikeYn = alRecAllFeedLikeYnF,
                alRecLoungeLikeYn = alRecLoungeLikeYnF,
                alRecAllLikeRepYn = alRecAllLikeRepYnF,
                alRecDuetComYn = alRecDuetComYnF,
                alRecBattleComYn = alRecBattleComYnF,
                alRecFollowFeedYn = alRecFollowFeedYnF,
                alRecFollowConYn = alRecFollowConYnF,
                alRecDmYn = alRecDmYnF,
                alRecAllDmYn = alRecAllDmYnF,
                alRecTimeYn = alRecTimeYnF
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    AppData.IS_MYPAGE_EDIT = true

                    loginManager.getUserLoginData().alRecAllYn = alRecAllYnF
                    loginManager.getUserLoginData().alRecUplPrgYn = alRecUplPrgYnF
                    loginManager.getUserLoginData().alRecUplFailYn = alRecUplFailYnF
                    loginManager.getUserLoginData().alRecUplComYn = alRecUplComYnF
                    backpress.call()
                }
            }, {
                DLogger.d("Error 비공개 => ${it.message}")
            })
    }

    fun updateAPI() {
        apiService.updateSettings(
            UploadSettingBody(
                alRecAllYn = alRecAllYnF,
                alRecUplPrgYn = alRecUplPrgYnF,
                alRecUplFailYn = alRecUplFailYnF,
                alRecUplComYn = alRecUplComYnF,
                alRecDorYn = alRecDorYnF,
                alRecSuspYn = alRecSuspYnF,
                alRecMarketYn = alRecMarketYnF,
                alRecNorEvtYn = alRecNorEvtYnF,
                alRecFnVoteYn = alRecFnVoteYnF,
                alRecSeasonYn = alRecSeasonYnF,
                alRecAllFlowYn = alRecAllFlowYnF,
                alRecAllFeedLikeYn = alRecAllFeedLikeYnF,
                alRecLoungeLikeYn = alRecLoungeLikeYnF,
                alRecAllLikeRepYn = alRecAllLikeRepYnF,
                alRecDuetComYn = alRecDuetComYnF,
                alRecBattleComYn = alRecBattleComYnF,
                alRecFollowFeedYn = alRecFollowFeedYnF,
                alRecFollowConYn = alRecFollowConYnF,
                alRecDmYn = alRecDmYnF,
                alRecAllDmYn = alRecAllDmYnF,
                alRecTimeYn = alRecTimeYnF
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    accountPrefOff.call()

                    loginManager.getUserLoginData().alRecAllYn = alRecAllYnF
                    loginManager.getUserLoginData().alRecUplPrgYn = alRecUplPrgYnF
                    loginManager.getUserLoginData().alRecUplFailYn = alRecUplFailYnF
                    loginManager.getUserLoginData().alRecUplComYn = alRecUplComYnF
                }
            }, {
                DLogger.d("Error 비공개 => ${it.message}")
            })
    }

    fun setPushToggle(my: GetSettingInfoData) {

        if (isNotificationOn.value){
            if (my.alRecAllYn == AppData.Y_VALUE) {
                all_push.value = true
                alRecAllYnF = AppData.Y_VALUE
            } else {
                all_push.value = false
                alRecAllYnF = AppData.N_VALUE
            }

            if (my.alRecUplPrgYn == AppData.Y_VALUE) {
                push_uploading.value = true
                alRecUplPrgYnF = AppData.Y_VALUE
            } else {
                push_uploading.value = false
                alRecUplPrgYnF = AppData.N_VALUE
            }

            if (my.alRecUplFailYn == AppData.Y_VALUE) {
                push_upload_fail.value = true
                alRecUplFailYnF = AppData.Y_VALUE
            } else {
                push_upload_fail.value = false
                alRecUplFailYnF = AppData.N_VALUE
            }

            if (my.alRecUplComYn == AppData.Y_VALUE) {
                push_upload_complete.value = true
                alRecUplComYnF = AppData.Y_VALUE
            } else {
                push_upload_complete.value = false
                alRecUplComYnF = AppData.N_VALUE
            }

            if (my.alRecDorYn == AppData.Y_VALUE) {
                push_dormant_user.value = true
                alRecDorYnF = AppData.Y_VALUE
            } else {
                push_dormant_user.value = false
                alRecDorYnF = AppData.N_VALUE
            }

            if (my.alRecSuspYn == AppData.Y_VALUE) {
                push_stop_user.value = true
                alRecSuspYnF = AppData.Y_VALUE
            } else {
                push_stop_user.value = false
                alRecSuspYnF = AppData.N_VALUE
            }

            if (my.alRecMarketYn == AppData.Y_VALUE) {
                push_marketing.value = true
                alRecMarketYnF = AppData.Y_VALUE
            } else {
                push_marketing.value = false
                alRecMarketYnF = AppData.N_VALUE
            }

            if (my.alRecNorEvtYn == AppData.Y_VALUE) {
                push_new_event.value = true
                alRecNorEvtYnF = AppData.Y_VALUE
            } else {
                push_new_event.value = false
                alRecNorEvtYnF = AppData.N_VALUE
            }

            if (my.alRecFnVoteYn == AppData.Y_VALUE) {
                push_vote.value = true
                alRecFnVoteYnF = AppData.Y_VALUE
            } else {
                push_vote.value = false
                alRecFnVoteYnF = AppData.N_VALUE
            }

            if (my.alRecSeasonYn == AppData.Y_VALUE) {
                push_season.value = true
                alRecSeasonYnF = AppData.Y_VALUE
            } else {
                push_season.value = false
                alRecSeasonYnF = AppData.N_VALUE
            }

            if (my.alRecAllFlowYn == AppData.Y_VALUE) {
                push_follow_me.value = true
                alRecAllFlowYnF = AppData.Y_VALUE
            } else {
                push_follow_me.value = false
                alRecAllFlowYnF = AppData.N_VALUE
            }

            if (my.alRecAllFeedLikeYn == AppData.Y_VALUE) {
                like_my_contents.value = true
                alRecAllFeedLikeYnF = AppData.Y_VALUE
            } else {
                like_my_contents.value = false
                alRecAllFeedLikeYnF = AppData.N_VALUE
            }

            if (my.alRecLoungeLikeYn == AppData.Y_VALUE) {
                like_my_post.value = true
                alRecLoungeLikeYnF = AppData.Y_VALUE
            } else {
                like_my_post.value = false
                alRecLoungeLikeYnF = AppData.N_VALUE
            }

            if (my.alRecAllLikeRepYn == AppData.Y_VALUE) {
                like_my_comments.value = true
                alRecAllLikeRepYnF = AppData.Y_VALUE
            } else {
                like_my_comments.value = false
                alRecAllLikeRepYnF = AppData.N_VALUE
            }

            if (my.alRecDuetComYn == AppData.Y_VALUE) {
                complete_my_duet.value = true
                alRecDuetComYnF = AppData.Y_VALUE
            } else {
                complete_my_duet.value = false
                alRecDuetComYnF = AppData.N_VALUE
            }

            if (my.alRecBattleComYn == AppData.Y_VALUE) {
                complete_my_battle.value = true
                alRecBattleComYnF = AppData.Y_VALUE
            } else {
                complete_my_battle.value = false
                alRecBattleComYnF = AppData.N_VALUE
            }

            if (my.alRecFollowFeedYn == AppData.Y_VALUE) {
                my_follower_new_contents.value = true
                alRecFollowFeedYnF = AppData.Y_VALUE
            } else {
                my_follower_new_contents.value = false
                alRecFollowFeedYnF = AppData.N_VALUE
            }

            if (my.alRecFollowConYn == AppData.Y_VALUE) {
                my_follower_new_post.value = true
                alRecFollowConYnF = AppData.Y_VALUE
            } else {
                my_follower_new_post.value = false
                alRecFollowConYnF = AppData.N_VALUE
            }

            if (my.alRecDmYn == AppData.Y_VALUE) {
                receive_my_follower.value = true
                alRecDmYnF = AppData.Y_VALUE
            } else {
                receive_my_follower.value = false
                alRecDmYnF = AppData.N_VALUE
            }

            if (my.alRecAllDmYn == AppData.Y_VALUE) {
                get_message.value = true
                alRecAllDmYnF = AppData.Y_VALUE
            } else {
                get_message.value = false
                alRecAllDmYnF = AppData.N_VALUE
            }

            if (my.alRecTimeYn == AppData.Y_VALUE) {
                manner_mode_description.value = true
                alRecTimeYnF = AppData.Y_VALUE
            } else {
                manner_mode_description.value = false
                alRecTimeYnF = AppData.N_VALUE
            }

        } else {
            all_push.value = false
            alRecAllYnF = AppData.N_VALUE

            push_uploading.value = false
            alRecUplPrgYnF = AppData.N_VALUE

            push_upload_fail.value = false
            alRecUplFailYnF = AppData.N_VALUE

            push_upload_complete.value = false
            alRecUplComYnF = AppData.N_VALUE

            push_dormant_user.value = false
            alRecDorYnF = AppData.N_VALUE

            push_stop_user.value = false
            alRecSuspYnF = AppData.N_VALUE

            push_marketing.value = false
            alRecMarketYnF = AppData.N_VALUE

            push_new_event.value = false
            alRecNorEvtYnF = AppData.N_VALUE

            push_vote.value = false
            alRecFnVoteYnF = AppData.N_VALUE

            push_season.value = false
            alRecSeasonYnF = AppData.N_VALUE

            push_follow_me.value = false
            alRecAllFlowYnF = AppData.N_VALUE

            like_my_contents.value = false
            alRecAllFeedLikeYnF = AppData.N_VALUE

            like_my_post.value = false
            alRecLoungeLikeYnF = AppData.N_VALUE

            like_my_comments.value = false
            alRecAllLikeRepYnF = AppData.N_VALUE

            complete_my_duet.value = false
            alRecDuetComYnF = AppData.N_VALUE

            complete_my_battle.value = false
            alRecBattleComYnF = AppData.N_VALUE

            my_follower_new_contents.value = false
            alRecFollowFeedYnF = AppData.N_VALUE

            my_follower_new_post.value = false
            alRecFollowConYnF = AppData.N_VALUE

            receive_my_follower.value = false
            alRecDmYnF = AppData.N_VALUE

            get_message.value = false
            alRecAllDmYnF = AppData.N_VALUE

            manner_mode_description.value = false
            alRecTimeYnF = AppData.N_VALUE

            updateAPI()
        }

    }
}