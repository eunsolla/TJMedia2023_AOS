package com.verse.app.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ViewWebviewBinding
import com.verse.app.extension.getActivity
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WebViewActivity : BaseActivity<ViewWebviewBinding, WebViewViewModel>() {
    companion object {
        const val WEB_VIEW_INTENT_DATA_TYPE: String = "DataType"
        const val WEB_VIEW_INTENT_VALUE_URL: String = "URL"
        const val WEB_VIEW_INTENT_VALUE_CONTENTS: String = "CONTENTS"
        const val WEB_VIEW_INTENT_TITLE: String = "WEB_TITLE"
        const val WEB_VIEW_INTENT_URL: String = "WEB_URL"
        const val WEB_VIEW_INTENT_DATA: String = "WEB_DATA"
    }

    override val layoutId = R.layout.view_webview
    override val viewModel: WebViewViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private var title: String? = null
    private var dataType: String? = null
    private var url: String? = null
    private var data: String? = null

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(binding.wvWebView.canGoBack()){
                binding.wvWebView.goBack()
            }else{
                finish()
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        val i = intent
        try {
            dataType = i.getStringExtra(WEB_VIEW_INTENT_DATA_TYPE)
            title = i.getStringExtra(WEB_VIEW_INTENT_TITLE)
            url = i.getStringExtra(WEB_VIEW_INTENT_URL)
            data = i.getStringExtra(WEB_VIEW_INTENT_DATA)
        } catch (e: Exception) {
            DLogger.d("Fail WebViewActivity Load Data")
        }

        with(viewModel) {
            if (dataType.equals(WEB_VIEW_INTENT_VALUE_URL)) {
                DLogger.d("dataType WEB_VIEW_INTENT_VALUE_URL :::  $dataType")
                if (title != null) {
                    binding.tvTitle.text = title
                }

                if (url != null) {
                    binding.wvWebView.loadUrl(url!!)
                }

            } else {
                DLogger.d("dataType else :::  $dataType")
                if (title != null) {
                    binding.tvTitle.text = title
                }

                if (data != null) {
                    val html = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no'/>" +
                            "<style>\n" +
                            "body { padding-left: 10px;padding-right: 10px; }\n" +
                            "img { max-width: 100%; }\n" +
                            "figure {margin-left: 0;margin-right: 0;}\n" +
                            "figure.table {margin: 0;}\n" +
                            "      table {\n" +
                            "        width: 100%; border-collapse: collapse;\n" +
                            "      }\n" +
                            "      table, th, td {\n" +
                            "        border: 1px solid #000;\n" +
                            "      }" +
                            "</style>\n" +
                            "</head>" +
                            "<body>" +
                            data + "</body>" + "</html>"
                    binding.wvWebView.loadDataWithBaseURL(null, html, "text/html; charset=utf-8", "utf-8", null)
                    DLogger.d("data:: ${data}")
                }
            }

            binding.apply {
                backpress.observe(this@WebViewActivity) {
                    finish()
                }
            }
        }
    }

}