package com.verse.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.verse.app.contants.AppData
import com.verse.app.tracking.ui.TrackingManager
import com.verse.app.ui.fake.InternalFakeActivity
import com.verse.app.ui.intro.activity.IntroActivity
import com.verse.app.ui.main.MainActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.SocketException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : MyApplication Class
 *
 * Created by jhlee on 2023-01-01
 */
@HiltAndroidApp
class MainApplication : MultiDexApplication(), androidx.work.Configuration.Provider {

    enum class AppStatus {
        BACKGROUND, // 앱 Background 상태
        RETURNED_TO_FOREGROUND, // Background -> ForeGournd 올라온경우
        FOREGROUND
    }

    companion object {
        var gAppStatus = AppStatus.BACKGROUND
    }

    var introActivity: WeakReference<IntroActivity>? = null
    var mainActivity: WeakReference<MainActivity>? = null

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var loginManager: LoginManager

//    @Inject
//    lateinit var accountPref: AccountPref

    override fun getWorkManagerConfiguration() =
        androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeApp()
        initRxJava()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        // initTracking()
        initErrorTracking()
    }

    private fun initializeApp(){
        //Firebase SDK
        FirebaseApp.initializeApp(this)
        //Naver SDK
        loginManager.initializeNaver()
        //Kakao SDK
        loginManager.initializeKakao()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityLifecycleCallbacks.currentActivity?.get()?.run {
            runCatching {
//                exitApp()
            }
        }
    }

    /**
     * reactivex.exceptions.UndeliverableException 처리 함수.
     * reactivex.exceptions.UndeliverableException
     *  Ref https://thdev.tech/android/2019/03/04/RxJava2-Error-handling/
     */
    private fun initRxJava() {

        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            DLogger.d("RxError $error")
            if (error is UndeliverableException) {
                error = e.cause!!
            }
            if (error is SocketException || error is IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (error is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (error is NullPointerException || error is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    error
                )
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    error
                )
                return@setErrorHandler
            }
        }
    }

    /**
     * Activity Memory 참조 해제.
     */
    fun unregisterActivity(activity: Activity?) {
        if (activity is MainActivity) {
            mainActivity = null
        } else if (activity is IntroActivity) {
            introActivity = null
        }
    }

    /**
     * Activity 참조.
     */
    fun registerActivity(activity: Activity) {
        if (activity is MainActivity) {
            mainActivity = WeakReference(activity)
        } else if (activity is IntroActivity) {
            introActivity = WeakReference(activity)
        }
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        var running = 0
        var currentActivity: WeakReference<Activity>? = null

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is MainActivity || activity is IntroActivity) {
                registerActivity(activity)
            }
            currentActivity?.clear()
            currentActivity = WeakReference(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            if (++running == 1) {
                gAppStatus = AppStatus.RETURNED_TO_FOREGROUND
            } else if (running > 1) {
                gAppStatus = AppStatus.FOREGROUND
                AppData.isOnApp = true
            }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            if (--running == 0) {
                gAppStatus = AppStatus.BACKGROUND
                AppData.isOnApp = false
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity is IntroActivity) {
                unregisterActivity(activity)
            }
        }
    }

    private fun initTracking() {
        TrackingManager.getInstance()
            .setLogMaxSize(100)
            .setWifiShare(BuildConfig.DEBUG)
            .setBuildType(BuildConfig.DEBUG)
            .build(this)
    }

    /**
     * 네트워크 에러 발생시 공통으로 처리하는 함수
     */
    private fun initErrorTracking() {
        // 특정 상태코드 전달시 InternalFakeActivity in NetworkErrorDialog 처리하는 함수
        // 이벤트가 여러번 들어올수 있으니 debounce 로 1.5초간 이벤트 발생이 없는 경우 최신값을 뱉는 걸로 처리
        RxBus.listen(RxBusEvent.HttpStatusErrorEvent::class.java)
            .toFlowable(BackpressureStrategy.BUFFER)
            .debounce(1500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                val intent = Intent(this, InternalFakeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(InternalFakeActivity.ERROR_STATUS, it.type)
                intent.putExtra(InternalFakeActivity.IS_EXIT, it.isExitApp)
                this.startActivity(intent)
            }
            .doOnError {
                DLogger.d("ERROR? $it")
            }
            .subscribe()
    }
}