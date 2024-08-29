package com.verse.app.model.feed

import android.os.Parcelable
import com.verse.app.contants.AppData
import com.verse.app.contants.BattleStatusType
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.ShowContentsType
import com.verse.app.contants.SingType
import com.verse.app.extension.getDateToMilliseconds
import com.verse.app.model.base.BaseModel
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date


/**
 * Description : Recommend / Following Data
 *
 * Created by jhlee on 2023-04-13
 */
@Parcelize
@Serializable
data class FeedContentsData(

    @SerialName("feedMngCd")
    val feedMngCd: String = "",                   // 피드관리코드
    @SerialName("paTpCd")
    val paTpCd: String = "",                      // 피드파트유형코드(PA001:솔로/PA002:듀엣/PA003:그룹/PA004:배틀/PA005:일반영상/PA006:광고)
    @SerialName("mdTpCd")
    val mdTpCd: String = "",                      // 피드미디어유형코드(녹화/녹음)
    @SerialName("singTpCd")
    val singTpCd: String = "",                       // 부르기유형코드(전체부르기/구간부르기)
    @SerialName("songMngCd")
    val songMngCd: String = "",                  // 서비스음원관리코드
    @SerialName("songId")
    val songId: String = "",                          // 서비스음원곡아이디
    @SerialName("ownerMemCd")
    val ownerMemCd: String = "",                    // 생성자회원관리코드
    @SerialName("ownerMemNk")
    val ownerMemNk: String = "",                    // 생성자회원닉네임
    @SerialName("ownerFrImgPath")
    val ownerFrImgPath: String = "",               // 생성자 프로필 사진
    @SerialName("ownerBgImgPath")
    val ownerBgImgPath: String = "",               // 생성자 프로필 배경 사진
    @SerialName("thumbPicPath")
    val thumbPicPath: String = "",                  // 썸네일이미지경로
    @SerialName("orgConPath")
    val orgConPath: String = "",                    // 업로드콘텐츠경로
    @SerialName("highConPath")
    val highConPath: String = "",                   // 하이라이트콘텐츠경로
    @SerialName("audioConPath")
    val audioConPath: String = "",                  // 오디오콘텐츠경로
    @SerialName("fgHighlightYn")
    val fgHighlightYn: String = "",                 // 하이라이트여부
    @SerialName("orgFeedMngCd")
    val orgFeedMngCd: String = "",                  // 원작배틀피드관리코드
    @SerialName("stBattleDt")
    val stBattleDt: String = "",                      // 배틀시작일시
    @SerialName("fnBattleDt")
    val fnBattleDt: String = "",                       // 배틀종료일시
    @SerialName("baStCd")
    val baStCd: String = "",                           // 배틀진행상태코드
    @SerialName("secStartTime")
    val secStartTime: String = "",                  // 구간부르기시작시간
    @SerialName("secEndTime")
    val secEndTime: String = "",                    // 구간부르기종료시간
    @SerialName("singPart")
    val singPart: String = "",                     // 부르기파트(A/B/T)
    @SerialName("singScore")
    val singScore: String = "",                    // 부르기점수
    @SerialName("feedNote")
    val feedNote: String = "",                     // 피드설명
    @SerialName("feedTag")
    val feedTag: String = "",                    // 피드태그
    @SerialName("hitCnt")
    var hitCnt: String = "",                      // 사용자조회수
    @SerialName("fgAplRepYn")
    var fgAplRepYn: String = "",                    // 댓글허용여부
    @SerialName("exposCd")
    var exposCd: String = "",                      // 콘텐츠노출설정코드(나만허용/친구허용/전체허용)
    @SerialName("fgLikeYn")
    var fgLikeYn: String = "",                   // 좋아요 선택 여부
    @SerialName("fgBookMarkYn")
    var fgBookMarkYn: String = "",                  // 즐겨찾기 선택 여부
    @SerialName("fgFollowYn")
    var fgFollowYn: String = "",                    // 팔로잉 여부
    @SerialName("replyCount")
    var replyCount: Int = 0,                    // 댓글수
    @SerialName("likeCount")
    var likeCount: Int = 0,                        // 좋아요수
    @SerialName("songNm")
    val songNm: String = "",                          // 노래제목
    @SerialName("artNm")
    val artNm: String = "",                           // 가수명
    @SerialName("albImgPath")
    val albImgPath: String = "",                    // 음원앨범아트이미지경로
    @SerialName("adConUrl")
    val adConUrl: String = "",                        // 광고 연결 URL
    @SerialName("partApfImgPath")
    val partApfImgPath: String = "",              // A파트생성자프로필이미지경로
    @SerialName("partBpfImgPath")
    val partBpfImgPath: String = "",              // B파트생성자프로필이미지경로
    @SerialName("partAmemCd")
    val partAmemCd: String = "",                  // A파트생성자회원관리코드
    @SerialName("partBmemCd")
    val partBmemCd: String = "",                  // B파트생성자회원관리코드
    @SerialName("partAfgFollowYn")
    var partAfgFollowYn: String = "",             // A파트생성자원팔로잉여부
    @SerialName("partBfgFollowYn")
    var partBfgFollowYn: String = "",             // B파트생성자원팔로잉여부
    @SerialName("fgSongBookMarkYn")
    var fgSongBookMarkYn: String = "",             // 반주음 즐겨찾기 여부
    @SerialName("songDelYn")
    var songDelYn: String = "",             // 음원서비스정지여부(Y:서비스정지 / N:정상서비스중)
    @SerialName("fgDelYn")
    var fgDelYn: String = "N",                     //삭제여부
) : BaseModel(), Parcelable {

    @IgnoredOnParcel
    var isDualMic: Boolean = false
        get() {
            var result: Boolean = false

            if (paTpCd.uppercase() == SingType.DUET.code || paTpCd.uppercase() == SingType.BATTLE.code) {
                result = true
            }

            return result
        }


    @IgnoredOnParcel
    var isOwerFollow: Boolean = false
        get() {
            return fgFollowYn == AppData.Y_VALUE
        }
        set(value) {
            fgFollowYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    @IgnoredOnParcel
    var followApartYn: Boolean = false
        get() {
            return partAfgFollowYn == AppData.Y_VALUE
        }
        set(value) {
            partAfgFollowYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    @IgnoredOnParcel
    var followBpartYn: Boolean = false
        get() {
            return partBfgFollowYn == AppData.Y_VALUE
        }
        set(value) {
            partBfgFollowYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    //좋아요
    @IgnoredOnParcel
    var isLike: Boolean = false
        get() {
            return fgLikeYn == AppData.Y_VALUE
        }
        set(value) {
            fgLikeYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    @IgnoredOnParcel
    var likeCnt: Int = 0
        get() {
            var result = 0

            if (fgLikeYn == AppData.Y_VALUE && likeCount == 0) {
                result = 1
            } else {
                result = likeCount
            }

            return result
        }
        set(value) {
            likeCount = value
            field = value
        }

    //즐겨찾기
    @IgnoredOnParcel
    var isBookMark: Boolean = false
        get() {
            return fgBookMarkYn == AppData.Y_VALUE
        }
        set(value) {
            fgBookMarkYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    //차단 on/off
    @IgnoredOnParcel
    var isBlock: Boolean = false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    //관심업음 on/off
    @IgnoredOnParcel
    var isNotInterested: Boolean = false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    //좋아요
    @IgnoredOnParcel
    var isSongBookMark: Boolean = false
        get() {
            return fgSongBookMarkYn == AppData.Y_VALUE
        }
        set(value) {
            fgSongBookMarkYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    @IgnoredOnParcel
    var isFollowingContentsLast: Boolean = false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    /**
     * 댓글 허용 여부
     */
    @IgnoredOnParcel
    var isAcceptComment: Boolean = false
        get() {
            return fgAplRepYn == AppData.Y_VALUE
        }
        set(value) {
            fgAplRepYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    /**
     * 컨텐츠 공개/비공개 여부
     * 나만 허용이 아니면 공개라 간주 함
     */
    @IgnoredOnParcel
    var isPublicContents: Boolean = false
        get() {
            return exposCd != ShowContentsType.PRIVATE.code
        }

    /**
     * 음원 서비스 정지 여부
     */
    @IgnoredOnParcel
    var isSongDelYn: Boolean = false
        get() {
            return songDelYn == AppData.Y_VALUE
        }

    /**
     * 컨텐츠 삭제 여부
     */
    @IgnoredOnParcel
    var isDeleted: Boolean = false
        get() {
            return fgDelYn == AppData.Y_VALUE
        }
        set(value) {
            field = value
        }

    /**
     * 광고/일반 피드 URL구분
     */
    @IgnoredOnParcel
    val feedUrl: String
        get() {
            return if (paTpCd == SingType.AD.code) orgConPath else highConPath
        }

    /**
     * 배틀/듀엣 여부
     */
    @IgnoredOnParcel
    var isDuetOrBattle: Boolean = false
        get() {
            return paTpCd.uppercase() == SingType.DUET.code || paTpCd.uppercase() == SingType.BATTLE.code
        }

    /**
     * 배틀 가능 여부
     */
    @IgnoredOnParcel
    var isJoinBattle: Boolean = false
        get() {
            val battleMilisec = fnBattleDt.getDateToMilliseconds()
            val localMilisec = System.currentTimeMillis()
            return baStCd == BattleStatusType.BS001.name && localMilisec <= battleMilisec
        }

    /**
     * 배틀 /듀엣 참여 가능 여부
     */
    @IgnoredOnParcel
    var isJoin: Boolean = false
        get() {
            return if (paTpCd.uppercase() == SingType.BATTLE.code) {
                isJoinBattle
            } else if (paTpCd.uppercase() == SingType.DUET.code) {
                orgFeedMngCd.isEmpty()
            } else {
                return false
            }
        }

    @IgnoredOnParcel
    var isFeedContents: Boolean = false
        get() {
            return paTpCd.uppercase() == SingType.SOLO.code || paTpCd.uppercase() == SingType.DUET.code || paTpCd.uppercase() == SingType.BATTLE.code
        }

    @IgnoredOnParcel
    var isMikeVisible: Boolean? = null
        get() {
            if (field == null) {
                field = if (paTpCd.equals(SingType.AD.code, true)) {
                    false
                } else !paTpCd.equals(SingType.NORMAL.code, true)
            }
            return field
        }

    @IgnoredOnParcel
    var isMore: Boolean = false

    @IgnoredOnParcel
    var noteLineCount: Int = -1

    @IgnoredOnParcel
    var note: CharSequence? = null
        get() {
            if (field == null) {
                val str = StringBuilder()
                if (feedNote.isNotEmpty()) {
                    str.append(feedNote)
                }

                if (feedTag.isNotEmpty()) {
                    str.append("\n")
                    str.append(feedTag)
                }

                field = str
            }
            return field
        }

    @IgnoredOnParcel
    var isAniStarting: Boolean = false

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "FeedContentsData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is FeedContentsData) {
            this.feedMngCd == diffUtil.feedMngCd
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is FeedContentsData) {
            this == diffUtil
        } else {
            false
        }
    }
}