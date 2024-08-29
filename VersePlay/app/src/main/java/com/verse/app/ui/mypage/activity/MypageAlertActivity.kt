package com.verse.app.ui.mypage.activity

import MyPageAlertFragment
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.databinding.ActivityMypageAlartBinding
import com.verse.app.extension.initFragment
import com.verse.app.extension.replaceFragment
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.viewmodel.MypageAlartViewModel
import com.verse.app.utility.moveToLinkPage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MypageAlertActivity : BaseActivity<ActivityMypageAlartBinding, MypageAlartViewModel>() {
    override val layoutId: Int = R.layout.activity_mypage_alart
    override val viewModel: MypageAlartViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        with(viewModel) {
            requestMyAlrimList()

            // 뒤로가기
            backpress.observe(this@MypageAlertActivity) {
                finish()
            }

            moveToPage.observe(this@MypageAlertActivity) {
                // 레드마인 #2461
                // linkCd가 LD010이고, 하단 세 필드 조건 추가
                if (it.linkCd == LinkMenuTypeCode.LINK_FEED_CONTENTS.code) {
                    if (it.fgDelYn == AppData.Y_VALUE || it.fgExposeYn == AppData.N_VALUE || it.songDelYn == AppData.Y_VALUE){
                        showPopup()
                    } else {
                        moveToLinkPage(it.linkCd, it.linkInfo)
                    }
                } else {
                    moveToLinkPage(it.linkCd, it.linkInfo)
                    if(it.linkCd != LinkMenuTypeCode.LINK_MESSAGE_ROOM.code && it.linkCd != LinkMenuTypeCode.LINK_USER_MYPAGE.code) {
                        finish()
                    }
                }
            }

            alrimData.observe(this@MypageAlertActivity) {
                //start fragment
                startFragment(binding.flRoot.id, initFragment<MyPageAlertFragment>())
            }

        }

    }

    /**
     * default
     */
    private fun startFragment(flId: Int, fragment: Fragment) {
        replaceFragment(flId, fragment)
    }

    private fun showPopup() {
        CommonDialog(this@MypageAlertActivity)
            .setContents(getString(R.string.str_deleted_detail_feed))
            .setCancelable(true)
            .setIcon(AppData.POPUP_WARNING)
            .setPositiveButton(getString(R.string.str_confirm))
            .show()
    }

}