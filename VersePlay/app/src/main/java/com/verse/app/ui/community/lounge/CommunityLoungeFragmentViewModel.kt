package com.verse.app.ui.community.lounge

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.contants.*
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.community.CommunityLoungeData
import com.verse.app.model.empty.EmptyData
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.model.param.BlockBody
import com.verse.app.model.param.LoungeQueryMap
import com.verse.app.model.param.ReportParam
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.dialogfragment.ReportDialogFragment
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.lounge.LoungeRootActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.WebLinkProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : 커뮤니티 > [라운지]
 *
 * Created by juhongmin on 2023/05/16
 */
@HiltViewModel
class CommunityLoungeFragmentViewModel @Inject constructor(
    private val apiService: ApiService,
    public val loginManager: LoginManager,
    private val linkProvider: WebLinkProvider
) : FragmentViewModel(),
    ReportDialogFragment.Listener {

    private val _dataList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val dataList: ListLiveData<BaseModel> get() = _dataList
    private val _isEmpty: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    private val _startReportDialogEvent: SingleLiveEvent<CommunityLoungeData> by lazy { SingleLiveEvent() }
    val startReportDialogEvent: LiveData<CommunityLoungeData> get() = _startReportDialogEvent
    val startActivityReportEvent: SingleLiveEvent<ReportParam> by lazy { SingleLiveEvent() }

    val startScrollTop: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    // [s] Parameter
    val pagingModel: PagingModel by lazy { PagingModel() }
    private val queryMap: LoungeQueryMap by lazy { LoungeQueryMap() }
    // [e] Parameter

    val refresh: SingleLiveEvent<Pair<Int, CommunityLoungeData>> by lazy { SingleLiveEvent() }
    val startCheckPrivateAccount: SingleLiveEvent<String> by lazy { SingleLiveEvent() } // 비공개 계정 관련 팝업

    fun initStart() {
        RxBus.listen(RxBusEvent.LoungeRefreshEvent::class.java)
            .delay(500, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .doOnNext { refresh(it.isTop) }
            .subscribe().addTo(compositeDisposable)
    }

    fun loungeLikeRefresh() {
        RxBus.listen(RxBusEvent.LoungeLikeRefreshEvent::class.java)
            .delay(500, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .doOnNext { refresh(it.isTop) }
            .subscribe().addTo(compositeDisposable)
    }

    fun start() {
        apiService.fetchLounges(queryMap)
            .doLoading()
            .map { it.result.list }
            .flatMap { handleLinkThumbnail(it) }
            .applyApiScheduler()
            .request(success = { handleOnSuccess(it,true) }, failure = { handleOnError(it) })
    }

    fun refresh(isTop:Boolean) {
        _dataList.clear()
        queryMap.pageNo = 1
        pagingModel.initParams()
        apiService.fetchLounges(queryMap)
            .map { it.result.list }
            .flatMap { handleLinkThumbnail(it) }
            .applyApiScheduler()
            .request(
                success = { handleOnSuccess(it,isTop) },
                failure = { handleOnError(it) })
    }

    private fun handleOnSuccess(list: List<BaseModel>,isTop: Boolean) {
        if (queryMap.pageNo == 1) {
            if (list.isEmpty()) {
                _dataList.add(EmptyData())
            }
        }
        _dataList.addAll(list)
        queryMap.pageNo++
        pagingModel.isLoading = false
        pagingModel.isLast = list.isEmpty()

        if(isTop){
            startScrollTop.call()
        }
        onLoadingDismiss()
    }

    private fun handleOnError(err: Throwable) {
        if (queryMap.pageNo == 1) {
            _dataList.add(EmptyData())
        }
        onLoadingDismiss()
        DLogger.d("ERROR $err")
    }

    fun onLoadPage() {
        reqList(queryMap)
    }

    /**
     * 반주음 API 요청하는 함수
     */
    private fun reqList(queryMap: LoungeQueryMap) {
        apiService.fetchLounges(queryMap)
            .doOnSubscribe { pagingModel.isLoading = true }
            .map { it.result.list }
            .flatMap { handleLinkThumbnail(it) }
            .applyApiScheduler()
            .doOnSuccess { handleOnSuccess(it,false) }
            .doOnError { handleOnError(it) }
            .subscribe().addTo(compositeDisposable)
    }

    fun onMoveToDetail(data: CommunityLoungeData) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        DLogger.d("onMoveToDetail $data")
        moveToPage(
            ActivityResult(
                targetActivity = LoungeRootActivity::class,
                data = bundleOf(ExtraCode.WRITE_LOUNGE_DATA to LoungeIntentModel(data))
            )
        )
    }

    fun onDeclarationReport(data: CommunityLoungeData) {
        DLogger.d("onDeclarationReport $data")
        _startReportDialogEvent.value = data
    }

    private fun handleLinkThumbnail(list: List<CommunityLoungeData>): Single<List<CommunityLoungeData>> {
        val works = mutableListOf<Single<CommunityLoungeData>>()
        list.forEach { item ->
            if (item.code.isNotEmpty()) {
                works.add(linkProvider.getLinkMeta(item.linkUrl)
                    .map {
                        item.linkThumbnailUrl = it.imageUrl
                        item.linkTitle = it.title
                        item
                    }.onErrorReturn { item })
            }
        }
        return Single.zip(works) {
            return@zip list
        }.subscribeOn(Schedulers.io())
    }

    /**
     * 차단 콜백
     */
    override fun onBlockConfirm(data: ReportDialogFragment.ReportData) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        DLogger.d("onBlockConfirm $data")
        val blockBody = BlockBody(
            blockContentCode = data.manageCode,
            blockYn = AppData.Y_VALUE,
            blockType = BlockType.COMMUNITY.code
        )
        apiService.updateBlock(blockBody)
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                refresh(false)
            })
    }

    /**
     * 신고 콜백
     */
    override fun onReportConfirm(data: ReportDialogFragment.ReportData) {
        DLogger.d("onReportConfirm $data")
        val reportParam = ReportParam(
            repTpCd = ReportType.LOUNGE.code,
            repMngCd = data.manageCode,
            repConCd = "",
            fgLoginYn = loginManager.isLoginYN()
        )

        startActivityReportEvent.value = reportParam
    }

}
