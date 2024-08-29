package com.verse.app.model.feed

import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 업로드, 좋아요, 즐겨찾기 콘텐츠일경우 Data
 *
 * Created by esna on 2023-05-05
 */

@Serializable
data class CommonContentsData(
    @SerialName("feedMngCd")
    val feedMngCd: String = "",                   // 피드관리코드
    @SerialName("paTpCd")
    val paTpCd: String = "",                      // 피드파트유형코드(PA001:솔로/PA002:듀엣/PA003:그룹/PA004:배틀/PA005:일반영상/PA006:광고)
    @SerialName("mdTpCd")
    val mdTpCd: String = "",                      // 피드미디어유형코드(녹화/녹음)
    @SerialName("singTpCd")
    val singTpCd: String = "",                    // 부르기유형코드(전체부르기/구간부르기)
    @SerialName("songMngCd")
    val songMngCd: String = "",                   // 서비스음원관리코드
    @SerialName("songId")
    val songId: String = "",                      // 서비스음원곡아이디
    @SerialName("ownerMemCd")
    val ownerMemCd: String = "",                  // 생성자회원관리코드
    @SerialName("ownerMemNk")
    val ownerMemNk: String = "",                  // 생성자회원닉네임
    @SerialName("thumbPicPath")
    val thumbPicPath: String = "",                // 썸네일이미지경로
    @SerialName("orgConPath")
    val orgConPath: String = "",                  // 업로드콘텐츠경로
    @SerialName("highConPath")
    val highConPath: String = "",                 // 하이라이트콘텐츠경로
    @SerialName("audioConPath")
    val audioConPath: String = "",                // 오디오콘텐츠경로
    @SerialName("fgHighlightYn")
    val fgHighlightYn: String = "",               // 하이라이트여부
    @SerialName("orgFeedMngCd")
    val orgFeedMngCd: String = "",                // 원작배틀피드관리코드
    @SerialName("stBattleDt")
    val stBattleDt: String = "",                  // 배틀시작일시
    @SerialName("fnBattleDt")
    val fnBattleDt: String = "",                  // 배틀종료일시
    @SerialName("baStCd")
    val baStCd: String = "",                      // 배틀진행상태코드
    @SerialName("secStartTime")
    val secStartTime: String = "",                // 구간부르기시작시간
    @SerialName("secEndTime")
    val secEndTime: String = "",                  // 구간부르기종료시간
    @SerialName("singPart")
    val singPart: String = "",                    // 부르기파트(A/B/T)
    @SerialName("singScore")
    val singScore: String = "",                   // 부르기점수
    @SerialName("feedNote")
    val feedNote: String = "",                    // 피드설명
    @SerialName("feedTag")
    val feedTag: String = "",                     // 피드태그
    @SerialName("hitCnt")
    val hitCnt: String = "",                     // 사용자조회수
    @SerialName("fgAplRepYn")
    val fgAplRepYn: String = "",                  // 댓글허용여부
    @SerialName("exposCd")
    val exposCd: String = "",                     // 콘텐츠노출설정코드(나만허용/친구허용/전체허용)
    @SerialName("fgLikeYn")
    var fgLikeYn: String = "",                    // 좋아요 선택 여부
    @SerialName("fgBookMarkYn")
    var fgBookMarkYn: String = "",                // 즐겨찾기 선택 여부
    @SerialName("fgFollowYn")
    val fgFollowYn: String = "",                  // 팔로잉 여부
    @SerialName("replyCount")
    val replyCount: Int = 0,                      // 댓글수
    @SerialName("likeCount")
    val likeCount: Int = 0,                       // 좋아요수
    @SerialName("songNm")
    val songNm: String = "",                      // 노래제목
    @SerialName("artNm")
    val artNm: String = "",                       // 가수명
    @SerialName("albImgPath")
    val albImgPath: String = "",                  // 음원앨범아트이미지경로
    @SerialName("partApfImgPath")
    val partApfImgPath: String = "",              // A파트생성자프로필이미지경로
    @SerialName("partBpfImgPath")
    val partBpfImgPath: String = "",              // B파트생성자프로필이미지경로
    @SerialName("partAmemCd")
    val partAmemCd: String = "",                  // A파트생성자회원관리코드
    @SerialName("partBmemCd")
    val partBmemCd: String = "",                  // B파트생성자회원관리코드
    @SerialName("partAfgFollowYn")
    val partAfgFollowYn: String = "",             // A파트생성자원팔로잉여부
    @SerialName("partBfgFollowYn")
    val partBfgFollowYn: String = "",             // B파트생성자원팔로잉여부

) : BaseModel() {

    //좋아요
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

    //즐겨찾기
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
    var isBlock: Boolean = false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    //관심업음 on/off
    var isInterested: Boolean = false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "CommonContentsData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CommonContentsData
        return this.feedMngCd == asItem.feedMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CommonContentsData
        return this == asItem
    }
}