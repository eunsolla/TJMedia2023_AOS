package com.verse.app.ui.feed.viewholders

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.BaseActFeedViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.utility.DLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 피드 상세 viewmodel
 *
 * Created by jhlee on 2023/06/16
 */
@HiltViewModel
class  FeedDetailViewModel @Inject constructor() : BaseActFeedViewModel() {

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    fun start() {

        val feedMngCd = savedStateHandle.get<String>(ExtraCode.FEED_MNG_CD)

        DLogger.d("FeedDetailViewModel => ${feedMngCd}")

        if (feedMngCd.isNullOrEmpty()) {
            _startFinishEvent.call()
            return
        }

        repository.fetchFeedDetail(feedMngCd = feedMngCd)
            .applyApiScheduler()
            .cachedIn(viewModelScope)
            .request({ response ->
                response?.let {
                    _startFeedDetailEvent.value = Triple(0 ,feedMngCd,response)
                }
            })
    }

}
