package com.verse.app.ui.permissions

import android.os.Bundle
import androidx.activity.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityPermissionsBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.startAct
import com.verse.app.ui.intro.activity.IntroActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionsActivity : BaseActivity<ActivityPermissionsBinding, PermissionsViewModel>() {

    override val layoutId = R.layout.activity_permissions
    override val viewModel: PermissionsViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        with(viewModel) {

            startPermissions.observe(this@PermissionsActivity) {
                startAct<IntroActivity>(
                    enterAni = android.R.anim.fade_in,
                    exitAni = android.R.anim.fade_out,
                )
                finish()
            }
        }
    }

    override fun onBackPressed() {}

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}