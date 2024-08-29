package com.verse.app.ui.song.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HeaderInfo
import com.verse.app.contants.SingType
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.param.SongListBody
import com.verse.app.model.song.SongMainData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Song List ViewModel
 *
 * Created by jhlee on 2023-04-05
 */
@HiltViewModel
class SongListViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider
) : ActivityViewModel() {

    private val _pageParam: MutableLiveData<Triple<HeaderInfo, String, String>> by lazy { MutableLiveData() }
    val pageParam: MutableLiveData<Triple<HeaderInfo, String, String>> get() = _pageParam

    private val _headerInfo: MutableLiveData<HeaderInfo> by lazy { MutableLiveData() }
    val headerInfo: MutableLiveData<HeaderInfo> get() = _headerInfo

    lateinit var songListBody: SongListBody

    private val _songDataList: MutableLiveData<PagingData<SongMainData>> by lazy { MutableLiveData() }
    val songDataList: MutableLiveData<PagingData<SongMainData>> get() = _songDataList
    val moveToSing: SingleLiveEvent<Triple<String, String, String>> by lazy { SingleLiveEvent() } //부르기 이동 param

    val refresh: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } //refresh

    fun start() {
        val param: Triple<HeaderInfo, String, String>? = savedStateHandle[ExtraCode.SONG_MORE_TYPE]
        if (param != null) {
            _pageParam.value = param
            _headerInfo.value = param.first
            onRefresh(0)
            requestSongList()
        }
    }

    /**
     * Song List
     */
    fun requestSongList() {
        songListBody?.let {
            repository.fetchSongList(it,this)
                .cachedIn(viewModelScope)
                .request({ response ->
                    _songDataList.value = response
                })
        }
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
     *  파라미터 설정 및 갱신
     */
    fun onRefresh(pos: Int) {

        _pageParam.value?.let {
            when (pos) {
                0,
                1 -> {
                    songListBody = SongListBody(
                        genreCd = if (it.first == HeaderInfo.SONG_GENRE) it.second else "",
                        reqTypeCd = it.third,
                        sortType = SortEnum.DESC.query,
                        pageNum = 1,
                        songMngCd = ""
                    )
                }

                2 -> {
                    songListBody = SongListBody(
                        genreCd = if (it.first == HeaderInfo.SONG_GENRE) it.second else "",
                        reqTypeCd = it.third,
                        sortType = SortEnum.ASC.query,
                        pageNum = 1,
                        songMngCd = ""
                    )
                }
            }
            if (pos > 0) {
                refresh.call()
            }
        }
    }
}