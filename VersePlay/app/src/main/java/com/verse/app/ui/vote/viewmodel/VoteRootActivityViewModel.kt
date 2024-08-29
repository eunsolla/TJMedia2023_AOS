package com.verse.app.ui.vote.viewmodel

import androidx.lifecycle.LiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.vote.VoteIntentModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@HiltViewModel
class VoteRootActivityViewModel @Inject constructor(

) : ActivityViewModel() {
    private val _startMoveVotePage: SingleLiveEvent<VoteIntentModel> by lazy { SingleLiveEvent() }
    val startMoveVotePage: LiveData<VoteIntentModel> get() = _startMoveVotePage

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    fun start() {
        val data: VoteIntentModel? = savedStateHandle[ExtraCode.VOTE_DETAIL_CODE]
        if (data == null) {
            _startFinishEvent.call()
            return
        }
        _startMoveVotePage.value = data
    }
}
