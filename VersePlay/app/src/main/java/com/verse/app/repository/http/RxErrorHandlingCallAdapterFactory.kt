package com.verse.app.repository.http

import android.app.DownloadManager.Request
import com.verse.app.contants.Config
import com.verse.app.contants.HttpStatusType
import com.verse.app.model.base.BaseResponse
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.http.Url
import java.lang.reflect.Type

/**
 * Description : Rx Error Handling 처리하는 CallAdapter
 *
 * Created by juhongmin on 2023/06/06
 */
class RxErrorHandlingCallAdapterFactory : CallAdapter.Factory() {

    private val original: RxJava3CallAdapterFactory = RxJava3CallAdapterFactory
        .createWithScheduler(Schedulers.io())

    companion object {
        fun create(): CallAdapter.Factory {
            return RxErrorHandlingCallAdapterFactory()
        }
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val adapter = original.get(returnType, annotations, retrofit)
        return if (adapter != null) {
            RxJavaCallAdapterWrapper(adapter)
        } else {
            null
        }
    }

    inner class RxJavaCallAdapterWrapper<R>(
        private val original: CallAdapter<R, *>
    ) : CallAdapter<R, Any> {

        override fun responseType(): Type = original.responseType()

        override fun adapt(call: Call<R>): Any {
            return when (val res = original.adapt(call)) {
                is Single<*> -> {
                    res.map {
                        handleHttpError(it)
                    }.doOnError {
                        DLogger.d("Single doOnError")
                        // url.encodedPath에 맞게 rxbus 호출 스킵하는 함수 생성
                        isRxBusSkip(call.request().url.encodedPath)
                    }
                }

                is Flowable<*> -> {
                    res.map {
                        handleHttpError(it)
                    }.doOnError {
                        DLogger.d("Flowable doOnError")
                        RxBus.publish(RxBusEvent.HttpStatusErrorEvent(HttpStatusType.DEFAULT))
                    }
                }

                else -> {
                    DLogger.d("else doOnError")
                    RxBus.publish(RxBusEvent.HttpStatusErrorEvent(HttpStatusType.DEFAULT))
                    throw IllegalArgumentException("Not Invalid Type")
                }
            }
        }

        private fun handleHttpError(res: Any): Any {
            // 특정 status code 가 발생시 에러를 변환하거나 여기서 자유롭게 컨트롤 할수 있음.
            return if (res is BaseResponse) {
                DLogger.d("res is BaseResponse : ${res}")

                // TEST CODE
//                if (call.request().url.encodedPath.startsWith("/api/v2/community/getMainBannerInfo")) {
//                    val httpStatus = HttpStatusType.DUPLICATED_LOGIN.apply {
//                        fromServerErrMsg = res.message
//                    }
//                    RxBus.publish(RxBusEvent.HttpStatusErrorEvent(httpStatus))
//                }

                // 중복 로그인, 인증 유효 체크
                if (res.status == HttpStatusType.DUPLICATED_LOGIN.status ||
                    res.status == HttpStatusType.NO_AUTHENTICATION.status
                ) {
                    val httpStatus = HttpStatusType.DUPLICATED_LOGIN.apply {
                        fromServerErrMsg = res.message
                    }
                    RxBus.publish(RxBusEvent.HttpStatusErrorEvent(httpStatus))
                }
                res
            } else {
                res
            }
        }

        private fun isRxBusSkip(encodePath: String) {
            // 업로드 관련 api에서는 네트워크 관련 에러 팝업 노출하지 않도록 함
            if (!encodePath.startsWith(Config.API_VERSION + "common/getResourcePath")
                && !encodePath.startsWith(Config.API_VERSION + "contents/uploadFeed")
                && !encodePath.startsWith(Config.API_VERSION + "contents/requestSingHistory")
                && !encodePath.startsWith(Config.API_VERSION + "contents/viewFeedContentsHistory")
                && !encodePath.startsWith(Config.API_VERSION + "contents/completedSong")
                && !encodePath.startsWith(Config.API_VERSION + "contents/viewAdContents")
            ) {

                val isExit = encodePath.startsWith(Config.API_VERSION + "common/getAppVersionInfo")
                RxBus.publish(
                    RxBusEvent.HttpStatusErrorEvent(
                        HttpStatusType.DEFAULT,
                        isExitApp = isExit
                    )
                )
            }
        }

    }
}
