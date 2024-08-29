package com.verse.app.tracking.interceptor.model

import okhttp3.MediaType

class Part(
    val type: MediaType? = null,
    val bytes: ByteArray? = null
)
