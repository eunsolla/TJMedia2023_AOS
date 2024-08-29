package com.verse.app.ui.push

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ActivityPushBinding
import com.verse.app.extension.onMain
import com.verse.app.extension.startAct
import com.verse.app.ui.intro.activity.IntroActivity
import com.verse.app.ui.mypage.activity.MypageAlertActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PushRouterActivity : BaseActivity<ActivityPushBinding, PushRouterViewModel>() {
    override val layoutId = R.layout.activity_push
    override val viewModel: PushRouterViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private var title: String? = ""
    private var message: String? = ""
    private var linkCd: String? = ""
    private var linkData: String? = ""
    private var attImagePath: String? = ""
    private var showType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            title = intent.getStringExtra(ExtraCode.PUSH_MSG_TITLE)
            message = intent.getStringExtra(ExtraCode.PUSH_MSG_MESSAGE)
            linkCd = intent.getStringExtra(ExtraCode.PUSH_MSG_LINK_TYPE)
            linkData = intent.getStringExtra(ExtraCode.PUSH_MSG_LINK_DATA)
            attImagePath = intent.getStringExtra(ExtraCode.PUSH_MSG_ATT_IMAGE)
            showType = intent.getStringExtra(ExtraCode.PUSH_MSG_SHOW_TYPE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!AppData.isOnApp) {
            startApp()
        } else {
            openAlarmList()
        }
    }
    /**
     * 앱 실행
     */
    private fun startApp() {
        val i = Intent(this, IntroActivity::class.java)

        i.putExtra(ExtraCode.PUSH_MSG_TITLE, title)
        i.putExtra(ExtraCode.PUSH_MSG_MESSAGE, message)
        i.putExtra(ExtraCode.PUSH_MSG_LINK_TYPE, linkCd)
        i.putExtra(ExtraCode.PUSH_MSG_LINK_DATA, linkData)
        i.putExtra(ExtraCode.PUSH_MSG_ATT_IMAGE, attImagePath)
        i.putExtra(ExtraCode.PUSH_MSG_SHOW_TYPE, showType)

        startActivity(i)
        finish()
    }

    /**
     * 알림 리스트 액티비티로 이동
     */
    private fun openAlarmList() {
        onMain {
            startAct<MypageAlertActivity>(enterAni =R.anim.in_right_to_left, exitAni = R.anim.out_right_to_left ){
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        finish()
    }
    override fun finish() {
        super.finish()
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}