package com.verse.app.base.viewmodel

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.LoadingDialogState
import com.verse.app.contants.ServerCheckState
import com.verse.app.extension.request
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.encode.EncodeData
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.utility.exo.ExoStyledPlayerView
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo

/**
 * Description : BaseViewModel Class
 *
 * Created by jhlee on 2023-01-01
 */
open class BaseViewModel : ViewModel() {

    protected open val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    val startLoadingDialog: SingleLiveEvent<LoadingDialogState> by lazy { SingleLiveEvent() }
    val showNetWorkErrorDialog: SingleLiveEvent<HttpStatusType> by lazy { SingleLiveEvent() }
    val showServerCheckDialog: SingleLiveEvent<ServerCheckState> by lazy { SingleLiveEvent() }
    val showToastStringMsg: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val showToastIntMsg: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    //피드 컨텐츠 업로드 대상 정보
    val _reEncodeData: MutableLiveData<EncodeData> by lazy { MutableLiveData() }
    val reEncodeData: LiveData<EncodeData> get() = _reEncodeData

    /**
     * RxJava 로 작업 요청시 로딩바 보이게 하기 위한 확장 함수.
     * subscribeOn 보다 위에 있어야 한다.
     * Type Single
     */
    protected fun <T : Any, S : Single<T>> S.doLoading(): Single<T> =
        doOnSubscribe { onLoadingShow() }

    /**
     * RxJava 로 작업 요청시 로딩바 보이게 하기 위한 확장 함수.
     * subscribeOn 보다 위에 있어야 한다.
     * Type Observable
     */
    protected fun <T : Any, S : Observable<T>> S.doLoading(): Observable<T> =
        doOnSubscribe { onLoadingShow() }

    /**
     * 로딩 다이얼로그 Show
     */
    fun onLoadingShow() {
        if (startLoadingDialog.value != LoadingDialogState.SHOW) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                startLoadingDialog.value = LoadingDialogState.SHOW
            } else {
                startLoadingDialog.postValue(LoadingDialogState.SHOW)
            }
        }
    }

    /**
     * 로딩 다이얼로그 Dismiss
     */
    fun onLoadingDismiss() {
        if (startLoadingDialog.value != LoadingDialogState.DISMISS || startLoadingDialog.value == null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                startLoadingDialog.value = LoadingDialogState.DISMISS
            } else {
                startLoadingDialog.postValue(LoadingDialogState.DISMISS)
            }
        }
    }

    /**
     * 에러 다이얼로그 Show
     */
    fun showNetworkErrorDialog(errorMsg: String? = null) {
        errorMsg?.let {
            showNetWorkErrorDialog.value = HttpStatusType.DEFAULT.apply {
                fromServerErrMsg = it
            }
        } ?: run {
            showNetWorkErrorDialog.value = HttpStatusType.DEFAULT
        }
    }

    /**
     * 서버 점검 다이얼로그 Show
     */
    fun showServerCheckDialog() {
        if (showServerCheckDialog.value != ServerCheckState.SHOW) {
            showServerCheckDialog.postValue(ServerCheckState.SHOW)
        }
    }

    /**
     * 서버 점검 다이얼로그 Dismiss
     */
    fun dismissServerCheckDialog() {
        if (showServerCheckDialog.value != ServerCheckState.DISMISS) {
            showServerCheckDialog.postValue(ServerCheckState.DISMISS)
        }
    }

    /**
     * ViewModel
     * 네트워크 공통 처리 함수.
     * 쓰레드 세팅 자동 io, UiThread 타입.
     * @param success Api Call Success 콜백 고차 함수.
     * @param failure Api Call 실패시 콜백 고차 함수.
     * @author jhlee
     */
    protected inline fun <reified T : Any> Single<T>.request(
        crossinline success: (T) -> Unit = {},
        crossinline failure: (Throwable) -> Unit = {},
    ) {
        request(
            loadingDialog = startLoadingDialog,
            errorDialog = showNetWorkErrorDialog,
            success = success,
            failure = failure
        ).addTo(compositeDisposable)
    }

    /**
     * ViewModel
     * 네트워크 공통 처리 함수.
     * 쓰레드 세팅 자동 io, UiThread 타입.
     * @param success Api Call Success 콜백 고차 함수.
     * @param failure Api Call 실패시 콜백 고차 함수.
     * @author jhlee
     */
    protected inline fun <reified T : Any> Observable<T>.request(
        crossinline next: (T) -> Unit = {},
        crossinline error: (Throwable) -> Unit = {},
        crossinline complete: () -> Unit = {}
    ) {
        request(
            loadingDialog = startLoadingDialog,
            errorDialog = showNetWorkErrorDialog,
            onNext = next,
            onError = error,
            onComplete = complete
        ).addTo(compositeDisposable)
    }

    /**
     * ViewModel
     * 네트워크 공통 처리 함수.
     * 쓰레드 세팅 자동 io, UiThread 타입.
     * @param success Api Call Success 콜백 고차 함수.
     * @param failure Api Call 실패시 콜백 고차 함수.
     * @author jhlee
     */
    protected inline fun <reified T : Any> Flowable<T>.request(
        crossinline next: (T) -> Unit = {},
        crossinline error: (Throwable) -> Unit = {},
        crossinline complete: () -> Unit = {}
    ) {
        request(
            loadingDialog = startLoadingDialog,
            errorDialog = showNetWorkErrorDialog,
            onNext = next,
            onError = error,
            onComplete = complete
        ).addTo(compositeDisposable)
    }


    /**
     * Activity / Fragment 에서 onDestroy 시 리소스들 정리
     * clear
     */
    fun clearDisposable() {
        compositeDisposable.clear()
    }

    /**
     * ViewModel 이 제거 되는 시점
     */
    override fun onCleared() {
        super.onCleared()
        // Disposable 구독 해제.
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    /**
     * Set Player Map
     */
    open fun setPlayerView(playerView: ExoStyledPlayerView) {}

    /**
     * 검색 결과 피드 상세 화면 이동 처리
     */
    open fun moveToFeedDetail(index: Int) {}

    /**
     * 검색 결과 피드 상세 화면 이동 처리
     */
    open fun moveToFeedDetail(index: Int, dataList: PagingData<FeedContentsData>) {}

    open fun moveToFeedDetail(index: Int, mngCd:String, dataList: PagingData<FeedContentsData>) {}

    /**
     * 플레이어 play and plause
     */
    open fun togglePlayAndPause() {
    }

    open fun moveToNextFeed(){}

    fun addDisposable(work: Disposable) {
        compositeDisposable.add(work)
    }
}