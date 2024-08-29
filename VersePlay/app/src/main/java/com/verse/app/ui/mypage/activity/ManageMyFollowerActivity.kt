package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityMypageSettingManageMyFollowerBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.viewPagerNotifyPayload
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.viewmodel.ManageMyFollowerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageMyFollowerActivity :
    BaseActivity<ActivityMypageSettingManageMyFollowerBinding, ManageMyFollowerViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_manage_my_follower
    override val viewModel: ManageMyFollowerViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        with(viewModel) {
            isLoadingShow.value = true

            requestBlockData()

            backpress.observe(this@ManageMyFollowerActivity) {
                finish()
            }

            refresh.observe(this@ManageMyFollowerActivity) {
                binding.rvBlock.viewPagerNotifyPayload(it.first, null)
                startOneButtonPopup.call()
            }

            startOneButtonPopup.observe(this@ManageMyFollowerActivity) {
                CommonDialog(this@ManageMyFollowerActivity)
                    .setContents(getString(R.string.mypage_setting_unblocking_complete))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                isLoadingShow.value = false
                                requestBlockData()
                            }
                        }
                    }).show()
            }
        }
    }


}