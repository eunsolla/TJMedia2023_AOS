package com.verse.app.ui.lounge.detail

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.CommentType
import com.verse.app.contants.Config
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.LikeType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.community.CommunityLoungeData
import com.verse.app.model.lounge.LoungeDetailData
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.model.lounge.LoungeModifyImageData
import com.verse.app.model.lounge.LoungeModifyLinkData
import com.verse.app.model.lounge.LoungeModifyTextData
import com.verse.app.model.param.LikeBody
import com.verse.app.model.param.LoungeBody
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.comment.CommentActivity
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.WebLinkProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/20
 */
@HiltViewModel
class LoungeDetailFragmentViewModel @Inject constructor(
    private val apiService: ApiService,
    private val webLinkProvider: WebLinkProvider,
    private val loginManager: LoginManager
) : FragmentViewModel() {

    private val _dataList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val dataList: ListLiveData<BaseModel> get() = _dataList

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    private val _startFinishModifyEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishModifyEvent: LiveData<Unit> get() = _startFinishModifyEvent

    private val _startModifyPageEvent: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val startModifyPageEvent: LiveData<String> get() = _startModifyPageEvent

    private val detail: MutableLiveData<LoungeDetailData> by lazy { MutableLiveData() }

//    val likeCountText: LiveData<String> get() = Transformations.map(detail) { it.likeCount.toString() }

    private val _likeCountTxt: MutableLiveData<String> by lazy { MutableLiveData() }
    val likeCountTxt: LiveData<String> get() = _likeCountTxt

//    val likeYn: LiveData<String> get() = Transformations.map(detail) { it.likeYn }

    private val _likeeYn: MutableLiveData<String> by lazy { MutableLiveData() }
    val likeeYn: LiveData<String> get() = _likeeYn

    val commentCountText: LiveData<String> get() = Transformations.map(detail) { it.commentCnt }

    var loungeCode = ""

    private val _isModify: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isModify: LiveData<Boolean> get() = _isModify

    val moveToLinkUrl: SingleLiveEvent<String> by lazy { SingleLiveEvent() } // 링크 URL 이동 처리

    val refresh: SingleLiveEvent<Pair<Int, CommunityLoungeData>> by lazy { SingleLiveEvent() }
    val startCheckPrivateAccount: SingleLiveEvent<String> by lazy { SingleLiveEvent() } // 비공개 계정 관련 팝업


    fun initStart() {
        RxBus.listen(RxBusEvent.LoungeRefreshEvent::class.java)
            .delay(500, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .doOnNext { refresh() }
            .subscribe().addTo(compositeDisposable)
    }

    fun start() {
        val data: LoungeIntentModel = savedStateHandle[ExtraCode.WRITE_LOUNGE_DATA]!!
        val info = data.modifyInfo ?: return
        _isModify.value = loginManager.getUserLoginData().memCd == info.memberCode
        apiService.fetchLoungeDetail(info.code)
            .doLoading()
            .map { it.result }
            .map {
                if (it.code.isEmpty()) {
                    throw IllegalArgumentException("유효한 코드가 아닙니다.")
                } else {
                    it
                }
            }
            .map { it to getCommonUiModels(it) }
            .applyApiScheduler()
            .request(success = {
                _dataList.addAll(it.second)
                detail.value = it.first
                _likeCountTxt.value = it.first.likeCount.toString()
                _likeeYn.value = it.first.likeYn
                loungeCode = it.first.code

                DLogger.d("###lounge      $loungeCode ${likeCountTxt.value} ${likeeYn.value}")

                handleLinkUrlUiModel(it.first)
                onLoadingDismiss()
            }, failure = {
                DLogger.d("ERROR $it")
                onLoadingDismiss()
                _startFinishEvent.call()
            })
    }

    fun refresh() {
        detail.value?.let {detailData->
            _dataList.clear()
            apiService.fetchLoungeDetail(detailData.code)
                .map { it.result }
                .map {
                    if (it.code.isEmpty()) {
                        throw IllegalArgumentException("유효한 코드가 아닙니다.")
                    } else {
                        it
                    }
                }
                .map { it to getCommonUiModels(it) }
                .applyApiScheduler()
                .request(success = {
                    _dataList.addAll(it.second)
                    detail.value = it.first
                    handleLinkUrlUiModel(it.first)
                }, failure = {
                    DLogger.d("ERROR $it")
                    _startFinishEvent.call()
                })
        }
    }


    private fun getCommonUiModels(data: LoungeDetailData): List<BaseModel> {
        val list = mutableListOf<BaseModel>()
        list.add(LoungeModifyTextData(contents = data.contents))
        data.getImageList().forEachIndexed { idx, s ->
            val imageUri = Uri.parse(Config.BASE_FILE_URL.plus(s))
            val imagePath = Config.BASE_FILE_URL.plus(imageUri.path)
            list.add(LoungeModifyImageData(idx, imagePath))
        }
        return list
    }

    private fun handleLinkUrlUiModel(data: LoungeDetailData) {
        if (!data.linkUrl.isNullOrEmpty()) {
            webLinkProvider.getLinkMeta(data.linkUrl)
                .applyApiScheduler()
                .doOnSuccess {
                    _dataList.add(LoungeModifyLinkData(it))
                }
                .subscribe()
        }
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

    fun onModify() {
        // 일단 주석 처리
        _startModifyPageEvent.value = detail.value?.code
    }

    fun deleteMyLounge(code: String) {
        apiService.deleteLoungeContents(LoungeBody(code = code))
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                RxBus.publish(RxBusEvent.LoungeRefreshEvent())
                onLoadingDismiss()
                _startFinishModifyEvent.call()
            }, failure = {
                onLoadingDismiss()
                DLogger.d("ERROR $it")
                _startFinishEvent.call()
            })
    }

    fun moveToCommentPage() {
        val data = detail.value ?: return
        val page = ActivityResult(
            targetActivity = CommentActivity::class,
            data = bundleOf(ExtraCode.COMMENT_TYPE to (CommentType.LOUNGE to data.code))
        )
        moveToPage(page)
    }

    /**
     * 라운지 글 좋아요
     */
    fun onLikeLounge() {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)
            return
        }

        val likeYn = if (!detail.value!!.isLike) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }

        onLike(likeYn, LikeType.LOUNGE.code, loungeCode) { result ->

            if (result) {
                detail.value!!.likeYn = likeYn
                _likeeYn.value = likeYn

                if (detail.value!!.isLike) {
                    detail.value!!.likeCount = detail.value!!.likeCount + 1
                    _likeCountTxt.value = detail.value!!.likeCount.toString()
                } else {
                    detail.value!!.likeCount = detail.value!!.likeCount - 1
                    _likeCountTxt.value = detail.value!!.likeCount.toString()
                }
            }
        }
    }

    private fun onLike(
        likeYn: String,
        likeType: String,
        conMngCd: String,
        result: (Boolean) -> Unit
    ) {
        apiService.updateLike(
            LikeBody(
                likeType = likeType,
                likeYn = likeYn,
                contentsCode = conMngCd
            )
        )
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    result.invoke(true)
                    RxBus.publish(RxBusEvent.LoungeLikeRefreshEvent(isTop = false))
                } else if (it.status == HttpStatusType.FAIL.status) {
                    startCheckPrivateAccount.value = it.message
                }
            }, {
                DLogger.d("Error Like=>${it.message}")
            })
    }

    fun moveToLinkUrl(url: String) {
        moveToLinkUrl.value = url
    }
}
