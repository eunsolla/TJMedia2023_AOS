package com.verse.app.repository.paging

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.FeedSubDataType
import com.verse.app.extension.onMain
import com.verse.app.model.community.CommunityMainBannerData
import com.verse.app.model.enums.SearchType
import com.verse.app.model.feed.CommonAccompanimentData
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.feed.FeedContentsResponse
import com.verse.app.model.mypage.GetPrivateContentsResponse
import com.verse.app.model.param.CommentParam
import com.verse.app.model.param.MyPageAccompanimentQueryMap
import com.verse.app.model.param.MyPageBookMarkQueryMap
import com.verse.app.model.param.MyPageLikeFeedQueryMap
import com.verse.app.model.param.MyPageUploadFeedQueryMap
import com.verse.app.model.param.SearchQueryMap
import com.verse.app.model.param.SongListBody
import com.verse.app.model.search.SearchResultSongData
import com.verse.app.model.search.SearchResultTagData
import com.verse.app.model.search.SearchResultUserData
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.feed.viewmodel.CollectionFeedViewModel
import com.verse.app.utility.exo.ExoProvider
import com.verse.app.utility.provider.FeedRefreshProvider
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description :Paging Class
 *
 * Created by jhlee on 2023-02-03
 */
class Repository @Inject constructor(
    val exoProvider: ExoProvider,
    val apiService: ApiService,
    private val feedRefreshProvider: FeedRefreshProvider
) : BaseRepository() {

    /**
     * 마이페이지 피드 비공개 동영상 조회
     */
    fun fetchMypagePrivate(dataType: FeedSubDataType, sortType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getMyPrivateContents(dataType.toString(), pageNo, sortType)
            .doLoading(pageNo, viewModel)
            .map {
                goPreLoadContents(it.result.dataList)
                it
            }
            .map { handleRefreshFeed(it) }
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 댓글 목록  조회
     */
    fun fetchCommentList(param: CommentParam) = createPager { pageNo ->
        param.pageNum = pageNo
        apiService.fetchCommentList(param.apply {
            pageNum = pageNo
        }.toMap())
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 유형별반주음 목록 조회
     */
    fun fetchSongList(param: SongListBody, viewModel: BaseViewModel) = createPager { pageNo ->
        param.pageNum = pageNo
        apiService.fetchSongList(
            SongListBody(
                pageNum = param.pageNum,
                songMngCd = param.songMngCd,
                reqTypeCd = param.reqTypeCd,
                sortType = param.sortType,
                genreCd = param.genreCd
            )
        )
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(
                    page = pageNo,
                    totalPageCount = it.result.totalPageCnt,
                    data = it.result.resSongInfoList
                )
            }
            .onErrorReturn { PagingSource.LoadResult.Error(it) }
    }

    /**
     * 로그인 디바이스
     */
    fun fetchLoginDevice(sortType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getRecentLoginHistory(pageNo, sortType)
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 차단 유저
     */
    fun fetchMypageBlockList(sortType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getBlockUserList(pageNo, sortType)
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 내 팔로잉 리스트
     */
    fun fetchMyFollowing(sortType: String, reqType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getMyFollowingList(pageNo, sortType, reqType)
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.followingList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 내 팔로워 리스트
     */
    fun fetchMyFollower(sortType: String, reqType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getMyFollowerList(pageNo, sortType, reqType)
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.followerList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 타 유저 팔로잉 리스트
     */
    fun fetchUserFollowing(sortType: String, userMemCd: String, reqType: String, viewModel: BaseViewModel) =
        createPager { pageNo ->
            apiService.getUserFollowingList(pageNo, sortType, userMemCd, reqType)
                .doLoading(pageNo, viewModel)
                .map {
                    toLoadResult(pageNo, it.result.totalPageCnt, it.result.followingList)
                }
                .onErrorReturn {
                    PagingSource.LoadResult.Error(it)
                }
        }

    /**
     * 타 유저 팔로워 리스트
     */
    fun fetchUserFollower(sortType: String, userMemCd: String, reqType: String, viewModel: BaseViewModel) =
        createPager { pageNo ->
            apiService.getUserFollowerList(pageNo, sortType, userMemCd, reqType)
                .doLoading(pageNo, viewModel)
                .map {
                    toLoadResult(pageNo, it.result.totalPageCnt, it.result.followerList)
                }
                .onErrorReturn {
                    PagingSource.LoadResult.Error(it)
                }
        }

    /**
     * 씽패스 랭킹 목록 조회
     */
    fun fetchSingPassRanking(genreCd: String, listYn: String) = createPager { pageNo ->
        apiService.getSingPassRanking(genreCd, listYn, pageNo)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.singPassRankingInfo)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 씽패스 미션 유형별 미션 목록 조회
     */
    fun fetchSingPassMissionInfo(missionType: String) = createPager { pageNo ->
        apiService.getSingPassMissionInfo(missionType, pageNo)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.missionList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 피드 모아보기 목록 조회
     */
    fun fetchCollectionFeedList(
        viewModel: CollectionFeedViewModel,
        songMngCd: String,
        sortType: String
    ) = createPager { pageNo ->
        apiService.getCollectionFeedContents(songMngCd, sortType, pageNo)
            .doLoading(pageNo, viewModel)
            .map { goPreLoadContents(it) }
            .map { handleRefreshFeed(it) }
            .map {
                onMain {
                    viewModel.collectionContentsCount.postValue(it.result.totalCnt)
                    viewModel.isBookMark.postValue(it.result.fgSongBookMarkYn == AppData.Y_VALUE)
                }
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 태그 모아보기 목록 조회
     */
    fun fetchCollectionTagList(
        viewModel: CollectionFeedViewModel,
        tagName: String,
        sortType: String
    ) = createPager { pageNo ->
        apiService.getCollectionTagContents(tagName, sortType, pageNo)
            .doLoading(pageNo, viewModel)
            .map { goPreLoadContents(it) }
            .map {
                // Feed 자동 갱신 업데이트 처리
                handleRefreshFeed(it)
                viewModel.collectionContentsCount.postValue(it.result.totalCnt)
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 공지사항 목록 조회
     */
    fun fetchNoticeList(sortType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getNoticeList(pageNo, sortType)
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 피드 상세
     */
    fun fetchFeedDetail(feedMngCd: String) = createPager { pageNo ->
        apiService.getFeedDetailInfo(feedMngCd)
            .map { goPreLoadContents(it) }
            .map { handleRefreshFeed(it) }
            .delay(1000, TimeUnit.MILLISECONDS)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    fun fetchCommunityMainBannerList(): Single<List<CommunityMainBannerData>> {
        return apiService.fetchCommunityMainBanner()
            .map { it.result.list }
            .map { list ->
                val modelList = mutableListOf<CommunityMainBannerData>()
                list.forEach { item ->
                    val model1 = item.getModel1()
                    if (model1 != null) {
                        modelList.add(model1)
                    }
                    val model2 = item.getModel2()
                    if (model2 != null) {
                        modelList.add(model2)
                    }
                    val model3 = item.getModel3()
                    if (model3 != null) {
                        modelList.add(model3)
                    }
                    val model4 = item.getModel4()
                    if (model4 != null) {
                        modelList.add(model4)
                    }
                    val model5 = item.getModel5()
                    if (model5 != null) {
                        modelList.add(model5)
                    }
                }
                return@map modelList
            }
    }

    /**
     * 알림 리스트
     */
    fun fetchMyAlrimList(sortType: String, viewModel: BaseViewModel) = createPager { pageNo ->
        apiService.getAlrimList(pageNo, sortType)
            .doLoading(pageNo, viewModel)
            .map {
                toLoadResult(pageNo, it.result.totalPageCnt, it.result.dataList)
            }
            .onErrorReturn {
                PagingSource.LoadResult.Error(it)
            }
    }

    /**
     * 컨텐츠 캐싱 작업
     */
    private fun goPreLoadContents(res: FeedContentsResponse): FeedContentsResponse {
        if (res.result.dataList.isNotEmpty()) {
            exoProvider.onPreLoadDetailList(res.result.dataList.toMutableList())
        }
        return res
    }

    /**
     * 컨텐츠 캐싱 작업
     */
    private fun goPreLoadContents(dataList: List<FeedContentsData>) {
        if (dataList.isNotEmpty()) {
            exoProvider.onPreLoadDetailList(dataList.toMutableList())
        }
    }

    /**
     * 마이 페이지 업로드
     * @param queryMap API 호출에 필요한 QueryParameters
     * memberCode 에 따라서 해당 사용자의 데이터를 호출할지, 로그인한 사용자의 데이터를 가져올지 처리
     */
    fun fetchMyPageUploads(
        queryMap: MyPageUploadFeedQueryMap,
        viewModel: FragmentViewModel,
        isLoadingShow: Boolean = true,
        isRefresh: Boolean? = false
    ): Observable<PagingData<FeedContentsData>> {
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            val api = if (queryMap.memberCode.isNullOrEmpty()) {
                apiService.fetchMemberUploadContents(queryMap)
            } else {
                apiService.fetchUserUploadContents(queryMap)
            }
            api.doOnSubscribe {
                if (isRefresh == true || isLoadingShow) {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
            }
                .map { it.result }
                .map {
                    // Feed 자동 갱신 업데이트 처리
                    handleRefreshFeed(it.dataList)
                    toLoadResult(pageNo, it.totalPageCnt, it.dataList, it.totalCnt)
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    /**
     * 마이 페이지 좋아요
     * @param queryMap API 호출에 필요한 QueryParameters
     * memberCode 에 따라서 해당 사용자의 데이터를 호출할지, 로그인한 사용자의 데이터를 가져올지 처리
     */
    fun fetchMyPageLike(
        queryMap: MyPageLikeFeedQueryMap,
        viewModel: FragmentViewModel,
        isLoadingShow: Boolean = true,
        isRefresh: Boolean? = false
    ): Observable<PagingData<FeedContentsData>> {
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            val api = if (queryMap.memberCode.isNullOrEmpty()) {
                apiService.fetchMemberLikeContents(queryMap)
            } else {
                apiService.fetchUserLikeContents(queryMap)
            }
            api.doOnSubscribe {
                if (isRefresh == true || isLoadingShow) {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
            }
                .map { it.result }
                .map {
                    // Feed 자동 갱신 업데이트 처리
                    handleRefreshFeed(it.dataList)
                    it
                }
                .map {
                    toLoadResult(pageNo, it.totalPageCnt, it.dataList, it.totalCnt)
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    /**
     * 마이 페이지 즐겨찾기
     * @param queryMap API 호출에 필요한 QueryParameters
     * memberCode 에 따라서 해당 사용자의 데이터를 호출할지, 로그인한 사용자의 데이터를 가져올지 처리
     */
    fun fetchMyPageBookmark(
        queryMap: MyPageBookMarkQueryMap,
        viewModel: FragmentViewModel,
        isLoadingShow: Boolean = true,
        isRefresh: Boolean? = false
    ): Observable<PagingData<FeedContentsData>> {
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            val api = if (queryMap.memberCode.isNullOrEmpty()) {
                apiService.fetchMemberBookMarkContents(queryMap)
            } else {
                apiService.fetchUserBookMarkContents(queryMap)
            }
            api.doOnSubscribe {
                if (isRefresh == true || isLoadingShow) {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
            }
                .map { it.result }
                .map {
                    // Feed 자동 갱신 업데이트 처리
                    handleRefreshFeed(it.dataList)
                    toLoadResult(pageNo, it.totalPageCnt, it.dataList, it.totalCnt)
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    /**
     * 마이페이지 즐겨찾기 > 반주음
     * @param queryMap API 호출에 필요한 QueryParameters
     * memberCode 에 따라서 해당 사용자의 데이터를 호출할지, 로그인한 사용자의 데이터를 가져올지 처리
     */
    fun fetchMyPageAccompaniment(
        queryMap: MyPageAccompanimentQueryMap,
        viewModel: FragmentViewModel,
        isLoadingShow: Boolean = true,
        isRefresh: Boolean? = false
    ): Observable<PagingData<CommonAccompanimentData>> {
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            val api = if (queryMap.memberCode.isNullOrEmpty()) {
                apiService.fetchMemberAccompanimentContents(queryMap)
            } else {
                apiService.fetchUserAccompanimentContents(queryMap)
            }
            api.doOnSubscribe {
                if (isRefresh == true || isLoadingShow) {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
            }.map { it.result }
                .map {
                    toLoadResult(pageNo, it.totalPageCnt, it.dataList, it.totalCnt)
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    /**
     * 검색 > 동영상 페이징 함수
     * @param queryMap QueryParameters
     */
    fun fetchFeedSearch(
        queryMap: SearchQueryMap,
        viewModel: BaseViewModel
    ): Observable<PagingData<FeedContentsData>> {
        queryMap.type = SearchType.VIDEO
        queryMap.pageNo = 1
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            apiService.requestSearch(queryMap)
                .doOnSubscribe {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
                .map { it.result }
                .map {
                    // Feed 자동 갱신 업데이트 처리
                    handleRefreshFeed(it.searchResult.feedList)
                    toLoadResult(
                        pageNo,
                        it.totalPageCnt,
                        it.searchResult.feedList
                    )
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    /**
     * 검색 > 반주음 페이징 함수
     * @param queryMap QueryParameters
     */
    fun fetchMrSearch(
        queryMap: SearchQueryMap,
        viewModel: BaseViewModel
    ): Observable<PagingData<SearchResultSongData>> {
        queryMap.type = SearchType.MR
        queryMap.pageNo = 1
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            apiService.requestSearch(queryMap)
                .doOnSubscribe {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
                .map { it.result }
                .map {
                    toLoadResult(
                        pageNo,
                        it.totalPageCnt,
                        it.searchResult.songList
                    )
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }


    /**
     * 검색 > 태그 페이징 함수
     * @param queryMap QueryParameters
     */
    fun fetchTagSearch(
        queryMap: SearchQueryMap,
        viewModel: BaseViewModel
    ): Observable<PagingData<SearchResultTagData>> {
        queryMap.type = SearchType.TAG
        queryMap.pageNo = 1
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            apiService.requestSearch(queryMap)
                .doOnSubscribe {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
                .map { it.result }
                .map {
                    toLoadResult(
                        pageNo,
                        it.totalPageCnt,
                        it.searchResult.tagList
                    )
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    /**
     * 검색 > 사용자 페이징 함수
     * @param queryMap QueryParameters
     */
    fun fetchUserSearch(
        queryMap: SearchQueryMap,
        viewModel: BaseViewModel
    ): Observable<PagingData<SearchResultUserData>> {
        queryMap.type = SearchType.USER
        queryMap.pageNo = 1
        return createPager { pageNo ->
            queryMap.pageNo = pageNo
            apiService.requestSearch(queryMap)
                .doOnSubscribe {
                    if (pageNo == 1) {
                        viewModel.onLoadingShow()
                    }
                }
                .map { it.result }
                .map {
                    toLoadResult(
                        pageNo,
                        it.totalPageCnt,
                        it.searchResult.userList
                    )
                }
                .onErrorReturn { PagingSource.LoadResult.Error(it) }
        }
    }

    private fun handleRefreshFeed(res: FeedContentsResponse): FeedContentsResponse {
        feedRefreshProvider.setUpdateFeed(res)
        return res
    }

    private fun handleRefreshFeed(res: GetPrivateContentsResponse): GetPrivateContentsResponse {
        feedRefreshProvider.setUpdateFeed(res.result.dataList)
        return res
    }

    private fun handleRefreshFeed(list: List<FeedContentsData>) {
        feedRefreshProvider.setUpdateFeed(list)
    }
}