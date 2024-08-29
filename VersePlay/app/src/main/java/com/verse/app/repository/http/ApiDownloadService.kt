package com.verse.app.repository.http

import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * API Class
 * fetch{api_name]():Single<Response>
 * fetch{api_name]List():Single<Response>
 */
interface ApiDownloadService {
    @Streaming
    @GET
    fun fetchXTF(
        @Url fileUrl: String,
    ): Single<ResponseBody>
}

