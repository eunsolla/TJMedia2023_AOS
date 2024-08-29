package com.verse.app.ui.report

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityReportBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.initFragment
import com.verse.app.extension.replaceFragment
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.report.fragment.ReportFragment
import com.verse.app.ui.report.fragment.ReportSubFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 신고하기  Class
 *
 * Created by jhlee on 2023-04-21
 */
@AndroidEntryPoint
class ReportActivity : BaseActivity<ActivityReportBinding, ReportViewModel>() {

    override val layoutId = R.layout.activity_report
    override val viewModel: ReportViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        with(viewModel) {

            viewModel.requestReportList()

            //서브 카테고리
            reportSubData.observe(this@ReportActivity) {
                if (it != null && it.size > 0) {
                    replaceFragment(
                        enterAni = R.anim.in_right_to_left_short,
                        exitAni = R.anim.out_right_to_left_short,
                        popEnterAni = R.anim.out_left_to_right_short,
                        popExitAni = R.anim.in_left_to_right_short,
                        binding.flSubReport.id, initFragment<ReportSubFragment>()
                    )
                }
            }

            /**
             * 신고하기 결과
             */
            resultString.observe(this@ReportActivity) {
                showToast(it)
                finish()
            }

            startOneButtonPopup.observe(this@ReportActivity) {
                CommonDialog(this@ReportActivity)
                    .setContents(getString(R.string.str_report_success))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                finish()
                            }
                        }
                    }).show()
            }

            //default
            replaceFragment(
                containerId = binding.flSubReport.id,
                initFragment<ReportFragment>()
            )
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
    }
}