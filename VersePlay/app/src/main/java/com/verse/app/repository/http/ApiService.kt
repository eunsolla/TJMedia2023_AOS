package com.verse.app.repository.http

import com.verse.app.contants.AppData
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.chat.ChatMemberRoomInfo
import com.verse.app.model.chat.ChatMemberRoomsResponse
import com.verse.app.model.chat.ChatMessagesResponse
import com.verse.app.model.comment.CommentReResponse
import com.verse.app.model.comment.CommentRepCountResponse
import com.verse.app.model.comment.CommentResponse
import com.verse.app.model.common.CheckSystemNotPopupResponse
import com.verse.app.model.common.GetResourcePathResponse
import com.verse.app.model.common.NoticeResponse
import com.verse.app.model.common.VersionResponse
import com.verse.app.model.community.CommunityEventResponse
import com.verse.app.model.community.CommunityLoungeResponse
import com.verse.app.model.community.CommunityMainBannerResponse
import com.verse.app.model.community.CommunityVoteResponse
import com.verse.app.model.events.EventDetailResponse
import com.verse.app.model.feed.FeedContentsResponse
import com.verse.app.model.feed.GetMyBookMarkAccompanimentResponse
import com.verse.app.model.lounge.LoungeDetailResponse
import com.verse.app.model.mypage.*
import com.verse.app.model.param.*
import com.verse.app.model.report.ReportResponse
import com.verse.app.model.search.SearchMainResponse
import com.verse.app.model.search.SearchResponse
import com.verse.app.model.singpass.SingPassDashBoardResponse
import com.verse.app.model.singpass.SingPassMissionResponse
import com.verse.app.model.singpass.SingPassMissionSkipResponse
import com.verse.app.model.singpass.SingPassRankingListResponse
import com.verse.app.model.singpass.SingPassResponse
import com.verse.app.model.singpass.SingPassUserInfoResponse
import com.verse.app.model.song.SongListResponse
import com.verse.app.model.song.SongMainResponse
import com.verse.app.model.user.*
import com.verse.app.model.videoupload.UploadFeedBody
import com.verse.app.model.videoupload.UploadFeedResponse
import com.verse.app.model.vote.VoteBody
import com.verse.app.model.vote.VoteDetailResponse
import com.verse.app.model.vote.VoteResultResponse
import com.verse.app.model.feed.FollowingContentsResponse
import com.verse.app.model.song.SongInfoResponse
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * API Class
 * fetch{api_name]():Single<Response>
 * fetch{api_name]List():Single<Response>
 */
interface ApiService {

    /*버전 정보 조회*/
    @GET("common/checkProhibitWord")
    fun checkProhibitWord(
        @Query("contents") contents: String,
    ): Single<BaseResponse>

    /*금칙어 포함 여부 확인*/
    @GET("common/getAppVersionInfo")
    fun fetchVersions(
        @Query("osType") osType: String = AppData.OS,
    ): Single<VersionResponse>

    /*메인 추천 피드 정보 조회*/
    @GET("contents/getRecommendContents")
    fun fetchRecommendContents(
        @Query("pageNum") pageNum: Int,
    ): Single<FeedContentsResponse>

    /*메인 팔로잉(Feed) 피드 정보 조회*/
    @GET("contents/getFollowingContents")
    fun fetchFollowingFeed(
        @Query("pageNum") pageNum: Int,
    ): Single<FollowingContentsResponse>

    /*좋아요 설정/해제*/
    @POST("contents/updateLike")
    fun updateLike(
        @Body likeBody: LikeBody,
    ): Single<BaseResponse>

    /*즐겨찾기 설정/해제*/
    @POST("contents/updateBookMark")
    fun updateBookMark(
        @Body bookMarkBody: BookMarkBody,
    ): Single<BaseResponse>

    /*신고하기*/
    @POST("common/requestReport")
    fun requestReport(
        @Body reportParam: ReportParam,
    ): Single<BaseResponse>

    /*신고하기 카테고리 정보 조회*/
    @GET("common/getReportCategoryInfo")
    fun fetchReportCategoryInfo(): Single<ReportResponse>

    /*차단 설정/해제*/
    @POST("common/updateBlock")
    fun updateBlock(
        @Body blockBody: BlockBody,
    ): Single<BaseResponse>

