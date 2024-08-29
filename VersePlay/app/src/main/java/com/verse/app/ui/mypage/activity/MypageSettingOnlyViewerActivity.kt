package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityMypageSettingOnlyViewerBinding
import com.verse.app.extension.startAct
import com.verse.app.ui.intro.activity.SetCountryActivity
import com.verse.app.ui.intro.activity.SetLanguageActivity
import com.verse.app.ui.mypage.viewmodel.MypageSettingOnlyViewerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MypageSettingOnlyViewerActivity :
    BaseActivity<ActivityMypageSettingOnlyViewerBinding, MypageSettingOnlyViewerViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_only_viewer
    override val viewModel: MypageSettingOnlyViewerViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    var title: String = ""

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback) // 콜백 인스턴스

        title = intent.getStringExtra("title").toString()
        if (TextUtils.isEmpty(title)) title = ""

        with(viewModel) {

            // 레이아웃 visible 세팅
            if (title.equals(getString(R.string.mypage_setting_security))) showLayout(1)
            if (title.equals(getString(R.string.setting_service_lan_country))) showLayout(2)
            
            // 상단 마진
            binding.apply {
                // 헤더 타이틀
                tvTitle.text = title
            }

            backpress.observe(this@MypageSettingOnlyViewerActivity) {
                finish()
            }

            setting_country.observe(this@MypageSettingOnlyViewerActivity) {
                startAct<SetCountryActivity>() {
                    putExtra("isSetting", true)
                }
            }

            setting_lan.observe(this@MypageSettingOnlyViewerActivity) {
                startAct<SetLanguageActivity>() {
                    putExtra("isSetting", true)
                }
            }
        }
    }

}