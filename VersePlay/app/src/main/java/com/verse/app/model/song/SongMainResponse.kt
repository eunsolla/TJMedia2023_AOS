package com.verse.app.model.song

import com.google.common.collect.Lists
import com.verse.app.contants.TabPageType
import com.verse.app.livedata.ListLiveData
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.base.BaseViewTypeModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 곡 정보 Data
 *
 * Created by jhlee on 2023-04-27
 */

@Serializable
data class SongMainResponse(
    @SerialName("result")
    val result: SongMainInfo = SongMainInfo()
) : BaseResponse()

@Serializable
data class SongMainInfo(
    var nowHotSongList: List<SongMainData> = listOf(),         //지금 뜨는 노래 목록 (CMS 내 등록된 모든 노래 목록)
    var recentSingList: List<SongMainData> = listOf(),         //최근 불렀던 노래 (노래부르기 완료 이력에 존재하는 최근 순 10곡 고정)
    var genreList: List<GenreData> = listOf(),                     //장르
    var songChartList: SongChartInfo = SongChartInfo(),                  //인기곡,신곡
) : BaseViewTypeModel() {
    var genreisOpened: Boolean = false
}

@Serializable
data class SongChartInfo(
    var popSongList: List<SongMainData> = listOf(),        //인기곡 목록 (고정 10곡)
    var newSongList: List<SongMainData> = listOf(),        //신곡 목록 (고정 10곡)
    var prPageType: TabPageType? = TabPageType.SONG_POPULAR, //인기곡,신곡에서 사용
) {
    @Contextual
    var prDataList: ListLiveData<SongDataList>? = null
        get() = if (prPageType == TabPageType.SONG_POPULAR) {
            popSongList?.let { items ->
                if (items.isNotEmpty()) {
                    getList(items)
                } else {
                    ListLiveData()
                }
            }
        } else {
            newSongList?.let { items ->
                if (items.isNotEmpty()) {
                    getList(items)
                } else {
                    ListLiveData()
                }
            }
        }

    private fun getList(items: List<SongMainData>): ListLiveData<SongDataList> {

        return ListLiveData<SongDataList>().apply {

            val onePageList = mutableListOf<SongMainData>()
            val twoPageList = mutableListOf<SongMainData>()

            items.forEachIndexed { index, songMainData ->
                if (index <= 4) {
                    onePageList.add(songMainData)
                } else {
                    twoPageList.add(songMainData)
                }
            }
            add(SongDataList(dataList = onePageList))
            add(SongDataList(dataList = twoPageList))
        }
    }
}