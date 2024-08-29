package com.verse.app.ui.singpass.acivity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.bumptech.glide.Glide
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ActivitySingPassUserInfoBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.startAct
import com.verse.app.gallery.ui.GalleryImageDetailBottomSheetDialog
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.singpass.viewmodel.SingPassUserInfoViewModel
import com.verse.app.ui.webview.WebViewActivity
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingPassUserInfoActivity :
    BaseActivity<ActivitySingPassUserInfoBinding, SingPassUserInfoViewModel>() {
    override val layoutId = R.layout.activity_sing_pass_user_info
    override val viewModel: SingPassUserInfoViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    var userMemCd: String = ""
    var genreCd: String = ""
    var seasonCd: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setVariable(BR.requestManager, Glide.with(this))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        requestSingPassUserInfo()

        with(viewModel) {
            startFinish.observe(this@SingPassUserInfoActivity) {
                finish()
            }

            moveToWebView.observe(this@SingPassUserInfoActivity) {
                startAct<WebViewActivity> {
                    putExtra(
                        WebViewActivity.WEB_VIEW_INTENT_DATA_TYPE,
                        WebViewActivity.WEB_VIEW_INTENT_VALUE_CONTENTS
                    )
                    putExtra(
                        WebViewActivity.WEB_VIEW_INTENT_TITLE,
                        it.singPassTermsTitle
                    )

                    putExtra(WebViewActivity.WEB_VIEW_INTENT_DATA, it.singPassTermsContents)
                }
            }

            moveToProfileImageDetail.observe(this@SingPassUserInfoActivity) {
                showImageDetailDialog(it)
            }
        }
    }

    fun requestSingPassUserInfo() {
        this.userMemCd = intent.getStringExtra(ExtraCode.SING_PASS_USER_INFO).toString()
        this.genreCd = intent.getStringExtra(ExtraCode.SING_PASS_GENRE_INFO).toString()
        this.seasonCd = intent.getStringExtra(ExtraCode.SING_PASS_SEASON_INFO).toString()
        DLogger.d("userMemCd : ${userMemCd}")
        DLogger.d("genreCd : ${genreCd}")
        DLogger.d("seasonCd : ${seasonCd}")

        if (userMemCd.isNotEmpty() && genreCd.isNotEmpty() && seasonCd.isNotEmpty()) {
            viewModel.requestSingPassUserInfo(userMemCd, seasonCd, genreCd)
        } else {
            CommonDialog(this@SingPassUserInfoActivity)
                .setContents(getString(R.string.network_status_rs004))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (which == CommonDialog.POSITIVE) {
                            finish()
                        }
                    }
                }).show()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    private fun showImageDetailDialog(imagePath: String) {
        GalleryImageDetailBottomSheetDialog()
            .setImageUrl(imagePath)
            .simpleShow(supportFragmentManager)
    }
}