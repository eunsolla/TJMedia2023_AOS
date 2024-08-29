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
import com.verse.app.extension.toIntOrDef
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.vote.VoteBody
import com.verse.app.model.vote.VoteDetailData
import com.verse.app.model.vote.VoteIntentModel
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.comment.CommentActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@HiltViewModel
class VoteParticipationFragmentViewModel @Inject constructor(
    private val apiService: ApiService,
    val loginManager: LoginManager
) : FragmentViewModel() {

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent
    private val _startShareEvent: SingleLiveEvent<VoteDetailData> by lazy { SingleLiveEvent() }
    val startShareEvent: LiveData<VoteDetailData> get() = _startShareEvent

    private val _startConfirmDialogEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startConfirmDialogEvent: LiveData<Int> get() = _startConfirmDialogEvent
    private val _startVoteDisableEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startVoteDisableEvent: LiveData<Unit> get() = _startVoteDisableEvent

    private val _startMoveToEndPageEvent: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val startMoveToEndPageEvent: LiveData<String> get() = _startMoveToEndPageEvent

    private val detail: MutableLiveData<VoteDetailData> by lazy { MutableLiveData() }
    val title: LiveData<String> get() = Transformations.map(detail) { it.title }
    val imagePath: LiveData<String> get() = Transformations.map(detail) { it.imagePath }

    val selectOneTitle: LiveData<String> get() = Transformations.map(detail) { it.selectOne }
    val selectTwoTitle: LiveData<String> get() = Transformations.map(detail) { it.selectTwo }
    val selectThreeTitle: LiveData<String> get() = Transformations.map(detail) { it.selectThree }
    val selectFourTitle: LiveData<String> get() = Transformations.map(detail) { it.selectFour }

    private val _selectedPos: MutableLiveData<Int> by lazy { MutableLiveData() }
    val selectedPos: LiveData<Int> get() = _selectedPos

    fun start() {
        val data = savedStateHandle.get<VoteIntentModel>(ExtraCode.VOTE_DETAIL_CODE)!!
        apiService.fetchVoteDetail(data.code)
            .map { it.result }
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                detail.value = it
                _selectedPos.value = it.voteItem.toIntOrDef(-1)
            }, failure = {
                DLogger.d("ERROR $it")
            })
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

    fun onSelectVote(pos: Int) {
        val detail = detail.value ?: return
        if (detail.isVote) {
            // _startVoteDisableEvent.call()
            return
        }

        if (selectedPos.value != pos) {
            _selectedPos.value = pos
            _startConfirmDialogEvent.value = pos
        }
    }

    fun onSelectedVoteConfirm(pos: Int) {
        _selectedPos.value = pos
        val detail = detail.value ?: return
        // detail.joinVoteYn = "Y"
        apiService.postVote(
            VoteBody(
                code = detail.code,
                selectedPos = pos.toString()
            )
        ).doLoading().applyApiScheduler()
            .request(success = {
                detail.joinVoteYn = "Y"
            }, failure = {
                DLogger.d("ERROR $it")
            })
    }

    fun onSelectedCancel() {
        _selectedPos.value = -1
    }
}
