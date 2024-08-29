package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.NaviType
import com.verse.app.databinding.ActivityMypageSessionBinding
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.viewmodel.SessionViewModel
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SessionActivity : BaseActivity<ActivityMypageSessionBinding, SessionViewModel>() {
    override val layoutId = R.layout.activity_mypage_session
    override val viewModel: SessionViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        with(viewModel) {
            startLoadEtcPage.observe(this@SessionActivity) {
                binding.tvTitle.text = it.bctgMngNm
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
                        it.termsContent + "</body>" + "</html>"
                binding.sessionWebview.loadDataWithBaseURL(null, html, "text/html; charset=utf-8", "utf-8", null)

            }

            backpress.observe(this@SessionActivity) {
                finish()
            }

            showTwoButtonDialogPopup.observe(this@SessionActivity) {
                CommonDialog(this@SessionActivity)
                    .setContents(getString(R.string.str_is_withdrawal))
                    .setIcon(AppData.POPUP_HELP)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setNegativeButton(getString(R.string.str_cancel))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                setSession()
                            }
                        }
                    })
                    .show()
            }

            showOneButtonDialogPopup.observe(this@SessionActivity) {
                CommonDialog(this@SessionActivity)
                    .setContents(getString(R.string.str_withdrawal_complete))
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500)
                                    RxBus.publish(RxBusEvent.NaviEvent(NaviType.MAIN))
                                    viewModel.loginManager.logout(baseContext,true)
                                }
                            }
                        }
                    })
                    .show()
            }
        }

        viewModel.requestSession()

    }

}

