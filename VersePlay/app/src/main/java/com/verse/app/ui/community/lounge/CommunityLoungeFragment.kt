package com.verse.app.ui.community.lounge

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.ReportType
import com.verse.app.databinding.FragmentCommunityLoungeBinding
import com.verse.app.extension.onMain
import com.verse.app.extension.recyclerViewNotifyPayload
import com.verse.app.extension.startAct
import com.verse.app.model.community.CommunityLoungeData
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialogfragment.ReportDialogFragment
import com.verse.app.ui.report.ReportActivity
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/**
 * Description : 커뮤니티 > [라운지]
 *
 * Created by juhongmin on 2023/05/16
 */
@AndroidEntryPoint
class CommunityLoungeFragment :
    BaseFragment<FragmentCommunityLoungeBinding, CommunityLoungeFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_community_lounge
    override val viewModel: CommunityLoungeFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {

            startReportDialogEvent.observe(viewLifecycleOwner) {
                showReportDialog(it)
            }

            startActivityReportEvent.observe(viewLifecycleOwner) {
                requireActivity().startAct<ReportActivity>() {
                    this.putExtra(ExtraCode.REPORT_CODE, Pair(ReportType.LOUNGE, it.repMngCd))
                }
            }

            startCheckPrivateAccount.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }


            //row refresh
            refresh.observe(viewLifecycleOwner) {
                binding.rvLounge.recyclerViewNotifyPayload(it.first, it.second)
            }

            startScrollTop.observe(viewLifecycleOwner) {
                onMain {
                    delay(500)
                    binding.rvLounge.scrollToPosition(0)
                }
            }

            start()
            initStart()
            loungeLikeRefresh()
        }
    }

    private fun showReportDialog(data: CommunityLoungeData) {
        ReportDialogFragment()
            .setData(data)
            .setListener(viewModel)
            .simpleShow(childFragmentManager)
    }

    companion object {
        fun newInstance(): Fragment = CommunityLoungeFragment()
    }

    override fun onDestroy() {
        viewModel.onLoadingDismiss()
        DLogger.d("## onDestroy CommunityLoungeFragment")
        super.onDestroy()
    }

    override fun onDetach() {
        DLogger.d("## onDetach CommunityLoungeFragment")
        viewModel.onLoadingDismiss()
        super.onDetach()
    }

}
