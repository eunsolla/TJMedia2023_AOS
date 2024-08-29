package com.verse.app.ui.song.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.HeaderInfo
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.ReqTypeCd
import com.verse.app.contants.SingType
import com.verse.app.contants.TabPageType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.song.SongData
import com.verse.app.model.song.SongMainData
import com.verse.app.model.song.SongMainInfo
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.ui.search.activity.SearchMainActivity
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-03-29
 */
@HiltViewModel
class SongMainViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
) : ActivityViewModel() {

    private val _songMainDataList: ListLiveData<SongMainInfo> by lazy { ListLiveData() } // 지금뜨는노래,최근불렀던노래,장르,노래차트
    val songMainDataList: ListLiveData<SongMainInfo> get() = _songMainDataList

    val _songDataList: ListLiveData<SongData> by lazy { ListLiveData() } //인기곡 신곡
    val songDataList: ListLiveData<SongData> get() = _songDataList

    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = ""))                 //인기곡 empty
            add(PagerTabItem(title = ""))                 //신곡 empty
        }
    }

    val refresh: SingleLiveEvent<Int> by lazy { SingleLiveEvent() } // Rv Refresh
    val moveToMore: SingleLiveEvent<Triple<HeaderInfo, String, String>> by lazy { SingleLiveEvent() } //더보기 이동

    val moveToSing: SingleLiveEvent<Triple<String, String, String>> by lazy { SingleLiveEvent() } //부르기 이동 param

    private val _startRefresh: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } //부르기 이동 param
    val startRefresh: LiveData<Unit>get() = _startRefresh

    /**
     * Song Main API
     */
    fun requestSongMain() {

        apiService.fetchSingMainContents()
            .doLoading()
            .applyApiScheduler()
            .request(
                { response ->

                    val tmpList = mutableListOf<SongMainInfo>()

                    if(_songMainDataList.value.size <= 0){
                        tmpList.add(SongMainInfo().apply {
                            this.nowHotSongList = response.result.nowHotSongList
                            this.itemViewType = ListPagedItemType.SONG_HOT_INFO_VIEW
                        })

                        tmpList.add(SongMainInfo().apply {
                            this.recentSingList = response.result.recentSingList
                            this.itemViewType = ListPagedItemType.SONG_RECENTLY_VIEW
                        })

                        tmpList.add(SongMainInfo().apply {
                            this.genreList = response.result.genreList
                            this.itemViewType = ListPagedItemType.SONG_GENRE_VIEW
                        })

                        tmpList.add(SongMainInfo().apply {
                            this.songChartList = response.result.songChartList
                            this.itemViewType = ListPagedItemType.SONG_CHART_VIEW
                        })

                        _songMainDataList.addAll(tmpList)
                    }else{
                        _songMainDataList.value[0].nowHotSongList = response.result.nowHotSongList
                        _songMainDataList.value[1].recentSingList = response.result.recentSingList
                        _songMainDataList.value[2].genreList = response.result.genreList
                        _songMainDataList.value[3].songChartList?.popSongList = response.result.songChartList.popSongList
                        _songMainDataList.value[3].songChartList?.newSongList = response.result.songChartList.newSongList
                        _startRefresh.call()
                    }

                }, {

                })
    }

    /**
     * 인기곡 / 신곡 차트 전환
     */
    fun onChangeChartPage(type: TabPageType, position: Int) {
        _songMainDataList.value[position].songChartList?.let {
            it.prPageType = type
            refresh.value = position
        }
    }

    /**
     * 최근 불렀던 노래 / 노래 차트 (인기곡/신곡) 더보기
     */
    fun onMore(type: TabPageType, str: String? = "") {

        moveToMore.value = when (type) {
            TabPageType.SONG_RECENTLY -> {
                Triple(HeaderInfo.SONG_RECENTLY, "", ReqTypeCd.M.name)
            }

            TabPageType.SONG_POPULAR -> {
                Triple(HeaderInfo.SONG_POPULAR, "", ReqTypeCd.P.name)
            }

            TabPageType.SONG_RECENT -> {
                Triple(HeaderInfo.SONG_RECENT, "", ReqTypeCd.N.name)
            }

            TabPageType.SONG_GENRE -> {

                if (!str.isNullOrEmpty()) {
                    val sText = str.split("_")
                    Triple(HeaderInfo.SONG_GENRE.apply {
                        dynamicText = sText[0]
                    }, sText[1], ReqTypeCd.G.name)
                } else {
                    Triple(HeaderInfo.SONG_GENRE, "", ReqTypeCd.G.name)
                }
            }

            else -> Triple(HeaderInfo.DEFAULT, "", "")
        }
    }

    /**
     * 검색어 입력 페이지로 이동
     */
    fun moveToSearchKeyword() {
        moveToPage(
            ActivityResult(
                targetActivity = SearchMainActivity::class,
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
        )
    }


    /**
     * 파트선택,부르기 페이지 이동
     */
    fun onMoveToSing(data: SongMainData) {
        data?.let {
            if (it.songMngCd.isNotEmpty()) {
                moveToSing.value = Triple(SingType.SOLO.code, it.songMngCd, "")
            }
        }
    }

    /**
     * 인기곡/신곡 Set
     */
    fun setPRDataList(dataList: ArrayList<SongData>) {
        _songDataList.value.clear()
        _songDataList.value.addAll(dataList)
    }
}