    /*콘텐츠 관심 없음 설정/해제*/
    @POST("common/updateUninterested")
    fun updateUninterested(
        @Body unInterestedBody: UnInterestedBody,
    ): Single<BaseResponse>

    /*사용자 정보 조회 (본인)*/
    @GET("profile/getMyProfileInfo")
    fun fetchMyProfileInfo(): Single<UserInfoResponse>

    /*사용자 프로필 정보 조회(타인)*/
    @GET("profile/getUserProfileInfo")
    fun fetchUserProfileInfo(
        @Query("memCd") memCd: String,
    ): Single<UserInfoResponse>

    /*댓글/답글 총 집계 수 조회*/
    @GET("contents/getComRepCount")
    fun fetchComRepCount(
        @QueryMap(encoded = true) queryMap: Map<String, String>
    ): Single<CommentRepCountResponse>

    /*콘텐츠 유형 별 댓글 목록 조회*/
    @GET("contents/getCommentList")
    fun fetchCommentList(
        @QueryMap commentParam: MutableMap<String, Any>,
    ): Single<CommentResponse>

    /*콘텐츠 유형 별 댓글 작성*/
    @POST("contents/updateComment")
    fun updateComment(
        @Body commentUpdateParam: CommentUpdateParam
    ): Single<BaseResponse>

    /*콘텐츠 유형 별 답글 작성 */
    @POST("contents/updateReply")
    fun updateReply(
        @Body commentUpdateParam: CommentUpdateParam
    ): Single<BaseResponse>

    /*콘텐츠 유형 별 답글 조회*/
    @GET("contents/getReplyList")
    fun fetchReplyList(
        @QueryMap commentParam: MutableMap<String, Any>,
    ): Single<CommentReResponse>

    /*콘텐츠 유형 별 댓글 삭제 */
    @POST("contents/deleteComment")
    fun deleteComment(
        @Body commentDeleteBody: CommentDeleteBody
    ): Single<BaseResponse>

    /*콘텐츠 유형 별 답글 삭제 */
    @POST("contents/deleteReply")
    fun deleteReply(
        @Body commentDeleteBody: CommentDeleteBody
    ): Single<BaseResponse>

    /*파일 다운로드 */
    @GET
    fun fetchDownloadFile(
        @Url fileUrl: String,
    ): Single<ResponseBody>

    @POST("api/setPushToken")
    fun requestSetPush(
        @Body body: PushTokenData,
    ): Single<BaseResponse>

    @POST("api/checkRegMember")
    open fun checkRegMember(
        @Body checkRegBody: CheckRegData,
    ): Single<CheckRegMemberResponse>

    // 토큰 인증 요청
    @POST("login/requestTokenLogin")
    fun requestTokenLogin(
        @Body body: RequestTokenLoginBody,
    ): Single<UserLoginResponse>

    // 닉네임 자동 생성 요청
    @GET("login/requestCreateNick")
    fun requestCreateNickName(
    ): Single<AutoCreateNickNameResponse>

    // nick name 중목검사
    @POST("login/checkRegNickName")
    fun checkRegNickName(
        @Body body: CheckRegNickNameBody,
    ): Single<CheckRegNickNameResponse>

    // 회원가입 요청
    @POST("login/registerMember")
    fun registerMember(
        @Body body: RegisterMemberBody,
    ): Single<UserLoginResponse>

    // 피드업로드
    @POST("contents/uploadFeed")
    fun uploadFeed(
        @Body body: UploadFeedBody,
    ): Single<UploadFeedResponse>

    /*개인 설정 정보 저장 요청*/
    @POST("setting/updateSettings")
    fun updateSettings(
        @Body updateSettingsBody: UploadSettingBody,
    ): Single<BaseResponse>

    /*개인 설정 정보 조회*/
    @GET("setting/getSettingInfo")
    fun getSettingInfo(): Single<GetSettingInfoResponse>

    /*친구 관리(차단계정) 목록 조회*/
    @GET("setting/getBlockUserList")
    fun getBlockUserList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetBlockUserListResponse>

