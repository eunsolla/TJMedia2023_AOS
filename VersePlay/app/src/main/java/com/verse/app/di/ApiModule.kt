package com.verse.app.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.verse.app.contants.Config
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.http.RxErrorHandlingCallAdapterFactory
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.tracking.interceptor.TrackingHttpInterceptor
import com.verse.app.utility.provider.RetrofitLogger
import com.verse.app.utility.provider.RetrofitProvider
import com.verse.app.utility.provider.RetrofitProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * API 모듈
 * Created by jhlee on 2023-02-01
 */
@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideTrackingInterceptor(): TrackingHttpInterceptor {
        return TrackingHttpInterceptor()
    }

    @Singleton
    @Provides
    fun provideRetrofitLogger(): RetrofitLogger {
        return RetrofitLogger()
    }

    @Singleton
    @Provides
    fun provideHttpClient(
        accountPref: AccountPref,
        trackingHttpInterceptor: TrackingHttpInterceptor,
        retrofitLogger: RetrofitLogger,
        @ApplicationContext context:Context
    ): RetrofitProvider {
        return RetrofitProviderImpl(context,accountPref, trackingHttpInterceptor, retrofitLogger)
    }

    @ExperimentalSerializationApi
    @Singleton
    @Provides
    fun provideJson(): Json {
        return Json {
            isLenient = true // "", 잘못된 타입 처리
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // 잘못된 형태 값 default Argument 처리
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Singleton
    @Provides
    fun provideApiService(
        retrofitProvider: RetrofitProvider,
        json: Json
    ): ApiService {
        return Retrofit.Builder()
            .baseUrl(Config.BASE_API_URL)
            .client(retrofitProvider.createClient())
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(ApiService::class.java)
    }
}