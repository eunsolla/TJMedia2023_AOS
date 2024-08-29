package com.verse.app.utility.provider

import android.content.Context
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.repository.http.ProgressResponseBody
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.tracking.interceptor.TrackingHttpInterceptor
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


/**
 * Description : Retrofit Http Provider Class
 *
 * Created by jhlee on 2023-01-01
 */
interface RetrofitProvider {
    fun createClient(): OkHttpClient
    fun createProgressClient(onUpdate: (Int) -> Unit): OkHttpClient
}

class RetrofitProviderImpl(
    val context:Context,
    val accountPref: AccountPref,
    private val trackingInterceptor: TrackingHttpInterceptor,
    private val retrofitLogger: RetrofitLogger
) : RetrofitProvider {

    /**
     * 헤더관련 Interceptor 함수.
     */
    private fun headerInterceptor() = Interceptor { chain ->
        val origin = chain.request()
        chain.proceed(origin.newBuilder().apply {
            header("Accept", "application/json; charset=utf-8")
            header("Content-Type", "application/json; charset=utf-8")
            header("os-type", AppData.OS)
            header("nation-cd", accountPref.getLocaleCountry(context))
            header("lang-cd", accountPref.isPreferenceLocaleLanguage())
            if(accountPref.getJWTToken().isNotEmpty()){
                header("Authorization", accountPref.getJWTToken())
            }
            method(origin.method, origin.body)
        }.build())
    }

    override fun createClient() = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(false)
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(15, TimeUnit.SECONDS)
        writeTimeout(15, TimeUnit.SECONDS)
        connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
        addInterceptor(headerInterceptor())
        if (Config.IS_DEBUG) {
            addInterceptor(trackingInterceptor)
            addInterceptor(retrofitLogger)
        }
    }.build()

    override fun createProgressClient(onUpdate: (Int) -> Unit) = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(false)
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(15, TimeUnit.SECONDS)
        writeTimeout(15, TimeUnit.SECONDS)
        connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
        addInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(originalResponse.body?.let { ProgressResponseBody(it, onUpdate) })
                .build()
        }
        if (Config.IS_DEBUG) {
            addInterceptor(trackingInterceptor)
            addInterceptor(retrofitLogger)
        }
    }.build()
}