package com.verse.app.ui.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.verse.app.BuildConfig
import com.verse.app.R
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.extension.exitApp
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.NetworkConnectionProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Description : 웹뷰
 *
 * Created by esna on 2023-04-20
 */
@AndroidEntryPoint
class CustomWebView @JvmOverloads constructor(
    private val ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(ctx, attrs, defStyleAttr), LifecycleOwner, LifecycleObserver, NestedScrollingChild {
    interface Listener {
        fun onScroll(scrollY: Int)
    }

    @Inject
    lateinit var deviceProvider: DeviceProvider

    @Inject
    lateinit var loginManager: LoginManager

    @Inject
    lateinit var networkConnectionProvider: NetworkConnectionProvider

    private val activity: Activity by lazy { ctx as Activity }
    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0
    private val childHelper: NestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }

    private val uiHandler = Handler(Looper.getMainLooper())
    var viewModel: BaseViewModel? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    var stringBuilder : StringBuilder? = null
    var listener: Listener? = null

    private var currProgress = 0
    private var prevScroll = 0
    private val jsonFormat: Json by lazy {
        Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onStateEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        DLogger.d("WebView onStateEvent $event")
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }

            Lifecycle.Event.ON_RESUME -> {
            }

            else -> {
            }
        }
    }

    override fun getLifecycle() = lifecycleRegistry

    init {
        setWebSetting()
        setCookie()
        isNestedScrollingEnabled = true
        webChromeClient = WebChromeClient()
        webViewClient = WebViewClient()
        stringBuilder = StringBuilder()
        addJavascriptInterface(JavaInterface(), "auctionBridge")

        if (ctx is FragmentActivity) {
            ctx.lifecycle.addObserver(this)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setWebSetting() {
        this.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setNetworkAvailable(true)

        setBackgroundColor(0x00000000)

        settings.apply {
            javaScriptEnabled = true // 자바 스크립트 허용
            javaScriptCanOpenWindowsAutomatically = false // 팝업창 오픈 기능 팝업창을 띄울 경우가 있는데, 해당 속성을 추가해야 window.open() 이 제대로 작동
            useWideViewPort = true // 웹뷰 화면 맞춤
            loadWithOverviewMode = true // 메타태그 허용 여부
            loadsImagesAutomatically = true // 앱 이미지 리소스 자동 로드
            domStorageEnabled = true // 로컬 스토리지 사용 여부
            setSupportMultipleWindows(false) // 멀티 윈도우 허용여부
            setGeolocationEnabled(true) // 위치 허용
            textZoom = 100
            builtInZoomControls = false //줌 컨트롤 사용 여부 설정
            defaultTextEncodingName = "UTF-8" //인코딩 설정
            cacheMode = WebSettings.LOAD_NO_CACHE // 캐시 거부
            domStorageEnabled = true
            /*
            (1) LOAD_CACHE_ELSE_NETWORK 기간이 만료돼 캐시를 사용할 수 없을 경우 네트워크를 사용합니다.
            (2) LOAD_CACHE_ONLY 네트워크를 사용하지 않고 캐시를 불러옵니다.
            (3) LOAD_DEFAULT 기본적인 모드로 캐시를 사용하고 만료된 경우 네트워크를 사용해 로드합니다.
            (4) LOAD_NORMAL 기본적인 모드로 캐시를 사용합니다.
            (5) LOAD_NO_CACHE 캐시모드를 사용하지 않고 네트워크를 통해서만 호출합니다.
            */
            userAgentString = "app" // 웹에서 해당 속성을 통해 앱에서 띄운 웹뷰로 인지 할 수 있도록
            domStorageEnabled = true // 로컬저장소 허용 여부
//            defaultFontSize = 40
            if (BuildConfig.DEBUG) {
                setWebContentsDebuggingEnabled(true)
            }
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //https, http 호환 여부(https에서 http컨텐츠도 보여질수 있도록 함)
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }
        requestFocus()
        isFocusable = true
        isFocusableInTouchMode = true
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_UP -> {
                    if (v.hasFocus()) {
                        v.requestFocus()
                    }
                }
            }
            return@setOnTouchListener false
        }

    }

    /**
     * Setting Cookies
     */
    private fun setCookie() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@CustomWebView, true)
        }
    }

    override fun loadUrl(url: String) {
        super.loadUrl(url)
        DLogger.d("Normal LoadUrl $url")
    }

    override fun canGoBack(): Boolean {
        val result = super.canGoBack()
        DLogger.d("canGoBack Current Url $url, $result")
        return result
    }

    override fun goBack() {
        super.goBack()
    }

    /**
     * 자바스크립트 함수 호출
     */
    fun callJavaScript(script: String?) {
        if (script.isNullOrEmpty()) return

        DLogger.d("CallJavaScript $script")
        uiHandler.post {
            loadUrl(script)
        }
    }

    /**
     * 자바 스크립트 콜백 함수 호출
     */
    fun fetchJavaScript(script: String?, callback: (String) -> Unit) {
        if (script.isNullOrEmpty()) return

        DLogger.d("fetchJavaScript $script")
        uiHandler.post { evaluateJavascript(script, callback) }
    }

    inner class JavaInterface {
        @JavascriptInterface
        fun getAppVersionInfo(): String {
            var currVersion = deviceProvider.getVersionName()
            DLogger.d("Return Application Version : $currVersion")

            return currVersion

        }

//        /**
//         * 사용자 정보 Json 형식
//         */
//        @JavascriptInterface
//        fun setUserInfo(userJson: String?) {
//            if (userJson.isNullOrEmpty()) return
//            DLogger.d("JavaInterface Function setUserInfo $userJson")
//
//            runCatching {
//                jsonFormat.decodeFromString<UserInfo>(userJson).run {
//                    DLogger.d("UserInfo $this")
//                    loginManager.setUserToken(userToken)
//                }
//            }.onFailure {
//                DLogger.e("JsonParser Error $it")
//            }
//        }


//        @JavascriptInterface
//        fun moveWebPage(url: String?) {
//            if (url.isNullOrEmpty()) return
//
//            DLogger.d("JavaInterface Function moveWebPage $url")
//
//            uiHandler.post {
//                activity.startAct<CommonWebActivity> {
////                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
////                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    putExtra(ExtraCode.WEB_URL, url)
//                }
//            }
//        }
    }


    inner class WebChromeClientClass : WebChromeClient() {
        @SuppressLint("SetJavaScriptEnabled")
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            if (resultMsg == null) return false

            val popupWebView = WebView(activity).apply {
                settings.javaScriptEnabled = true
            }

            val dialog = Dialog(activity, R.style.FullScreenDialog).apply {
                setContentView(popupWebView)
                window?.attributes?.also {
                    it.width = ViewGroup.LayoutParams.MATCH_PARENT
                    it.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                setOnDismissListener {
                    popupWebView.destroy()
                }
            }
            popupWebView.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView?) {
                    dialog.dismiss()
                    window?.destroy()
                }
            }
            popupWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }

            dialog.show()
            (resultMsg.obj as WebViewTransport).webView = popupWebView
            resultMsg.sendToTarget()
            return true
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            DLogger.d("onProgressChanged $newProgress")
            currProgress = newProgress
        }
    }

    inner class WebViewClientClass : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            DLogger.d("onPageStarted $url")
            if (networkConnectionProvider.isNetworkAvailable()) {
                viewModel?.onLoadingShow()
            } else {
                view?.stopLoading()
                CommonDialog(context)
                    .setIcon(AppData.POPUP_WARNING)
                    .setContents(R.string.str_network_error_msg)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            activity.exitApp()
                        }
                    })
                    .show()
            }

        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            DLogger.d("doUpdateVisitedHistory isReload $isReload $url")
            if (url.equals(Config.BASE_API_URL) || url.equals(Config.BASE_API_URL + "/home")) {
                clearHistory()
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()

            if (url.startsWith("tel:")) {
                val mIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(mIntent)

                return true
            }
            DLogger.d("shouldOverrideUrlLoading $url")

            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            DLogger.d("onPageFinished $url")

            viewModel?.onLoadingDismiss()
            CookieManager.getInstance().flush()
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        var returnValue = false
        val event = MotionEvent.obtain(ev)
        val action = ev?.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY.toFloat())
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY: Int = lastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    lastY = eventY - scrollOffset[1]
                    event.offsetLocation(0f, -scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                }
                returnValue = super.onTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, scrollOffset[1], 0, deltaY, scrollOffset)) {
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                    lastY -= scrollOffset[1]
                }
            }

            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(event)
                lastY = eventY
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                returnValue = super.onTouchEvent(event)
                // end NestedScroll
                stopNestedScroll()
            }
        }
        return returnValue
    }

    // Nested Scroll implements
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        swipeRefreshLayout?.isEnabled = prevScroll == 0 && scrollY == 0
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        prevScroll = t
        listener?.onScroll(t)
    }
}