    /*로그인 이력 조회*/
    @GET("setting/getRecentLoginHistory")
    fun getRecentLoginHistory(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetRecentLoginHistoryResponse>

    /*FAQ 카테고리 목록 조회*/
    @GET("setting/getFaqCategoryList")
    fun getFaqCategoryList(): Single<GetFaqCategoryListResponse>

    /*FAQ 카테고리 목록 조회*/
    @GET("setting/getFaqList")
    fun getFaqCategorySubList(
        @Query("bctgMngCd") bctgMngCd: String
    ): Single<GetFaqLastCategoryListResponse>

    /*FAQ 상세 정보 조회*/
    @GET("setting/getDetailFaqInfo")
    fun getDetailFaqInfo(
        @Query("ctgCode") ctgCode: String
    ): Single<GetDetailFaqInfoResponse>

    /*1대1 문의 카테고리 목록 조회*/
    @GET("setting/getInquiryCategoryList")
    fun getInquiryCategoryList(): Single<GetInquiryCategoryListResponse>

    /*1:1 문의 내역 조회*/
    @GET("setting/getInquiryList")
    fun getInquiryList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetInquiryListResponse>

    /*1:1 문의 상세 정보 조회*/
    @GET("setting/getDetailInquiryInfo")
    fun getDetailInquiryInfo(
        @Query("inqCode") inqCode: String,
    ): Single<GetDetailInquiryInfoResponse>

    /*공지사항 목록 조회*/
    @GET("setting/getNoticeList")
    fun getNoticeList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetNoticeListResponse>

    /*공지사항 상세 정보 조회*/
    @GET("setting/getDetailNoticeInfo")
    fun getDetailNoticeInfo(
        @Query("notCode") notCode: String,
    ): Single<GetDetailNoticeInfoResponse>

    /*약관 및 정책 목록 조회*/
    @GET("setting/getTermList")
    fun getTermList(
    ): Single<GetTermsListResponse>

    /*약관 및 정책 상세 정보 조회*/
    @GET("setting/getDetailTermsInfo")
    fun getDetailTermsInfo(
        @Query("ctgCode") ctgCode: String,
        @Query("termsTpCd") termsTpCd: String,
    ): Single<GetDetailTermsInfoResponse>

    /*오픈라이선스 상세 정보 조회*/
    @GET("setting/getOpensourceLicenseInfo")
    fun getOpensourceLicenseInfo(): Single<GetOpensourceLicenseInfoResponse>

    /*쿠폰 사용 요청*/
    @POST("setting/requestUseCoupon")
    fun requestUseCoupon(
        @Body body: RequestUseCouponBody,
    ): Single<RequestUseCouponResponse>

    /*서비스국가 및 언어 목록 조회*/
    @GET("setting/getNationLanguageList")
    fun getNationLanguageList(
        @Query("reqType") reqType: String
    ): Single<GetNationLanguageListResponse>

    /*회원탈퇴 요청*/
    @POST("setting/requestWithdrawService")
    fun requestWithdrawService(): Single<BaseResponse>

    /*비공개 콘텐츠 조회(본인)*/
    // dataType -> 요청 피드 유형(P:개인소장(일반영상) / C:공연영상)
    @GET("profile/getMyPrivateContents")
    fun getMyPrivateContents(
        @Query("dataType") dataType: String,
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetPrivateContentsResponse>

    /* 메인 공지 팝업 정보 조회 */
    @GET("common/getNoticePopup")
    fun fetchMainNoticePopup(
    ): Single<NoticeResponse>

    /*검색 메인 정보 조회*/
    @GET("search/getSearchMain")
    fun getSearchMain(
    ): Single<SearchMainResponse>

    /*팔로우 설정/해제*/
    @POST("contents/updateFollow")
    fun updateFollow(
        @Body followBody: FollowBody,
    ): Single<BaseResponse>

    /*추천유저 조회*/
    @GET("profile/getRecommendUser")
    fun getRecommendUser(): Single<GetRecommendUserResponse>

    /* 씽패스 시즌클리어 조회*/
    @GET("profile/getSingPassSeasonClearInfo")
    fun getSingPassSeasonClearInfo(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetSingPassSeasonClearResponse>

    /* 부르기 메인 정보 조회 */
    @GET("contents/getSingMainContents")
    fun fetchSingMainContents(): Single<SongMainResponse>

    /* 씽패스 사용자 상세 정보 조회 */
    @GET("singpass/getSingPassUserDetailInfo")
    fun getSingPassUserDetailInfo(
        @Query("singMemCd") singMemCd: String,
        @Query("svcSeaMngCd") svcSeaMngCd: String,
        @Query("genreCd") genreCd: String,
    ): Single<SingPassUserInfoResponse>

    /* 씽패스 진행 상황 정보 조회 */
    @GET("singpass/getSingPassDashBoard")
    fun getSingPassDashBoard(
        @Query("singMemCd") singMemCd: String,
    ): Single<SingPassDashBoardResponse>

    /* 씽패스 진행 상황 미션 정보 조회 */
    @GET("singpass/getSingPassMissionInfo")
    fun getSingPassMissionInfo(
        @Query("svcMiRegTpCd") missionType: String,
        @Query("pageNum") pageNum: Int,
    ): Single<SingPassMissionResponse>

    /* 씽패스 랭킹 목록 조회 */
    @GET("singpass/getSingPassRanking")
    fun getSingPassRanking(
        @Query("genreCd") genreCd: String,
        @Query("fgListYn") listYn: String,
        @Query("pageNum") pageNum: Int,
    ): Single<SingPassRankingListResponse>

    /* 씽패스 미션 스킵 이용권 사용 요청 */
    @POST("singpass/requestSkipMission")
    fun requestSkipMission(
        @Body body: SkipMissionBody
    ): Single<SingPassMissionSkipResponse>

    /* 반주음 정보 조회*/
    @GET("contents/getSongInfo")
    fun fetchSongInfo(
        @Query("songMngCd") songMngCd: String,
        @Query("feedMngCd") feedMngCd: String,
    ): Single<SongInfoResponse>

    /* 씽패스 메인 정보 조회*/
    @GET("singpass/getSingPassMain")
    fun getSingPassMain(): Single<SingPassResponse>

    /* 팔로잉 목록조회(본인) 조회*/
    @GET("profile/getMyFollowList")
    fun getMyFollowingList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
        @Query("reqType") reqType: String,
    ): Single<GetMyFollowingListResponse>

    /* 팔로워 목록조회(본인) 조회*/
    @GET("profile/getMyFollowList")
    fun getMyFollowerList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
        @Query("reqType") reqType: String,
    ): Single<GetMyFollowerListResponse>

    /* 팔로잉 목록 조회(타인) 조회*/
    @GET("profile/getUserFollowList")
    fun getUserFollowingList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
        @Query("userMemCd") userMemCd: String,
        @Query("reqType") reqType: String,
    ): Single<GetMyFollowingListResponse>

