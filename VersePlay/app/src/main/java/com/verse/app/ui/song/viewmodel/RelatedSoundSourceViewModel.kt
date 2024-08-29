package com.verse.app.ui.song.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.ReqTypeCd
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
 * Description : 연관 음원
 *
 * Created by jhlee on 2023-05-26
 */
@HiltViewModel
class RelatedSoundSourceViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider
) : ActivityViewModel() {

    private val _songMngCd: MutableLiveData<String> by lazy { MutableLiveData() }
    val songMngCd: LiveData<String> get() = _songMngCd


    lateinit var songListBody: SongListBody
    private val _songDataList: MutableLiveData<PagingData<SongMainData>> by lazy { MutableLiveData() }
    val songDataList: MutableLiveData<PagingData<SongMainData>> get() = _songDataList

    val moveToSing: SingleLiveEvent<Triple<String, String, String>> by lazy { SingleLiveEvent() } //부르기 이동 param

    val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val _startSortDialogEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val refresh: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } //refresh


    fun start() {
        val songMngCd: String? = savedStateHandle[ExtraCode.SINGING_SONG_MNG_CD]

        if (songMngCd.isNullOrEmpty()) {
            return
        }
        _songMngCd.value = songMngCd
        onRefresh(0)
        fetchSongList()
    }

    /**
     * 연관 음원 List
     */
    fun fetchSongList() {
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

        _songMngCd.value?.let {
            when (pos) {
                0,
                1 -> {
                    songListBody = SongListBody(
                        genreCd = "",
                        reqTypeCd = ReqTypeCd.R.name,
                        sortType = SortEnum.DESC.query,
                        pageNum = 1,
                        songMngCd = it
                    )
                }

                2 -> {
                    songListBody = SongListBody(
                        genreCd = "",
                        reqTypeCd = ReqTypeCd.R.name,
                        sortType = SortEnum.ASC.query,
                        pageNum = 1,
                        songMngCd = it
                    )
                }
            }
            if (pos > 0) {
                refresh.call()
            }
        }
    }

    fun onShowSortDialog() {
        _startSortDialogEvent.call()
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

}