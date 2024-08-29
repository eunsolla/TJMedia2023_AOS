package com.verse.app.ui.report.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentReportSubBinding
import com.verse.app.extension.popBackStackParentFragment
import com.verse.app.ui.report.ReportViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 신고하기 서브
 *
 * Created by jhlee on 2023-04-21
 */
@AndroidEntryPoint
class ReportSubFragment : BaseFragment<FragmentReportSubBinding, ReportViewModel>() {

    override val layoutId: Int = R.layout.fragment_report_sub
    override val viewModel: ReportViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                popBackStackParentFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            binding.vHeader.setHeaderTitle(subTitle.value)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    override fun onDestroy() {
        DLogger.d("### ReportSubFragment onDestroy")
        super.onDestroy()
    }
}