package com.verse.app.ui.intro.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityLanguageSetBinding
import com.verse.app.extension.exitApp
import com.verse.app.extension.startAct
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.intro.viewmodel.SetLanguageViewModel
import com.verse.app.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : Setting Lan Activity Class
 *
 * Created by esna on 2023-03-16
 */

@AndroidEntryPoint
class SetLanguageActivity : BaseActivity<ActivityLanguageSetBinding, SetLanguageViewModel>() {
    override val layoutId = R.layout.activity_language_set
    override val viewModel: SetLanguageViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)
        viewModel.isSetting = intent.getBooleanExtra("isSetting", false)

        initSaveButton()

        with(viewModel) {
            originLanguage = accountPref.getPreferenceLocaleLanguage()

            getLangList()

            // 닫기
            startFinish.observe(this@SetLanguageActivity) {
                if (isSetting) {
                    backpress.call()
                } else {
                    CommonDialog(this@SetLanguageActivity)
                        .setContents(getString(R.string.str_common_finish_app_notice))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.str_finish))
                        .setNegativeButton(getString(R.string.str_cancel))
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    exitApp()
                                }
                            }
                        }).show()
                }
            }

            // 뒤로가기
            backpress.observe(this@SetLanguageActivity) {
                finish()
            }

            refreshList.observe(this@SetLanguageActivity) {
                binding.rvLanguage.adapter!!.notifyDataSetChanged()
            }

            //앱 최초 실행시 튜토리얼로 이동, 마이페이지 -> 설정 -> 언어 진입 시 pass
            selectedItem.observe(this@SetLanguageActivity) {
                if (!isSetting) {
                    accountPref.setPreferenceLocaleLanguage(it)
                    startAct<TutorialActivity>() {
                        putExtra("isUserGuide", 0)
                    }
                    finish()
                }
            }

            changeLanguage.observe(this@SetLanguageActivity) {
                if (originLanguage == selectedItem.value) {
                    finish()
                } else {
                    moveToMain()
                }
            }

            // "?" 클릭 시 관련 팝업 호출
            startOneButtonPopup.observe(this@SetLanguageActivity) {
                CommonDialog(this@SetLanguageActivity)
                    .setContents(getString(R.string.str_intro_help_language))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setIcon(AppData.POPUP_HELP)
                    .show()
            }
        }
    }

    fun initSaveButton() {
        if (!viewModel.isSetting) {
            binding.saveSettingData.visibility = View.GONE
        } else {
            binding.saveSettingData.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (viewModel.isSetting) super.onBackPressed()
        return
    }

    override fun finish() {
        super.finish()
        if (!viewModel.isSetting) {
            overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left)
        }
    }

    /**
     * 언어 설정 저장 후 메인 이동
     */
    private fun moveToMain() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {

            val intent = Intent(this@SetLanguageActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 500)
    }
}
