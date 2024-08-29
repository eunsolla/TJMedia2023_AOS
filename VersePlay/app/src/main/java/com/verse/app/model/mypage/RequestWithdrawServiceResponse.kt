package com.verse.app.model.mypage

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestWithdrawServiceResponse (
    val result: RequestWithdrawServiceData,
) : BaseResponse()

@Serializable
data class RequestWithdrawServiceData(
    @SerialName("userCode")
    val userCode: String = "",                      //사용자회원관리코드

    @SerialName("pushToken")
    val pushToken: String = "",                     //푸쉬메시지토큰

    @SerialName("statusMessage")
    val statusMessage: String = "",                 //상태메시지내용

    @SerialName("outLinkUrl")
    val outLinkUrl: String = "",                    //외부링크URL

    @SerialName("profileImagePath")
    val profileImagePath: String = "",              //프로필이미지경로

    @SerialName("profileBgPath")
    val profileBgPath: String = "",                 //프로필배경이미지경로

    @SerialName("nationCode")
    val nationCode: String = "",                    //서비스적용국가코드

    @SerialName("languageCode")
    val languageCode: String = "",                  //서비스적용언어

    @SerialName("privateAccount")
    val privateAccount: String = "",                //비공개계정여부

    @SerialName("recommendUser")
    val recommendUser: String = "",                 //추천유저사용여부(내계정다른사람에게추천여부)

    @SerialName("autoVolume")
    val autoVolume: String = "",                    //볼륨자동조절여부

    @SerialName("autoVolumeValue")
    val autoVolumeValue: String = "",               //볼륨자동조절값

    @SerialName("runMute")
    val runMute: String = "",                       //앱실행시볼륨음소거여부

    @SerialName("recEmail")
    val recEmail: String = "",                      //이메일알림수신여부

    @SerialName("recAllPush")
    val recAllPush: String = "",                    //모든푸쉬알림수신여부

    @SerialName("recFeedLike")
    val recFeedLike: String = "",                   //맞팔로우피드좋아요알림수신여부

    @SerialName("recAllFeedLike")
    val recAllFeedLike: String = "",                //모든피드좋아요알림수신여부

    @SerialName("recFeedReply")
    val recFeedReply: String = "",                  //맞팔로우피드댓글알림수신여부

    @SerialName("recAllFeedReply")
    val recAllFeedReply: String = "",               //모든피드댓글알림수신여부

    @SerialName("recReplyLike")
    val recReplyLike: String = "",                  //맞팔로우내댓글좋아요알림수신여부

    @SerialName("recAllReplyLike")
    val recAllReplyLike: String = "",               //모든내댓글좋아요알림수신여부

    @SerialName("recFollow")
    val recFollow: String = "",                     //맞팔로우팔로우알림수신여부

    @SerialName("recAllFollow")
    val recAllFollow: String = "",                  //모든팔로우알림수신여부

    @SerialName("recChatMsg")
    val recChatMsg: String = "",                    //맞팔로우실시간채팅메시지알림수신여부

    @SerialName("recAllChatMsg")
    val recAllChatMsg: String = "",                 //모든실시간채팅메시지알림수신여부

    @SerialName("recUpload")
    val recUpload: String = "",                     //업로드진행중알림수신여부

    @SerialName("recUploadCompleted")
    val recUploadCompleted: String = "",            //업로드완료알림수신여부

    @SerialName("recNewEvent")
    val recNewEvent: String = "",                   //새로운이벤트알림수신여부

    @SerialName("recResultEvent")
    val recResultEvent: String = "",                //이벤트당첨자알림수신여부

    @SerialName("recExpVote")
    val recExpVote: String = "",                    //투표마감알림수신여부

    @SerialName("recUpdateTerms")
    val recUpdateTerms: String = "",                //약관업데이트알림수신여부

    @SerialName("recPreCheckSvc")
    val recPreCheckSvc: String = "",                //정기점검사전안내알림수신여부

    @SerialName("recComCheckSvc")
    val recComCheckSvc: String = "",                //정기점검완료안내알림수신여부

    @SerialName("recNotice")
    val recNotice: String = "",                     //앱서비스공지안내알림수신여부

    @SerialName("recEvtSetTime")
    val recEvtSetTime: String = "",                 //이벤트알림가능시간설정여부

    @SerialName("recEvtStartTime")
    val recEvtStartTime: String = "",               //이벤트알림가능시작시간

    @SerialName("recEvtFinishTime")
    val recEvtFinishTime: String = "",              //이벤트알림가능종료시간
)