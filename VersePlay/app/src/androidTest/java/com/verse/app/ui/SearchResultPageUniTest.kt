package com.verse.app.ui

import android.content.Intent
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.verse.app.contants.ExtraCode
import com.verse.app.ui.search.activity.SearchResultActivity
import com.verse.app.utility.DLogger
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.net.SocketException

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/25
 */
@HiltAndroidTest
class SearchResultPageUniTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init(){
        hiltRule.inject()
        initRxJava()
    }

    private fun initRxJava() {

        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            DLogger.d("RxError $error")
            if (error is UndeliverableException) {
                error = e.cause!!
            }
            if (error is IOException || error is SocketException) {
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

    @Test
    fun 검색_결과_페이지_이동(){
        val keyword = "One Call Away"
        val pos = 3
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(appContext,SearchResultActivity::class.java).apply {
            putExtra(ExtraCode.SEARCH_KEYWORD,keyword)
            putExtra(ExtraCode.SEARCH_RESULT_POS,pos)
        }
        launchActivity<SearchResultActivity>(intent)

        Thread.sleep(20_000)
    }
}
