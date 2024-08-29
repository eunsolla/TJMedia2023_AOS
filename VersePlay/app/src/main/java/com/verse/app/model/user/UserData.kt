package com.verse.app.model.user

import com.verse.app.contants.AppData
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.utility.DLogger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    @SerialName("jwtToken")
    var jwtToken: String = "",
    @SerialName("memCd")
    val memCd: String = "",                                         //사용자회원관리코드
    @SerialName("memNk")
    val memNk: String = "",                                         //닉네임
    @SerialName("memStCd")
    var memStCd: String = "",                                       //회원상태코드(정상/정지/휴면/탈퇴)
    @SerialName("memGrCd")
    val memGrCd: String = "",                                       //회원구매등급코드
    @SerialName("memEmail")
    val memEmail: String = "",                                      //회원이메일
    @SerialName("memTpCd")
    var memTpCd: String = "",                                       //회원유형코드(신규/무료/유료/TJ대리점/협력사/BP파트너/인플루언서)
    @SerialName("authTpCd")
    val authTpCd: String = "",                                      //회원계정인증유형코드(AU001 / AU002 / AU003 / AU004 / AU005 / AU006)
    @SerialName("authToken")
    val authToken: String = "",                                     //회원계정인증토큰(SNS인증토큰)
    @SerialName("instDesc")
    val instDesc: String = "",                                      //회원상태메시지
    @SerialName("uploadFeedCnt")
    val uploadFeedCnt: Int = 0,                                     //업로드콘텐츠수
    @SerialName("followerCnt")
    var followerCnt: Int = 0,                                       //팔로워수
    @SerialName("followingCnt")
    val followingCnt: Int = 0,                                      //팔로잉수
    @SerialName("outLinkUrl")
    val outLinkUrl: String = "",                                    //외부Link URL
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",                                   //프로필이미지경로
    @SerialName("pfBgImgPath")
    val pfBgImgPath: String = "",                                   //프로필배경이미지경로
    @SerialName("subscTpCd")
    var subscTpCd: String = "",                                     //구독이용권유형코드(SC001:3곡이용권 / SC002:30곡이용권 / SC003:100곡이용권 / SC004:월간패스 / SC005:1주일VIP이용권 / SC006:1개월VIP이용권 / SC007:6개월VIP이용권 / SC008:12개월VIP이용권 / SC009:VP쿠폰)
    @SerialName("purchPrdId")
    var purchPrdId: String = "",                                    //구매상품아이디
    @SerialName("purchToken")
    var purchToken: String = "",                                    //구매상품토큰
    @SerialName("subscMngCd")
    var subscMngCd: String = "",                                    //구독이용권관리코드
    @SerialName("fgFollowYn")
    var fgFollowYn: String = "",                                    //팔로우여부
    @SerialName("newAlrimYn")
    var newAlrimYn: String = "",                                    //새알림내역
    @SerialName("subscSongCount")
    var subscSongCount: Int = 0,                                    //현재유효곡수량
    @SerialName("subscPeriodDt")
    var subscPeriodDt: String = "",                                    //이용권유효기간
    @SerialName("fgPrivateYn")
    var fgPrivateYn: String = "",                                    //비공개 계정 여부
    @SerialName("fgBlockYn")
    var fgBlockYn: String = "",                                    //차단 대상 여부
    @SerialName("alRecAllYn")
    var alRecAllYn: String = "",                                  //push 모든 알림
    @SerialName("alRecUplPrgYn")
    var alRecUplPrgYn: String = "",                             //push 기능 알림 -업로드 진행
    @SerialName("alRecUplFailYn")
    var alRecUplFailYn: String = "",                             //push 기능 알림 -업로드 실패
    @SerialName("alRecUplComYn")
    var alRecUplComYn: String = ""                             //push 기능 알림 -업로드 완료

) {
    //인코딩/업로드 진행 노티 여부
    var isShowingUplPrg: Boolean? = null
        get() {
            if (field == null) {
                field = if (alRecAllYn == AppData.Y_VALUE) {
                    true
                } else {
                    alRecUplPrgYn == AppData.Y_VALUE
                }
            }
            DLogger.d("isShowingUplPrg=> ${alRecUplPrgYn} / ${field}")
            return field
        }

    //인코딩/업로드 결과 실패 노티 여부
    var isShowingUplFail: Boolean? = null
        get() {
            if (field == null) {
                field = if (alRecAllYn == AppData.Y_VALUE) {
                    true
                } else {
                    alRecUplFailYn == AppData.Y_VALUE
                }
            }
            return field
        }

    //인코딩/업로드 결과 성공 노티 여부
    var isShowingUplSuccess: Boolean? = null
        get() {
            if (field == null) {
                field = if (alRecAllYn == AppData.Y_VALUE) {
                    true
                } else {
                    alRecUplComYn == AppData.Y_VALUE
                }
            }
            return field
        }
    
    constructor(entity: MyPageIntentModel) : this(
        memCd = entity.memberCode,
        memNk = entity.memberName ?: "",
        pfFrImgPath = entity.memberFrImagePath ?: "",
        pfBgImgPath = entity.memberBgImagePath ?: ""
    )
}