    /* 팔로워 목록조회(타인) 조회*/
    @GET("profile/getUserFollowList")
    fun getUserFollowerList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
        @Query("userMemCd") userMemCd: String,
        @Query("reqType") reqType: String,
    ): Single<GetMyFollowerListResponse>

    /* 팔로우(잉)카운트조회(본인) 조회*/
    @GET("profile/getMyFollowCount")
    fun getMyFollowCount(): Single<GetFollowCount>

    /* 팔로우(잉)카운트조회(타인) 조회*/
    @GET("profile/getUserFollowCount")
    fun getUserFollowCount(
        @Query("userMemCd") userMemCd: String,
    ): Single<GetFollowCount>


    /*검색결과조회(인기/동영상/반주음/태그/사용자)*/
    @GET("search/requestSearch")
    fun requestSearch(
        @QueryMap(encoded = true) query: SearchQueryMap
    ): Single<SearchResponse>

    /* 반주음 피드 모아보기 목록 조회 */
    @GET("contents/getCollectionContents")
    fun getCollectionFeedContents(
        @Query("songCode") songCode: String,
        @Query("sortType") sortType: String,
        @Query("pageNum") pageNum: Int,
    ): Single<FeedContentsResponse>

    /* 태그 피드 모아보기 목록 조회 */
    @GET("search/getTagCollectionContents")
    fun getCollectionTagContents(
        @Query("tagKeyword") tagKeyword: String,
        @Query("sortType") sortType: String,
        @Query("pageNum") pageNum: Int,
    ): Single<FeedContentsResponse>

    /* 피드 상세 정보 조회 */
    @GET("contents/getFeedDetailInfo")
    fun getFeedDetailInfo(
        @Query("feedMngCd") feedMngCd: String,
    ): Single<FeedContentsResponse>

    /*1대1 문의 의견보내기*/
    @POST("setting/requestInquiryInfo")
    fun requestInquiryInfo(
        @Body inquiryBody: InquiryBody
    ): Single<BaseResponse>

    /*이미지 업로드 정보 조회*/
    @GET("common/getResourcePath")
    fun getResourcePath(
        @Query("resType") resType: String,
        @Query("contentsCd") contentsCd: String? = null,
        @Query("attImgCount") updateImageCount: Int? = null
    ): Single<GetResourcePathResponse>

    /* 이미지 업로드 완료 처리 */
    @POST("common/uploadCompletedResource")
    fun uploadCompletedResource(
        @Body uploadCompletedResourceBody: UploadCompletedResourceBody
    ): Single<BaseResponse>

    @GET("community/getMainBannerInfo")
    fun fetchCommunityMainBanner(): Single<CommunityMainBannerResponse>

    @GET("community/getLoungeList")
    fun fetchLounges(
        @QueryMap(encoded = true) queryMap: LoungeQueryMap
    ): Single<CommunityLoungeResponse>

    @GET("community/getEventList")
    fun fetchEvents(
        @QueryMap(encoded = true) queryMap: EventQueryMap
    ): Single<CommunityEventResponse>

    @GET("community/getVoteList")
    fun fetchVotes(
        @QueryMap(encoded = true) queryMap: VoteQueryMap
    ): Single<CommunityVoteResponse>

    @POST("community/requestLoungeContents")
    fun postLoungeContents(
        @Body body: LoungeBody
    ): Single<BaseResponse>

    /*라운지 글 상세*/
    @GET("community/getLoungeDetailInfo")
    fun fetchLoungeDetail(
        @Query("louMngCd") code: String
    ): Single<LoungeDetailResponse>

    /*라운지 글 삭제*/
    @POST("community/deleteLoungeContents")
    fun deleteLoungeContents(
        @Body loungeBody: LoungeBody
    ): Single<LoungeDetailResponse>


    /*알림 목록 조회*/
    @GET("profile/getAlrimList")
    fun getAlrimList(
        @Query("pageNum") pageNum: Int,
        @Query("sortType") sortType: String,
    ): Single<GetAlrimListResponse>

    @GET("community/getEventDetailInfo")
    fun fetchEventDetail(
        @Query("evtMngCd") code: String
    ): Single<EventDetailResponse>

    @GET("community/getVoteDetailInfo")
    fun fetchVoteDetail(
        @Query("comVoteMngCd") code: String
    ): Single<VoteDetailResponse>

    /* 멤버십 이용권 목록 조회 */
    @GET("purchase/getSubscribeList")
    fun getSubscribeList(): Single<GetSubscribeListResponse>

    @POST("contents/getSongList")
    fun fetchSongList(
        @Body body: SongListBody
    ): Single<SongListResponse>

    /* 친구 초대 가능 횟수 조회 */
    @GET("setting/getInvitableCount")
    fun getInvitableCount(
        @Query("recNickName") recNickName: String
    ): Single<InvitableCountResponse>

    /* 추천 닉네임 처리 요청 */
    @POST("setting/requestRecommendNickName")
    fun requestRecommendNickName(
        @Body body: RecommendNickNameBody
    ): Single<RecommendNickNameResponse>

    @POST("community/requestVote")
    fun postVote(
        @Body body: VoteBody
    ): Single<BaseResponse>

    @GET("community/getVoteResult")
    fun fetchVoteResult(
        @Query("comVoteMngCd") code: String
    ): Single<VoteResultResponse>

    /*업로드 콘텐츠 조회(본인)*/
    @GET("profile/getMyUploadContents")
    fun fetchMemberUploadContents(
        @QueryMap(encoded = true) queryMap: MyPageUploadFeedQueryMap
    ): Single<GetMyUploadContentsResponse>

    /*업로드 콘텐츠 조회(타인)*/
    @GET("profile/getUserUploadContents")
    fun fetchUserUploadContents(
        @QueryMap(encoded = true) queryMap: MyPageUploadFeedQueryMap
    ): Single<GetMyUploadContentsResponse>

    /*좋아요 콘텐츠 조회(타인)*/
    @GET("profile/getUserLikeContents")
    fun fetchUserLikeContents(
        @QueryMap(encoded = true) queryMap: MyPageLikeFeedQueryMap
    ): Single<GetMyLikeContentsResponse>

    /*좋아요 콘텐츠 조회(본인)*/
    @GET("profile/getMyLikeContents")
    fun fetchMemberLikeContents(
        @QueryMap(encoded = true) queryMap: MyPageLikeFeedQueryMap
    ): Single<GetMyLikeContentsResponse>

    /*즐겨찾기 콘텐츠 피드영상 조회(타인)*/
    @GET("profile/getUserBookMarkContents")
    fun fetchUserBookMarkContents(
        @QueryMap(encoded = true) queryMap: MyPageBookMarkQueryMap
    ): Single<GetMyBookMarkContentsResponse>

    /*즐겨찾기 콘텐츠 피드영상 조회(본인)*/
    @GET("profile/getMyBookMarkContents")
    fun fetchMemberBookMarkContents(
        @QueryMap(encoded = true) queryMap: MyPageBookMarkQueryMap
    ): Single<GetMyBookMarkContentsResponse>

    /*즐겨찾기 콘텐츠 반주음 조회(본인)*/
    @GET("profile/getMyBookMarkContents")
    fun fetchMemberAccompanimentContents(
        @QueryMap(encoded = true) queryMap: MyPageAccompanimentQueryMap
    ): Single<GetMyBookMarkAccompanimentResponse>

    /*즐겨찾기 콘텐츠 반주음 조회(타인)*/
    @GET("profile/getUserBookMarkContents")
    fun fetchUserAccompanimentContents(
        @QueryMap(encoded = true) queryMap: MyPageAccompanimentQueryMap
    ): Single<GetMyBookMarkAccompanimentResponse>

    /* 상품 구매 영수증 검증 요청 */
    @POST("purchase/validatePaymentToken")
    fun validatePaymentToken(
        @Body body: PurchaseInfoBody
    ): Single<BaseResponse>

    /* 상품 구매 완료 요청 */
    @POST("purchase/requestPurchaseCompleted")
    fun requestPurchaseCompleted(
        @Body body: PurchaseInfoBody
    ): Single<UserLoginResponse>

    /*노래 부르기 이력 적재*/
    @POST("contents/requestSingHistory")
    fun requestSingHistory(
        @Body singHistoryBody: SingHistoryBody
    ): Single<BaseResponse>

    /*피드 재생 확인 이력 적재*/
    @POST("contents/viewFeedContentsHistory")
    fun insertViewFeedContentsHistory(
        @Body feedHistoryBody: FeedHistoryBody
    ): Single<BaseResponse>

    /*광고 확인 이력 집계*/
    @POST("contents/viewAdContents")
    fun insertViewAdContents(
        @Body adHistoryBody: AdHistoryBody
    ): Single<BaseResponse>

    /*광고 클릭 이력 집계*/
    @POST("contents/clickAdContents")
    fun insertViewClickAdContents(
        @Body adHistoryBody: AdHistoryBody
    ): Single<BaseResponse>

    /*부르기 완료 이력 집계*/
    @POST("contents/completedSong")
    fun reqeustCompletedSong(
        @Body singHistoryBody: SingHistoryBody
    ): Single<BaseResponse>

    /*피드 삭제 처리 요청*/
    @POST("profile/deleteMyFeed")
    fun deleteMyFeed(
        @Body feedUpdateBody: FeedUpdateBody
    ): Single<BaseResponse>

    /*피드 비공개/공개 전환 처리 요청*/
    @POST("profile/requestFeedVisibility")
    fun requestFeedVisibility(
        @Body feedUpdateBody: FeedUpdateBody
    ): Single<BaseResponse>

    /*피드 댓글 허용 여부 변경 요청*/
    @POST("profile/updateAvailComment")
    fun updateAvailComment(
        @Body availCommentBody: AvailCommentBody
    ): Single<BaseResponse>

    /* 해당 사용자 채팅방 관리 번호 조회 */
    @POST("chat/requestChatRoomInfo")
    fun fetchMemberChatRoomInfo(
        @Body body: ChatMemberRoomBody
    ): Single<ChatMemberRoomInfo>

    @GET("chat/getChatRoomList")
    fun fetchChatRooms(
        @QueryMap(encoded = true) queryMap: ChatRoomQueryMap
    ): Single<ChatMemberRoomsResponse>

    @GET("chat/getChatList")
    fun fetchChatMessages(
        @QueryMap(encoded = true) queryMap: ChatRoomMessageQueryMap
    ): Single<ChatMessagesResponse>

    @POST("chat/deleteChatRoom")
    fun deleteChatRoom(
        @Body body: DeleteChatBody
    ): Single<BaseResponse>

    /* 서비스 점검 팝업 정보 조회*/
    @GET("common/checkSystemNotPopup")
    fun checkSystemNotPopup(): Single<CheckSystemNotPopupResponse>

}
