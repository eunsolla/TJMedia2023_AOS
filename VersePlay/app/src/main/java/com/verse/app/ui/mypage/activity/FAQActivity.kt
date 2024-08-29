package com.verse.app.ui.mypage.activity

import FAQFragment
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityMypageSettingFaqBinding
import com.verse.app.extension.*
import com.verse.app.ui.mypage.fragment.FAQDetailFragment
import com.verse.app.ui.mypage.fragment.FAQLastFragment
import com.verse.app.ui.mypage.fragment.FAQSubFragment
import com.verse.app.ui.mypage.viewmodel.FAQViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FAQActivity :
    BaseActivity<ActivityMypageSettingFaqBinding, FAQViewModel>() {

    override val layoutId = R.layout.activity_mypage_setting_faq
    override val viewModel: FAQViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    var title: String = ""

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback) // 콜백 인스턴스

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        title = intent.getStringExtra("title").toString()
        if (TextUtils.isEmpty(title)) title = ""

        with(viewModel) {

            requestFAQList()

            //서브 카테고리
            subFAQList.observe(this@FAQActivity) {
                if (it != null && it.size > 0) {
                    replaceFragment(
                        enterAni = R.anim.in_right_to_left_short,
                        exitAni = R.anim.out_right_to_left_short,
                        popEnterAni = R.anim.out_left_to_right_short,
                        popExitAni = R.anim.in_left_to_right_short,
                        binding.flSubFaq.id, initFragment<FAQSubFragment>()
                    )
                }
            }

            lastFAQList.observe(this@FAQActivity) {
                if (it != null && it.size > 0) {
                    replaceFragment(
                        enterAni = R.anim.in_right_to_left_short,
                        exitAni = R.anim.out_right_to_left_short,
                        popEnterAni = R.anim.out_left_to_right_short,
                        popExitAni = R.anim.in_left_to_right_short,
                        binding.flSubFaq.id, initFragment<FAQLastFragment>()
                    )
                }
            }


            // 디테일 뷰
            subFAQContents.observe(this@FAQActivity) {
                if (it != null) {
                    replaceFragment(
                        enterAni = R.anim.in_right_to_left_short,
                        exitAni = R.anim.out_right_to_left_short,
                        popEnterAni = R.anim.out_left_to_right_short,
                        popExitAni = R.anim.in_left_to_right_short,
                        binding.flSubFaq.id, initFragment<FAQDetailFragment>()
                    )
                }
            }

            //default
            replaceFragment(
                containerId = binding.flSubFaq.id,
                initFragment<FAQFragment>()
            )
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
    }
}