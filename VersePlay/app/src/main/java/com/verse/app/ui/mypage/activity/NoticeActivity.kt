package com.verse.app.ui.mypage.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityNoticeBinding
import com.verse.app.extension.initFragment
import com.verse.app.extension.replaceFragment
import com.verse.app.ui.mypage.fragment.NoticeFragment
import com.verse.app.ui.mypage.fragment.NoticeSubFragment
import com.verse.app.ui.mypage.viewmodel.NoticeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeActivity : BaseActivity<ActivityNoticeBinding, NoticeViewModel>() {

    override val layoutId = R.layout.activity_notice
    override val viewModel: NoticeViewModel by viewModels()
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

            //서브 카테고리
            subTitle.observe(this@NoticeActivity) {
                if (it != null) {
                    replaceFragment(
                        enterAni = R.anim.in_right_to_left_short,
                        exitAni = R.anim.out_right_to_left_short,
                        popEnterAni = R.anim.out_left_to_right_short,
                        popExitAni = R.anim.in_left_to_right_short,
                        binding.flSubNotice.id, initFragment<NoticeSubFragment>()
                    )
                }
            }

            //default
            replaceFragment(
                containerId = binding.flSubNotice.id,
                initFragment<NoticeFragment>()
            )

            viewModel.requestNotiList()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
    }
}