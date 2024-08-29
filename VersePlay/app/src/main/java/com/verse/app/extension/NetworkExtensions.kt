package com.verse.app.extension

import androidx.lifecycle.MutableLiveData
import com.verse.app.contants.LoadingDialogState
import com.verse.app.contants.HttpStatusType
import com.verse.app.model.base.BaseResponse
import com.verse.app.utility.DLogger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableTransformer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.internal.functions.Functions
import io.reactivex.rxjava3.internal.observers.ConsumerSingleObserver
import io.reactivex.rxjava3.internal.observers.LambdaObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

/**
 * type Single
 */
inline fun <reified T : Any> Single<T>.applyApiScheduler(): Single<T> = compose { it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }

fun <T : Any> applyApiScheduler(): FlowableTransformer<T, T> {
    return FlowableTransformer { upstream: Flowable<T> ->
        upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * type Observable
 */
inline fun <reified T : Any> Observable<T>.applyApiScheduler(): Observable<T> = compose { it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }


inline fun <reified T : Any> handleNetworkErrorRetry(): SingleTransformer<T, T> =
    SingleTransformer { emit ->
        emit.retryWhen {
            it.take(3).flatMap { err ->
                when (err) {
                    is HttpException -> {
                        when (err.code()) {
                            in IntRange(201, 299) -> return@flatMap Flowable.timer(
                                500,
                                TimeUnit.MILLISECONDS
                            )

                            else -> return@flatMap Flowable.error(err)
                        }
                    }

                    else -> return@flatMap Flowable.error(err)
                }
            }
        }
    }

/**
 *  Network 공통 처리 함수.
 *  @param loadingDialog 로딩 프로그래스바 (Optional)
 *  @param errorDialog 네트워크 에러 다이얼 로그 (Optional)
 *  @param success Api Call Success 콜백 함수
 *  @param failure Api Call 실패시 콜백 함수 (Optional)
 *  @sample
 *  Case 1. -> 공통 실패 처리 후 따로 처리를 해야 하는 경우.
 *  {
 *      ApiService.Interface()
 *          .request(loadingDialog, errorDialog, { response->
 *              성공후 데이터 처리..
 *          }, {
 *              실패에 대한 후처리
 *          }
 *  }
 *  Case 2. -> 공통 실패 처리 후 처리가 없는 경우.
 *  {
 *      ApiService.Interface()
 *          .request(loadingDialog, errorDialog, { response->
 *              성공후 데이터 처리..
 *          }
 *  }
 *  @author jhlee
 */
inline fun <reified T : Any> Single<T>.request(
    loadingDialog: MutableLiveData<LoadingDialogState>? = null,
    errorDialog: MutableLiveData<HttpStatusType>? = null,
    crossinline success: (T) -> Unit,
    crossinline failure: (Throwable) -> Unit = {},
): Disposable {

    val observer: ConsumerSingleObserver<T> = ConsumerSingleObserver({ response ->
        // 로딩 프로그래스 노출된 상태면 -> 숨김
        if (LoadingDialogState.DISMISS != loadingDialog?.value) {
            loadingDialog?.postValue(LoadingDialogState.DISMISS)
        }

        // RxErrorHandlingCallAdapterFactory 에서 에러 발생시 처리하기때문에 아래 처리 로직 주석
//        if (response is BaseResponse) {
//
//            val res = response as BaseResponse
//
//            //중복 로그인 체크
//            //인증 유효 체크
//            //팝업 노출->로그아웃 후 메인 이동
//            if (res.status == HttpStatusType.DUPLICATED_LOGIN.status || res.status == HttpStatusType.NO_AUTHENTICATION.status) {
//                errorDialog?.let {
//                    res.message.let { msg ->
//                        it.value = HttpStatusType.DUPLICATED_LOGIN?.apply {
//                            this.fromServerErrMsg = msg
//                        }
//                    }
//                }
//            } else {
//                success.invoke(response)
//            }
//            success.invoke(response)
//        } else {
//            success.invoke(response)
//        }
        success.invoke(response)
    }, { throwable ->

        if (LoadingDialogState.DISMISS != loadingDialog?.value) {
            loadingDialog?.postValue(LoadingDialogState.DISMISS)
        }

//        errorDialog?.let {
//            throwable.message?.let { msg ->
//                it.value = HttpStatusType.DEFAULT?.apply {
//                    this.fromServerErrMsg = msg
//                }
//            }
//        }

        failure.invoke(throwable)
    })

    subscribe(observer)

    return observer
}


/**
 *  Network 공통 처리 함수.
 *  @param loadingDialog 로딩 프로그래스바 (Optional)
 *  @param errorDialog 네트워크 에러 다이얼 로그 (Optional)
 *  @param success Api Call Success 콜백 함수
 *  @param failure Api Call 실패시 콜백 함수 (Optional)
 *  @sample
 *  Case 1. -> 공통 실패 처리 후 따로 처리를 해야 하는 경우.
 *  {
 *      ApiService.Interface()
 *          .request(loadingDialog, errorDialog, { response->
 *              성공후 데이터 처리..
 *          }, {
 *              실패에 대한 후처리
 *          }
 *  }
 *  Case 2. -> 공통 실패 처리 후 처리가 없는 경우.
 *  {
 *      ApiService.Interface()
 *          .request(loadingDialog, errorDialog, { response->
 *              성공후 데이터 처리..
 *          }
 *  }
 *  @author jhlee
 */
inline fun <reified T : Any> Observable<T>.request(
    loadingDialog: MutableLiveData<LoadingDialogState>? = null,
    errorDialog: MutableLiveData<HttpStatusType>? = null,
    crossinline onNext: (T) -> Unit,
    crossinline onError: (Throwable) -> Unit = {},
    crossinline onComplete: () -> Unit = {},
): Disposable {

    if (LoadingDialogState.DISMISS != loadingDialog?.value) {
        loadingDialog?.postValue(LoadingDialogState.DISMISS)
    }

    val next = Consumer<T> { response ->
        onNext.invoke(response)
    }

    val throwable = Consumer<Throwable> { error ->

//        errorDialog?.let {
//            error.message?.let { msg ->
//                it.value = HttpStatusType.DEFAULT?.apply {
//                    this.fromServerErrMsg = msg
//                }
//            }
//        }

        onError.invoke(error)
    }

    val complete = Action {
        onComplete.invoke()
    }

    val ls = LambdaObserver(next, throwable, complete, Functions.emptyConsumer())
    subscribe(ls)

    return ls
}

inline fun <reified T : Any> Flowable<T>.request(
    loadingDialog: MutableLiveData<LoadingDialogState>? = null,
    errorDialog: MutableLiveData<HttpStatusType>? = null,
    crossinline onNext: (T) -> Unit,
    crossinline onError: (Throwable) -> Unit = {},
    crossinline onComplete: () -> Unit = {},
): Disposable {

    if (LoadingDialogState.DISMISS != loadingDialog?.value) {
        loadingDialog?.postValue(LoadingDialogState.DISMISS)
    }

    val next = Consumer<T> { response ->
        onNext.invoke(response)
    }

    val throwable = Consumer<Throwable> { error ->

//        errorDialog?.let {
//            error.message?.let { msg ->
//                it.value = HttpStatusType.DEFAULT?.apply {
//                    this.fromServerErrMsg = msg
//                }
//            }
//        }

        onError.invoke(error)
    }

    val complete = Action {
        onComplete.invoke()
    }

    return subscribe(next, throwable, complete)
}

