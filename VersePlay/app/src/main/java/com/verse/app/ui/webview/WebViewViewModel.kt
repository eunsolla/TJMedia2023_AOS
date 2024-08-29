package com.verse.app.ui.webview

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel @Inject constructor(
    val deviceProvider: DeviceProvider,
) : ActivityViewModel() {

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

}