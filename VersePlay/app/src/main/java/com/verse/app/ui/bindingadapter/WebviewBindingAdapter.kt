package com.verse.app.ui.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.ui.webview.CustomWebView

object WebViewBindingAdapter {

    @JvmStatic
    @BindingAdapter("loadWebUrl")
    fun setWebViewUrl(
        view: CustomWebView,
        url: String?
    ) {
        if (url.isNullOrEmpty()) return

        view.loadUrl(url)
    }

    @JvmStatic
    @BindingAdapter(value = ["viewModel","swipeLayout"],requireAll = false)
    fun setWebViewViewModel(
        view: CustomWebView,
        viewModel: BaseViewModel?,
        swipeLayout : SwipeRefreshLayout?
    ) {
        view.viewModel = viewModel
        view.swipeRefreshLayout = swipeLayout
    }

    @JvmStatic
    @BindingAdapter("callJavaScript")
    fun setWebViewJavaScript(
        view: CustomWebView,
        scriptFunction: String?
    ) {
        view.callJavaScript(scriptFunction)
    }
}