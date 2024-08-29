package com.verse.app.ui.search.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.ui.search.activity.SearchMainActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/08
 */
@HiltViewModel
class SearchResultFragmentViewModel @Inject constructor(
    private val resProvider: ResourceProvider
) : FragmentViewModel() {
    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = resProvider.getString(R.string.search_result_tab_popular)))
            add(PagerTabItem(title = resProvider.getString(R.string.search_result_tab_video)))
            add(PagerTabItem(title = resProvider.getString(R.string.search_result_tab_accompaniment)))
            add(PagerTabItem(title = resProvider.getString(R.string.search_result_tab_tag)))
            add(PagerTabItem(title = resProvider.getString(R.string.search_result_tab_user)))
        }
    }

    private val _searchKeyword: MutableLiveData<String> by lazy { MutableLiveData() }
    val searchKeyword: LiveData<String> get() = _searchKeyword

    private val _startInvalidIntentEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startInvalidIntentEvent: LiveData<Unit> get() = _startInvalidIntentEvent

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent
    val tabPosition: MutableLiveData<Int> by lazy { MutableLiveData() }
    private val _startSortDialogEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startSortDialogEvent: LiveData<Unit> get() = _startSortDialogEvent

    fun start() {
        val keyword: String? = savedStateHandle[ExtraCode.SEARCH_KEYWORD]
        val pos: Int? = savedStateHandle[ExtraCode.SEARCH_RESULT_POS]
        DLogger.d("searchKeyword $keyword")
        if (keyword.isNullOrEmpty()) {
            // 페이지 종료
            _startInvalidIntentEvent.call()
            return
        }
        _searchKeyword.value = keyword

        if (pos != null) {
            moveToTab(pos)
        }
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

    fun moveToTab(pos: Int) {
        if (tabPosition.value != pos) {
            tabPosition.value = pos
        }
    }

    /**
     * 검색어 입력 페이지로 이동
     */
    fun moveToSearchKeyword() {
        val page = ActivityResult(
            targetActivity = SearchMainActivity::class,
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK,
            enterAni = 0,
            exitAni = 0
        )
        moveToPage(page)
    }

    fun onShowSortDialog() {
        _startSortDialogEvent.call()
    }
}
