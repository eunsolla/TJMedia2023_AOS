package com.verse.app.ui.vote.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.CommentType
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.vote.VoteIntentModel
import com.verse.app.model.vote.VoteResultData
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.comment.CommentActivity
import com.verse.app.utility.DLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@HiltViewModel
class VoteEndFragmentViewModel @Inject constructor(
    private val apiService: ApiService
) : FragmentViewModel() {

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent
    private val _startShareEvent: SingleLiveEvent<VoteResultData> by lazy { SingleLiveEvent() }
    val startShareEvent: LiveData<VoteResultData> get() = _startShareEvent

    private val detail: MutableLiveData<VoteResultData> by lazy { MutableLiveData() }
    val title: LiveData<String> get() = Transformations.map(detail) { it.title }
    val imagePath: LiveData<String> get() = Transformations.map(detail) { it.imagePath }

    val firstVote: LiveData<VoteResultData.VoteRaking> get() = Transformations.map(detail) { it.firstInfo }
    val secondVote: LiveData<VoteResultData.VoteRaking> get() = Transformations.map(detail) { it.secondInfo }
    val thirdVote: LiveData<VoteResultData.VoteRaking> get() = Transformations.map(detail) { it.thirdInfo }
    val fourthVote: LiveData<VoteResultData.VoteRaking> get() = Transformations.map(detail) { it.fourthInfo }

    fun start() {
        val data = savedStateHandle.get<VoteIntentModel>(ExtraCode.VOTE_DETAIL_CODE)!!
        // val code = "CMVO993ee40d8c114c7688c7d2898e27b55f284885"
        // CMVO2c172a95f0874058ac0cf2d2666af8f3735602
        apiService.fetchVoteResult(data.code)
            .map { it.result }
            .map { handleCalculateRanking(it) }
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                detail.value = it
            }, failure = {
                DLogger.d("ERROR $it")
            })
    }

    private fun handleCalculateRanking(result: VoteResultData): VoteResultData {
        result.calculateRanking()
        return result
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

    fun onShare() {
        detail.value?.let {
            _startShareEvent.value = it
        }
    }

    fun moveToComment() {
        val data = detail.value ?: return
        val page = ActivityResult(
            targetActivity = CommentActivity::class,
            data = bundleOf(ExtraCode.COMMENT_TYPE to (CommentType.COMMUNITY_VOTE to data.code))
        )
        moveToPage(page)
    }
}
