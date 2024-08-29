package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadSettingBody(
    @SerialName("pushKey")                                  //푸쉬메시지토큰
    var pushKey: String = "",

    @SerialName("memCd")                                    //사용자회원관리코드
    var memCd: String = "",

    @SerialName("memEmail")                                 //이메일
    var memEmail: String = "",

    @SerialName("instDesc")                                 //상태메시지내용
    var instDesc: String = "",

    @SerialName("outLinkUrl")                               //외부링크URL
    var outLinkUrl: String = "",

    @SerialName("pfFrImgPath")                              //프로필이미지경로
    var pfFrImgPath: String = "",

    @SerialName("pfBgImgPath")                              //프로필배경이미지경로
    var pfBgImgPath: String = "",

    @SerialName("svcNtCd")                                  //서비스적용국가코드
    var svcNtCd: String = "",

    @SerialName("svcLangCd")                                //서비스적용언어
    var svcLangCd: String = "",

    @SerialName("prvAccYn")                                 //비공개계정여부
    var prvAccYn: String = "",

    @SerialName("recUserYn")                                //추천유저사용여부 (내계정다른사람에게추천여부)
    var recUserYn: String = "",

    @SerialName("alRecEmailYn")                             //이메일알림수신여부
    var alRecEmailYn: String = "",

    @SerialName("alRecAllYn")                               //모든푸쉬알림수신여부
    var alRecAllYn: String = "",

    @SerialName("alRecAllFeedLikeYn")                       //모든피드좋아요알림수신여부
    var alRecAllFeedLikeYn: String = "",

    @SerialName("alRecAllLikeRepYn")                        //모든내댓글좋아요알림수신여부
    var alRecAllLikeRepYn: String = "",

    @SerialName("alRecAllFlowYn")                           //모든팔로우알림수신여부
    var alRecAllFlowYn: String = "",

    @SerialName("alRecDmYn")                             //맞팔로우실시간채팅메시지알림수신여부
    var alRecDmYn: String = "",

    @SerialName("alRecAllDmYn")                             //모든실시간채팅메시지알림수신여부
    var alRecAllDmYn: String = "",

    @SerialName("alRecUplPrgYn")                           //업로드진행중알림수신여부
    val alRecUplPrgYn : String = "",

    @SerialName("alRecUplComYn")                            //업로드완료알림수신여부
    val alRecUplComYn: String = "",

    @SerialName("alRecNorEvtYn")                            //새로운이벤트알림수신여부
    val alRecNorEvtYn: String = "",

    @SerialName("alRecFnVoteYn")                            //투표마감알림수신여부
    val alRecFnVoteYn: String = "",

    @SerialName("alRecTimeYn")                              //이벤트알림가능시간설정여부
    val alRecTimeYn: String = "",

    @SerialName("alRecSuspYn")                              //회원계정정지알림수신여부
    val alRecSuspYn: String = "",

    @SerialName("alRecDorYn")                              //회원계정휴면알림수신여부
    val alRecDorYn: String = "",

    @SerialName("alRecMarketYn")                              //서비스및마케팅수신동의알림수신여부
    val alRecMarketYn: String = "",

    @SerialName("alRecFollowFeedYn")                              //나의팔로우회원새콘텐츠게시알림수신여부
    val alRecFollowFeedYn: String = "",

    @SerialName("alRecFollowConYn")                              //나의팔로우회원새글게시알림수신여부
    val alRecFollowConYn: String = "",

    @SerialName("alRecDuetComYn")                              //다른회원이나의듀엣참여완료
    val alRecDuetComYn: String = "",

    @SerialName("alRecBattleComYn")                              //다른 회원이 나의 배틀 참여 완료
    val alRecBattleComYn: String = "",

    @SerialName("alRecLoungeLikeYn")                              //다른 회원이 내게시글에 좋아요 알림 수신 여부
    val alRecLoungeLikeYn: String = "",

    @SerialName("alRecUplFailYn")                              //업로드실패알림수신여부
    val alRecUplFailYn: String = "",

    @SerialName("alRecReUplComYn")                              //업로드재시작알림수신여부
    val alRecReUplComYn: String = "",

    @SerialName("alRecSeasonYn")                              //씽패스시즌시작/마감알림수신여부
    val alRecSeasonYn: String = "",

    ) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "UploadSettingBody"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as UploadSettingBody
        return this == asItem
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as UploadSettingBody
        return this == asItem
    }

}