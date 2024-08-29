package com.verse.app.ui.intro.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityCountrySetBinding
import com.verse.app.extension.exitApp
import com.verse.app.extension.startAct
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.intro.viewmodel.SetCountryViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : Setting Country Activity Class
 *
 * Created by esna on 2023-03-16
 */
@AndroidEntryPoint
class SetCountryActivity :
    BaseActivity<ActivityCountrySetBinding, SetCountryViewModel>() {
    override val layoutId = R.layout.activity_country_set
    override val viewModel: SetCountryViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)
        viewModel.isSetting = intent.getBooleanExtra("isSetting", false)

        initSaveButton()

        with(viewModel) {
            getCountryList()

            // 닫기
            startFinish.observe(this@SetCountryActivity) {
                if (isSetting) {
                    backpress.call()
                } else {
                    CommonDialog(this@SetCountryActivity)
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

            // "?" 클릭 시 관련 팝업 호출
            startOneButtonPopup.observe(this@SetCountryActivity) {
                CommonDialog(this@SetCountryActivity)
                    .setContents(getString(R.string.str_intro_help_country))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setIcon(AppData.POPUP_HELP)
                    .show()
            }

            refreshList.observe(this@SetCountryActivity) {
                binding.rvNation.adapter!!.notifyDataSetChanged()
            }

            // 뒤로가기
            backpress.observe(this@SetCountryActivity) {
                finish()
            }

            // 저장
            selectedItem.observe(this@SetCountryActivity) {
                if (!isSetting) {
                    accountPref.setPreferenceLocaleCountry(it)
                    startAct<SetLanguageActivity>()
                    finish()
                }
            }
        }
    }

    private fun initSaveButton() {
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
}
