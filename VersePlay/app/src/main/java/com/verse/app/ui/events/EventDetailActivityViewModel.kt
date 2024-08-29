package com.verse.app.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.events.EventDetailResponse
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@HiltViewModel
class EventDetailActivityViewModel @Inject constructor(
    private val apiService: ApiService
) : ActivityViewModel() {

    private val _title: NonNullLiveData<String> by lazy { NonNullLiveData("이벤트") }
    val title: LiveData<String> get() = _title

    private val _imageUrl: MutableLiveData<String> by lazy { MutableLiveData() }
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    private val _startShareEvent: SingleLiveEvent<EventDetailResponse.EventDetail> by lazy { SingleLiveEvent() }
    val startShareEvent: LiveData<EventDetailResponse.EventDetail> get() = _startShareEvent

    private val _startParticipateEvent: SingleLiveEvent<EventDetailResponse.EventDetail> by lazy { SingleLiveEvent() }
    val startParticipateEvent: LiveData<EventDetailResponse.EventDetail> get() = _startParticipateEvent

    private var eventModel: EventDetailResponse.EventDetail? = null

    fun start() {
        val code = savedStateHandle.get<String>(ExtraCode.EVENT_DETAIL_CODE)
        if (code.isNullOrEmpty()) {
            _startFinishEvent.call()
            return
        }

        apiService.fetchEventDetail(code)
            .doLoading()
            .map { it.result }
            .applyApiScheduler()
            .request(success = {
                eventModel = it
                _title.value = it.title
                _imageUrl.value = it.imageUrl
                onLoadingDismiss()

            }, failure = {
                onLoadingDismiss()
                DLogger.d("ERROR $it")
                _startFinishEvent.call()
            })
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

    fun onShare() {
        eventModel?.let {
            _startShareEvent.value = it
        }
    }

    fun onParticipateEvent() {
        eventModel?.let {
            _startParticipateEvent.value = it
        }
    }
}
