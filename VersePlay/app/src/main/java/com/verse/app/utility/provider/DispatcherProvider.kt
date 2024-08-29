package com.verse.app.utility.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Description : Dispatcher Provider Class
 *
 * Created by jhlee on 2023-01-01
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

@Suppress("DEPRECATION")
class DispatcherProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : DispatcherProvider {

    override val main: CoroutineDispatcher
        get() = Dispatchers.Main
    override val io: CoroutineDispatcher
        get() = Dispatchers.IO
    override val default: CoroutineDispatcher
        get() = Dispatchers.Default
}