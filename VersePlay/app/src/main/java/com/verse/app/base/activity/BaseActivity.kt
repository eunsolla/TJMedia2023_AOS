package com.verse.app.base.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LoadingDialogState
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentAct
import com.verse.app.extension.startSongEncodeService
import com.verse.app.model.encode.EncodeData
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.LoadingDialog
import com.verse.app.ui.dialog.NetworkErrorDialog
import com.verse.app.ui.dialog.ServerCheckDialog
import com.verse.app.ui.intro.activity.IntroActivity
import com.verse.app.ui.videoupload.VideouploadActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.LocaleUtils
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : BaseActivity Class
 *
 * Created by jhlee on 2023-01-01
 */
abstract class BaseActivity<Binding : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity(),

    LifecycleObserver {

    private var _binding: Binding? = null
    val binding get() = _binding!!

    abstract val layoutId: Int
    abstract val viewModel: VM
    abstract val bindingVariable: Int
    private val loadingDialog by lazy { LoadingDialog(this) }
    private var toast: Toast? = null

    private var encodeDisposable: Disposable? = null
    private lateinit var preference: AccountPref

    private var isShowEncodeFailPopup = false
    private var isShowReUploadFailPopup = false
    private var isPopupFlag = false

    private lateinit var reUploadPopup: CommonDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<Binding>(this, layoutId).apply {
            lifecycleOwner = this@BaseActivity
            setVariable(bindingVariable, viewModel)
            _binding = this
        }

        DLogger.d("PUSH_MSG_LINK_TYPE BaseActivity onCreate : ${intent.getStringExtra(ExtraCode.PUSH_MSG_LINK_TYPE)}")

        with(viewModel) {

            startLoadingDialog.observe(this@BaseActivity) { state ->
                performLoadingDialog(state)
            }

            showNetWorkErrorDialog.observe(this@BaseActivity) {
                NetworkErrorDialog(this@BaseActivity, it).show()
            }

            showServerCheckDialog.observe(this@BaseActivity) {
                ServerCheckDialog(this@BaseActivity).show()
            }

            showToastIntMsg.observe(this@BaseActivity) {
                showToast(it)
            }

            showToastStringMsg.observe(this@BaseActivity) {
                showToast(it)
            }
        }

        if (viewModel is ActivityViewModel) {
            (viewModel as ActivityViewModel).startActivityPage.observe(this) {
                startActivityAndAnimation(it)
            }
        }

        //화면 꺼짐 방지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearDisposable()
        loadingDialog.setState(LoadingDialogState.DISMISS)
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        DLogger.d("BaseACT onResume")

        if (isShowReUploadFailPopup) {
            DLogger.d("BaseACT onResume showReUploadPopup")
            showReUploadPopup(viewModel.reEncodeData.value!!)
        }

        if (isShowEncodeFailPopup) {
            DLogger.d("BaseACT onResume showEncodeFailPopup")
            showEncodeFailPopup()
        }

        /**
         * 인코딩 실패했을 때 재업로드 팝업 노출하도록 하는 리스너 호출
         */
        encodeDisposable = RxBus.listen(RxBusEvent.EncodeFailEvent::class.java)
            .applyApiScheduler()
            .doOnNext {
                DLogger.d("@@ 관련 플래그 : AppData.IS_REUPLOAD_ING ${AppData.IS_REUPLOAD_ING} / AppData.IS_SING_ING ${AppData.IS_SING_ING}")

                // 팝업 노출되어 있는 경우 return
//                if (AppData.IS_REUPLOAD_ING) return@subscribe

                // 부르기 화면일경우 return
                if (AppData.IS_SING_ING) return@doOnNext

                DLogger.d("@@ 인코딩 받아온 데이터 ${it.encodeData}")
                DLogger.d("@@ it.isFile 파일 존재 여부${it.isFile}")

                viewModel._reEncodeData.value = it.encodeData

//                if (it.isFile) {
//                    // 파일 있는경우 -> 재업로드 팝업
//                    DLogger.d("showReUploadPopup")
//                    if (!AppData.IS_REUPLOAD_ING) {
//                        it.encodeData?.let { encodeData ->
//                            showReUploadPopup(encodeData) }
//                    }
////                        it.encodeData?.let { encodeData -> showReUploadPopup(encodeData) }
//                } else {
//                    // 파일 없는경우 -> 인코딩 실패 원버튼 팝업
//                    DLogger.d("showEncodeFailPopup")
//                    if (!AppData.IS_REUPLOAD_ING){
//                        showEncodeFailPopup()
//                    }
////                        showEncodeFailPopup()
//                }

                if (AppData.isOnApp) {

                    if (it.isFile) {
                        // 파일 있는경우 -> 재업로드 팝업
                        DLogger.d("showReUploadPopup")
                        if (!AppData.IS_REUPLOAD_ING) {
                            it.encodeData?.let { encodeData -> showReUploadPopup(encodeData) }
                        }
//                        it.encodeData?.let { encodeData -> showReUploadPopup(encodeData) }
                    } else {
                        // 파일 없는경우 -> 인코딩 실패 원버튼 팝업
                        DLogger.d("showEncodeFailPopup")
                        if (!AppData.IS_REUPLOAD_ING) {
                            showEncodeFailPopup()
                        }
//                        showEncodeFailPopup()
                    }

                } else {

                    // 앱이 백그라운드로 내려갔을 때 인코딩 실패하면 다시 포그라운드로 올라왔을 때 팝업 노출하기 위해
                    // 플래그 처리함
                    if (it.isFile) {
                        DLogger.d("isShowReUploadFailPopup true")
                        // 파일 있는경우 -> 재업로드 팝업
                        isShowReUploadFailPopup = true
                    } else {
                        DLogger.d("isShowEncodeFailPopup true")
                        // 파일 없는경우 -> 인코딩 실패 원버튼 팝업
                        isShowEncodeFailPopup = true
                    }
                }
            }
            .subscribe()
    }

    override fun finish() {
        super.finish()
        if (this !is IntroActivity) {
            overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
        }
    }

    protected fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.run {
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    /**
     * Status Bar 영역까지 넓히는 함수.
     */
    @Suppress("DEPRECATION")
    protected fun setFitsWindows() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.run {
//                hide(WindowInsets.Type.navigationBars())
                // Statusbar 영역까지 넓힘.
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    /**
     * Stats Bar 투명 처리
     */
    protected fun setStatusBarTransparent() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    /**
     * 로딩 화면 비노출
     */
    protected fun dismissLoadingDialog() {
        loadingDialog.setVisible(false)
    }

    /**
     * 로딩 프로그래스 바 노출 유무
     */
    @MainThread
    protected fun performLoadingDialog(state: LoadingDialogState) {
        // Show Progress Dialog
        loadingDialog.setState(state)
    }

    /**
     *  Toast Msg
     */
    fun showToast(@StringRes strId: Int) {
        showToast(getString(strId))
    }

    fun showToast(msg: String) {
        runOnUiThread {
            if (toast != null) {
                toast?.cancel()
                toast = null
            }
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    private fun showReUploadPopup(encodeData: EncodeData) {
        DLogger.d("showReUploadPopup 팝업노출")

        // 팝업 노출 플래그 true (이벤트 중복 호출 시 팝업 여러개 뜨는 것을 방지하기 위함)
        AppData.IS_REUPLOAD_ING = true

        val dataList = mutableListOf<EncodeData>()
        dataList.add(encodeData)

        CommonDialog(this@BaseActivity)
            .setTitle(getString(R.string.mypage_setting_push_upload_fail))
            .setContents(getString(R.string.video_upload_fail))
            .setPositiveButton(getString(R.string.str_cancel))
            .setNegativeButton(getString(R.string.video_upload_fail_reload))
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    AppData.IS_REUPLOAD_ING = false
                    isShowReUploadFailPopup = false
                    DLogger.d("@@ 관련 플래그 변경 : AppData.IS_REUPLOAD_ING ${AppData.IS_REUPLOAD_ING}")

                    // 재업로드 버튼 클릭 시
                    if (which == CommonDialog.NEGATIVE) {
                        if (getFragmentAct() is VideouploadActivity) {
                            (getFragmentAct() as VideouploadActivity).viewModel.showUploadProgress(true)
                        }
                        startSongEncodeService() {
                            this.putParcelableArrayListExtra(ExtraCode.SING_ENCODE_ITEM, ArrayList(dataList))
                        }
                    }
                }
            })
            .show()
    }

    private fun showEncodeFailPopup() {
        DLogger.d("showEncodeFailPopup 팝업노출")
        // 팝업 노출 플래그 true (이벤트 중복 호출 시 팝업 여러개 뜨는 것을 방지하기 위함)
        AppData.IS_REUPLOAD_ING = true

        CommonDialog(this@BaseActivity)
            .setContents(getString(R.string.encode_fail))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.str_confirm))
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    // 확인 버튼 클릭 시
                    if (which == CommonDialog.POSITIVE) {
                        isShowEncodeFailPopup = false
                        AppData.IS_REUPLOAD_ING = false
                        DLogger.d("@@ 관련 플래그 변경 : AppData.IS_REUPLOAD_ING ${AppData.IS_REUPLOAD_ING}")

                    }
                }
            })
            .show()
    }

    /**
     * Start Activity And Animation
     * @param result 페이지 이동할 데이터
     * @see ActivityResult
     */
    private fun startActivityAndAnimation(result: ActivityResult) {
        startActivity(result.getIntent(this))
        if (result.enterAni != -1 && result.exitAni != -1) {
            overridePendingTransition(result.enterAni, result.exitAni)
        } else {
            overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleUtils.wrap(newBase!!))
    }

    override fun onStop() {
        super.onStop()

        DLogger.d("AppData.isOnApp : ${AppData.isOnApp}")

        if (encodeDisposable != null) {
            encodeDisposable?.dispose()
            encodeDisposable = null
        }
//        // 포그라운드일때만 Disposable 구독 해제
//        if (AppData.isOnApp) {
//        }
    }
}