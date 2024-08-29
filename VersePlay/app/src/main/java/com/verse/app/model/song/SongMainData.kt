package com.verse.app.model.song

import android.os.Parcelable
import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.SITType
import com.verse.app.model.base.BaseModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 곡 정보 Data
 *
 * Created by jhlee on 2023-04-27
 */
@Parcelize
@Serializable
data class SongMainData(

    @SerialName("songMngCd")
    val songMngCd: String = "",                   //음원관리코드
    @SerialName("songId")
    val songId: String = "",                         //음원아이디(곡번호)
    @SerialName("songNm")
    val songNm: String = "",                  //음원명
    @SerialName("totTime")
    val totTime: String = "",                 //재생시간
    @SerialName("hiTime")
    val hiTime: String = "",                  //하이라이트재생시간
    @SerialName("genreCd")
    val genreCd: String = "",                 //장르코드
    @SerialName("saleDt")
    val saleDt: String = "",                  //발매일자
    @SerialName("svcStCd")
    val svcStCd: String = "",                 //서비스상태코드(SV001:정상 / SV002:정지)
    @SerialName("svcStUpdDt")
    val svcStUpdDt: String = "",                  //서비스상태마지막업데이트일시
    @SerialName("cprCd")
    val cprCd: String = "",                   //음원저작권코드(CP001:UMPG / CP002:SME / CP003:WMG / CP004:KOMCA)
    @SerialName("norSdScPath")
    val norSdScPath: String = "",                 //일반음원파일경로
    @SerialName("sdScPath")
    val sdScPath: String = "",                    //반주음원파일경로
    @SerialName("sdCtgTpCd")
    val sdCtgTpCd: String = "",                   //반주음원유형코드(SIT01:TJ반주음원 / SIT02:외부반주음원 / SIT03:사용자반주음원)
    @SerialName("subCon")
    val subCon: String = "",                  //가사정보
    @SerialName("orgSubPath")
    val orgSubPath: String = "",                  //가사원본파일경로
    @SerialName("relKwd")
    val relKwd: String = "",                  //이니셜검색어(연관검색어)
    @SerialName("albNm")
    val albNm: String = "",                   //앨범명
    @SerialName("albDistNm")
    val albDistNm: String = "",                   //앨범아트유통사명
    @SerialName("albDivNm")
    val albDivNm: String = "",                    //앨범아트소속사명
    @SerialName("albRelNtNm")
    val albRelNtNm: String = "",                  //앨범발매국가
    @SerialName("artNm")
    val artNm: String = "",                   //아티스트명
    @SerialName("artTpCd")
    val artTpCd: String = "",                 //아티스트유형(성별)
    @SerialName("artDesc")
    val artDesc: String = "",                 //아티스트설명(구분)
    @SerialName("artRetDt")
    val artRetDt: String = "",                    //아티스트은퇴일자
    @SerialName("artDebDt")
    val artDebDt: String = "",                    //아티스트데뷔일자
    @SerialName("artFandomNm")
    val artFandomNm: String = "",                 //아티스트팬덤명
    @SerialName("artFandomImgPath")
    val artFandomImgPath: String = "",                    //아티스트팬덤이미지경로
    @SerialName("artFandomColNm")
    val artFandomColNm: String = "",                  //팬덤공식색상명
    @SerialName("artFandomColPath")
    val artFandomColPath: String = "",                    //팬덤공식색상이미지경로
    @SerialName("singCnt")
    val singCnt: Int = 0,                 //음원부르기완료수
    @SerialName("hitCnt")
    val hitCnt: Int = 0,                   //음원조회수
    @SerialName("artNtNm")
    val artNtNm: String = "",                 //아티스트국가
    @SerialName("artBirthday")
    val artBirthday: String = "",                 //아티스트생년월일
    @SerialName("artImgPath")
    val artImgPath: String = "",                  //아티스트이미지파일경로
    @SerialName("albImgPath")
    val albImgPath: String = "",                  //앨범아트이미지파일경로
    @SerialName("jsonSubPath")
    val jsonSubPath: String = "",                 //가사JSON파일경로
    @SerialName("songCtgTpCd1")
    val songCtgTpCd1: String = "",                    //음원곡유형1(SO001:기본 / SO002:히트곡 / SO003:타이틀곡 / SO004:19세이상)
    @SerialName("songCtgTpCd2")
    val songCtgTpCd2: String = "",                    //음원곡유형2(SO001:기본 / SO002:히트곡 / SO003:타이틀곡 / SO004:19세이상)
    @SerialName("fgSoloYn")
    val fgSoloYn: String = "",                    //솔로지원여부
    @SerialName("fgDuetYn")
    val fgDuetYn: String = "",                    //듀엣지원여부
    @SerialName("fgGroupYn")
    val fgGroupYn: String = "",                   //그룹지원여부
    @SerialName("fgBattleYn")
    val fgBattleYn: String = "",                  //배틀지원여부
    @SerialName("apvStCd")
    val apvStCd: String = "",                 //음원승인관리코드
    @SerialName("svcNtCd")
    val svcNtCd: String = "",                 //적용대상국가코드
    @SerialName("showPr")
    val showPr: String = "",                  //서비스우선순위가중치
    @SerialName("comNm")
    val comNm: String = "",                  //작곡가
    @SerialName("lyrNm")
    val lyrNm: String = "",                    //작사가
    @SerialName("orgConPath")
    val orgConPath: String = "",            //원본피드영상파일경로(듀엣 및 배틀인 경우)
    @SerialName("audioConPath")
    val audioConPath: String = "",          //원본피드오디오파일경로(듀엣 및 배틀인 경우)
    @SerialName("singPart")
    val singPart: String = "",                 //원본피드부르기파트(A / B)(듀엣 및 배틀인 경우)
    @SerialName("orgFeedMngCd")
    val orgFeedMngCd: String = "",        //원본피드관리코드(듀엣 및 배틀인 경우)
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",            //프로필이미지경로
    @SerialName("relateSongYn")
    val relateSongYn: String = "",           //연관음원존재여부
    @SerialName("fgSingPassYn")
    val fgSingPassYn: String = "",           //씽패스도전여부
    @SerialName("singPassMngCd")
    val singPassMngCd: String = "",          //씽패스시즌관리코드 (씽패스시즌 도전 가능할 경우)
    @SerialName("missionMngCd")
    val missionMngCd: String = "",           //씽패스시즌미션관리코드 (씽패스시즌 도전 가능할 경우)
    @SerialName("availSongCount")
    val availSongCount: Int = 0,           //노래부르기가능횟수
    @SerialName("fgPaidYn")
    val fgPaidYn: String = "",                   //유료 회원 여부

) : Parcelable, BaseModel() {

    //솔로 지원 여부
    var isSolo: Boolean = false
        get() {
            return fgSoloYn == AppData.Y_VALUE
        }

    //듀엣 지원 여부
    var isDuet: Boolean = false
        get() {
            return fgDuetYn == AppData.Y_VALUE
        }

    //배틀 지원 여부
    var isBattle: Boolean = false
        get() {
            return fgBattleYn == AppData.Y_VALUE
        }

    //싱패스 도전 여부
    var isSingPass: Boolean = false
        get() {
            return fgSingPassYn == AppData.Y_VALUE
        }

    //미션 도전 가능 여부
    var isMission: Boolean = false
        get() {
            return !missionMngCd.isNullOrEmpty()
        }

    //TJ 반주 음원 여부
    var isTJSound: Boolean = false
        get() {
            return sdCtgTpCd == SITType.TJ_SOUND_SOURCE.code
        }


    // 사용자 음원 여부
    var isUserSound: Boolean = false
        get() {
            return sdCtgTpCd == SITType.USER_SOUND_SOURCE.code
        }

    //연관음원 존재 여부
    var isRlateSong: Boolean = false
        get() {
            return relateSongYn == AppData.Y_VALUE
        }

    //VIP  여부
    var isPaid: Boolean = false
        get() {
            return fgPaidYn == AppData.Y_VALUE
        }

    //곡 남은 횟수
    var isAvailSong: Boolean = false
        get() {
            return availSongCount != 0
        }

    //완곡 가능 여부
    var isWholeSong: Boolean = false
        get() {
            return if (isPaid) {
                return true
            } else {
                return isAvailSong
            }
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SongMainData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SongMainData
        return this.songMngCd == asItem.songMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SongMainData
        return this == asItem
    }
}