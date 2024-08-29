package com.verse.app.model.feed

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/26
 */
data class MyPageFeedContentsData(
    val trackingTag : String = "", // 해당 데이터 모델이 만들어지는 위치
    val feedMngCd: String = "",                   // 피드관리코드
    val paTpCd: String = "",                      // 피드파트유형코드(PA001:솔로/PA002:듀엣/PA003:그룹/PA004:배틀/PA005:일반영상/PA006:광고)
    val mdTpCd: String = "",                      // 피드미디어유형코드(녹화/녹음)
    val singTpCd: String = "",                       // 부르기유형코드(전체부르기/구간부르기)
    val songMngCd: String = "",                  // 서비스음원관리코드
    val songId: String = "",                          // 서비스음원곡아이디
    val ownerMemCd: String = "",                    // 생성자회원관리코드
    val ownerMemNk: String = "",                    // 생성자회원닉네임
    val ownerFrImgPath: String = "",               // 생성자 프로필 사진
    val ownerBgImgPath: String = "",               // 생성자 프로필 배경 사진
    val thumbPicPath: String = "",                  // 썸네일이미지경로
    val orgConPath: String = "",                    // 업로드콘텐츠경로
    val highConPath: String = "",                   // 하이라이트콘텐츠경로
    val audioConPath: String = "",                  // 오디오콘텐츠경로
    val fgHighlightYn: String = "",                 // 하이라이트여부
    val orgFeedMngCd: String = "",                  // 원작배틀피드관리코드
    val stBattleDt: String = "",                      // 배틀시작일시
    val fnBattleDt: String = "",                       // 배틀종료일시
    val baStCd: String = "",                           // 배틀진행상태코드
    val secStartTime: String = "",                  // 구간부르기시작시간
    val secEndTime: String = "",                    // 구간부르기종료시간
    val singPart: String = "",                     // 부르기파트(A/B/T)
    val singScore: String = "",                    // 부르기점수
    val feedNote: String = "",                     // 피드설명
    val feedTag: String = "",                    // 피드태그
    val hitCnt: String = "",                      // 사용자조회수
    val fgAplRepYn: String = "",                    // 댓글허용여부
    val exposCd: String = "",                      // 콘텐츠노출설정코드(나만허용/친구허용/전체허용)
    var fgLikeYn: String = "",                   // 좋아요 선택 여부
    var fgBookMarkYn: String = "",                  // 즐겨찾기 선택 여부
    val fgFollowYn: String = "",                    // 팔로잉 여부
    val replyCount: Int = 0,                    // 댓글수
    var likeCount: Int = 0,                        // 좋아요수
    val songNm: String = "",                          // 노래제목
    val artNm: String = "",                           // 가수명
    val albImgPath: String = "",                    // 음원앨범아트이미지경로
    val adConUrl: String = "",                        // 광고 연결 URL
    val partApfImgPath: String = "",              // A파트생성자프로필이미지경로
    val partBpfImgPath: String = "",              // B파트생성자프로필이미지경로
    val partAmemCd: String = "",                  // A파트생성자회원관리코드
    val partBmemCd: String = "",                  // B파트생성자회원관리코드
    val partAfgFollowYn: String = "",             // A파트생성자원팔로잉여부
    val partBfgFollowYn: String = "",             // B파트생성자원팔로잉여부
    var fgSongBookMarkYn: String = "",             // 반주음 즐겨찾기 여부
) : BaseModel() {

    constructor(tag: String,item: FeedContentsData) : this(
        trackingTag = tag,
        feedMngCd = item.feedMngCd,
        paTpCd = item.paTpCd,
        mdTpCd = item.mdTpCd,
        singTpCd = item.singTpCd,
        songMngCd = item.songMngCd,
        songId = item.songId,
        ownerMemCd = item.ownerMemCd,
        ownerMemNk = item.ownerMemNk,
        ownerFrImgPath = item.ownerFrImgPath,
        ownerBgImgPath = item.ownerBgImgPath,
        thumbPicPath = item.thumbPicPath,
        orgConPath = item.orgConPath,
        highConPath = item.highConPath,
        audioConPath = item.audioConPath,
        fgHighlightYn = item.fgHighlightYn,
        orgFeedMngCd = item.orgFeedMngCd,
        stBattleDt = item.stBattleDt,
        fnBattleDt = item.fnBattleDt,
        baStCd = item.baStCd,
        secStartTime = item.secStartTime,
        secEndTime = item.secEndTime,
        singPart = item.singPart,
        singScore = item.singScore,
        feedNote = item.feedNote,
        feedTag = item.feedTag,
        hitCnt = item.hitCnt,
        fgAplRepYn = item.fgAplRepYn,
        exposCd = item.exposCd,
        fgLikeYn = item.fgLikeYn,
        fgBookMarkYn = item.fgBookMarkYn,
        fgFollowYn = item.fgFollowYn,
        replyCount = item.replyCount,
        likeCount = item.likeCount,
        songNm = item.songNm,
        artNm = item.artNm,
        albImgPath = item.albImgPath,
        adConUrl = item.adConUrl,
        partApfImgPath = item.partApfImgPath,
        partBpfImgPath = item.partBpfImgPath,
        partAmemCd = item.partAmemCd,
        partBmemCd = item.partBmemCd,
        partAfgFollowYn = item.partAfgFollowYn,
        partBfgFollowYn = item.partBfgFollowYn,
        fgSongBookMarkYn = item.fgSongBookMarkYn
    )

    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_MY_PAGE_FEED
    }

    override fun getClassName(): String {
        return "MyPageFeedContentsData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is MyPageFeedContentsData) {
            feedMngCd == diffUtil.feedMngCd
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is MyPageFeedContentsData) {
            this == diffUtil
        } else {
            false
        }
    }

    fun toFeedContentsData(): FeedContentsData {
        return FeedContentsData(
            feedMngCd = this.feedMngCd,
            paTpCd = this.paTpCd,
            mdTpCd = this.mdTpCd,
            singTpCd = this.singTpCd,
            songMngCd = this.songMngCd,
            songId = this.songId,
            ownerMemCd = this.ownerMemCd,
            ownerMemNk = this.ownerMemNk,
            ownerFrImgPath = this.ownerFrImgPath,
            ownerBgImgPath = this.ownerBgImgPath,
            thumbPicPath = this.thumbPicPath,
            orgConPath = this.orgConPath,
            highConPath = this.highConPath,
            audioConPath = this.audioConPath,
            fgHighlightYn = this.fgHighlightYn,
            orgFeedMngCd = this.orgFeedMngCd,
            stBattleDt = this.stBattleDt,
            fnBattleDt = this.fnBattleDt,
            baStCd = this.baStCd,
            secStartTime = this.secStartTime,
            secEndTime = this.secEndTime,
            singPart = this.singPart,
            singScore = this.singScore,
            feedNote = this.feedNote,
            feedTag = this.feedTag,
            hitCnt = this.hitCnt,
            fgAplRepYn = this.fgAplRepYn,
            exposCd = this.exposCd,
            fgLikeYn = this.fgLikeYn,
            fgBookMarkYn = this.fgBookMarkYn,
            fgFollowYn = this.fgFollowYn,
            replyCount = this.replyCount,
            likeCount = this.likeCount,
            songNm = this.songNm,
            artNm = this.artNm,
            albImgPath = this.albImgPath,
            adConUrl = this.adConUrl,
            partApfImgPath = this.partApfImgPath,
            partBpfImgPath = this.partBpfImgPath,
            partAmemCd = this.partAmemCd,
            partBmemCd = this.partBmemCd,
            partAfgFollowYn = this.partAfgFollowYn,
            partBfgFollowYn = this.partBfgFollowYn,
            fgSongBookMarkYn = this.fgSongBookMarkYn
        )
    }
}
