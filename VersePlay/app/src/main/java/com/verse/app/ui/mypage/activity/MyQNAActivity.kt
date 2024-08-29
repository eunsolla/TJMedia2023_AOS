package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityMypageSettingMyQnaBinding
import com.verse.app.ui.mypage.viewmodel.QNAViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyQNAActivity: BaseActivity<ActivityMypageSettingMyQnaBinding, QNAViewModel>(){
    override val layoutId = R.layout.activity_mypage_setting_my_qna
    override val viewModel: QNAViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        with(viewModel) {
            // 나의 1:1문의 보기 카테고리는 추후 개발 예정
//            requestMyQnAData()
            binding.apply {
            }
            // 뒤로가기
            backpress.observe(this@MyQNAActivity) {
                finish()
            }
        }
    }

}