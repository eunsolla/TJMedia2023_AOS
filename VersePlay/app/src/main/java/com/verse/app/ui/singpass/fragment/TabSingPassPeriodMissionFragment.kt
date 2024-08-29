package com.verse.app.ui.singpass.fragment

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.viewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.TabPageType
import com.verse.app.databinding.FragmentTabSingPassPeriodMissionBinding
import com.verse.app.extension.customGetSerializable
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.singpass.viewmodel.SingPassDashBoardPeriodViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : TabFeedFragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class TabSingPassPeriodMissionFragment :
    BaseFragment<FragmentTabSingPassPeriodMissionBinding, SingPassDashBoardPeriodViewModel>() {

    override val layoutId: Int = R.layout.fragment_tab_sing_pass_period_mission
    override val viewModel: SingPassDashBoardPeriodViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    var userMemCd: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DLogger.d("TabSingPassPeriodMissionFragment onViewCreated")

        requestSingPassMissionInfo()

        binding.apply {
        }

        with(viewModel) {
            requestSkipMission.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(
                        HtmlCompat.fromHtml(
                            java.lang.String.format(
                                getString(R.string.season_sing_pass_use_mission_skip),
                                it.miMngNm
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                    )
                    .setCancelable(true)
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setNegativeButton(getString(R.string.str_cancel))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                requestSkipMission(it.svcSeaMngCd, it.svcMiMngCd, it.svcMiRegTpCd, it.msTpCd)
                            }
                        }
                    }).show()
            }

            startOneButtonPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }

        }
    }

    private fun requestSingPassMissionInfo() {
        var tabPageType: TabPageType? = null

        if (viewModel.loginManager.isLogin()) {
            arguments?.let {
                tabPageType = it.customGetSerializable<TabPageType>(ExtraCode.TAB_TYPE)
            }

            tabPageType?.let {
                viewModel.requestSingPassMissionInfo(tabPageType!!)
            }
        } else {
            CommonDialog(requireActivity())
                .setContents(getString(R.string.network_status_rs004))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (which == CommonDialog.POSITIVE) {
                            requireActivity().finish()
                        }
                    }
                }).show()
        }
    }

    override fun onDestroy() {
        DLogger.d("### TabSingPassPeriodMissionFragment onDestroy")
        super.onDestroy()
    }
}