package com.verse.app.ui.search.viewmodel

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.search.NowLoveSongData
import com.verse.app.model.search.PopKeywordData
import com.verse.app.model.search.PopularKeywordModel
import com.verse.app.model.search.RecentKeywordData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.search.activity.SearchResultActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Description : 검색어 입력 화면 ViewModel
 *
 * Created by juhongmin on 2023/05/09
 */
@HiltViewModel
class SearchMainViewModel @Inject constructor(
    private val apiService: ApiService,
    private val accountPref: AccountPref,
    val resProvider: ResourceProvider,

    ) : ActivityViewModel() {

    data class MiddleModel(
        val popKeywordList: List<PopularKeywordModel>,
        val nowLoveSongList: List<NowLoveSongData>
    )

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    private val _overallRecentKeywordList: ListLiveData<RecentKeywordData> by lazy { ListLiveData() }
    val overallRecentKeywordList: ListLiveData<RecentKeywordData> get() = _overallRecentKeywordList

    // 자세히 보기 버튼 노출됐는지 확인 하는 flag, true 자세히 보기 클릭, false 자세히 보기 클릭 X
    private var isClickOverallButton = false
    val recentKeywordList: LiveData<List<BaseModel>>
        get() = Transformations.map(overallRecentKeywordList) {
            if (isClickOverallButton) {
                it
            } else {
                it.subList(0, Math.min(it.size, 5))
            }
        }
    val isShowRecentOverall: LiveData<Boolean>
        get() = Transformations.map(overallRecentKeywordList) {
            it.size > 5 && !isClickOverallButton
        }
    private val _isRecentKeywordClear: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val isRecentKeywordClear: LiveData<Boolean> get() = _isRecentKeywordClear

    private val _popularList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val popularList: ListLiveData<BaseModel> get() = _popularList
    private val _nowLoveSongList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val nowLoveSongList: ListLiveData<BaseModel> get() = _nowLoveSongList
    private val _isContentsShowEvent: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val isContentsShowEvent get() = _isContentsShowEvent
    val currentKeyword: MutableLiveData<String> by lazy { MutableLiveData() }
    val showOneButtonDialogPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }


    fun start() {
        _isContentsShowEvent.value = false
        currentKeyword.value = null
        Single.zip(
            reqRecentKeywords(),
            reqPopularAndLoveSong()
        ) { keywords, middleModel ->
            return@zip keywords to middleModel
        }
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                handleOnSuccess(
                    it.first,
                    it.second.popKeywordList,
                    it.second.nowLoveSongList
                )
            })
    }

    private fun addRecentKeyword(keyword: String) {
        _overallRecentKeywordList.add(
            0,
            RecentKeywordData(overallRecentKeywordList.size.plus(1), keyword)
        )
        // 20개 넘어가면 맨 마지막 삭제
        if (overallRecentKeywordList.size > 20) {
            _overallRecentKeywordList.removeLast()
        }

        saveRecentKeywords(overallRecentKeywordList.data())
    }

    /**
     * 최근 검색어 리턴하는 함수
     * 최근 검색어는 20개까지 그 이상은 맨 마지막꺼 하나씩 삭제 하도록 처리
     */
    private fun reqRecentKeywords(): Single<List<RecentKeywordData>> {
        return Single.create { emitter ->
            try {
                val list = mutableListOf<RecentKeywordData>()
                val splitList = accountPref.getRecnetSearchWord()
                    .split(",")
                    .filter { it.trim() != "" }
                splitList.forEachIndexed { index, s ->
                    list.add(RecentKeywordData(index, s.trim()))
                }
                emitter.onSuccess(list)
            } catch (ex: Exception) {
                emitter.onSuccess(listOf())
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun reqPopularAndLoveSong(): Single<MiddleModel> {
        return apiService.getSearchMain().map { res ->
            val popKeywordList = res.result.popKeywordList.orEmpty()
            val nowSongList = res.result.nowLoveSongList.orEmpty()
            val popKeywordModel = mutableListOf<PopularKeywordModel>()


            for (idx in popKeywordList.indices step 5) {
                // idx + 5 최대 길이 넘지 않도록
                val toIndex = Math.min(idx.plus(5), popKeywordList.size)
                popKeywordModel.add(PopularKeywordModel(idx, popKeywordList.subList(idx, toIndex)))
            }
            return@map MiddleModel(popKeywordModel, nowSongList)
        }.subscribeOn(Schedulers.io())
            .onErrorReturn { MiddleModel(listOf(), listOf()) }
    }

    private fun handleOnSuccess(
        recentKeywords: List<RecentKeywordData>,
        popKeywords: List<PopularKeywordModel>,
        nowLoveSongs: List<NowLoveSongData>
    ) {
        _overallRecentKeywordList.clear()
        _overallRecentKeywordList.addAll(recentKeywords)
        _popularList.clear()
        _popularList.addAll(popKeywords)
        _nowLoveSongList.clear()
        _nowLoveSongList.addAll(nowLoveSongs)
        _isContentsShowEvent.value = true
    }

    /**
     * 최근 검색어 DB 저장처리함수
     * @param newKeywords 다시 저장할 최근 검색어
     */
    fun saveRecentKeywords(newKeywords: List<RecentKeywordData>) {
        DLogger.d("최근 검색어 저장합니다. ${newKeywords.size}")
        accountPref.setRecnetSearchWord(newKeywords.joinToString(",") { it.recentKeyword })
        if (newKeywords.isNotEmpty()) {
            if (isShowRecentOverall.value == true) {
                _isRecentKeywordClear.value = false
            } else if (isShowRecentOverall.value == false) {
                _isRecentKeywordClear.value = newKeywords.size > 5
            } else {
                _isRecentKeywordClear.value = false
            }
        } else {
            _isRecentKeywordClear.value = false
        }
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

    /**
     * 최근 검색어 자세히 보기
     */
    fun onShowRecentOverall() {
        isClickOverallButton = true
        _overallRecentKeywordList.refresh()
        _isRecentKeywordClear.value = true
    }

    fun onAllRecentDelete() {
        showOneButtonDialogPopup.value =
            resProvider.getString(R.string.search_recent_keyword_all_dekete_2)
    }

    fun onDeleteRecentKeyword(data: RecentKeywordData) {
        overallRecentKeywordList.remove(data)
        saveRecentKeywords(overallRecentKeywordList.data())
    }

    fun onMoveToSearch(data: BaseModel) {
        if (data is RecentKeywordData) {
            moveToPage(
                ActivityResult(
                    targetActivity = SearchResultActivity::class,
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                    data = bundleOf(ExtraCode.SEARCH_KEYWORD to data.recentKeyword)
                )
            )
        } else if (data is PopKeywordData) {
            addRecentKeyword(data.popKeyword)
            moveToPage(
                ActivityResult(
                    targetActivity = SearchResultActivity::class,
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                    data = bundleOf(ExtraCode.SEARCH_KEYWORD to data.popKeyword)
                )
            )
        }

    }

    /**
     * 키워드 입력시 전달 받는 함수
     * @param text keywords
     */
    fun onKeyword(text: String) {
        if (currentKeyword.value != text) {
            currentKeyword.value = text
        }
    }

    fun clearKeywords() {
        currentKeyword.value = ""
    }

    fun doSearch() {
        val keyword = currentKeyword.value ?: return
        if (keyword.isNotEmpty()) {
            addRecentKeyword(keyword)
            moveToPage(
                ActivityResult(
                    targetActivity = SearchResultActivity::class,
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                    data = bundleOf(ExtraCode.SEARCH_KEYWORD to keyword)
                )
            )

        }
    }
}