package com.verse.app.ui.mypage.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityTermsOfServiceBinding
import com.verse.app.extension.initFragment
import com.verse.app.extension.replaceFragment
import com.verse.app.ui.mypage.fragment.TermsFragment
import com.verse.app.ui.mypage.fragment.TermsSubFragment
import com.verse.app.ui.mypage.viewmodel.TermsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsActivity : BaseActivity<ActivityTermsOfServiceBinding, TermsViewModel>() {

    override val layoutId = R.layout.activity_terms_of_service
    override val viewModel: TermsViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback) // 콜백 인스턴스

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        with(viewModel) {

            viewModel.requestTermList()

            //서브 카테고리
            subTerms.observe(this@TermsActivity) {
                if (it != null) {
                    replaceFragment(
                        enterAni = R.anim.in_right_to_left_short,
                        exitAni = R.anim.out_right_to_left_short,
                        popEnterAni = R.anim.out_left_to_right_short,
                        popExitAni = R.anim.in_left_to_right_short,
                        binding.flSubTerms.id, initFragment<TermsSubFragment>()
                    )
                }
            }

            //default
            replaceFragment(
                containerId = binding.flSubTerms.id,
                initFragment<TermsFragment>()
            )
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
    }
}