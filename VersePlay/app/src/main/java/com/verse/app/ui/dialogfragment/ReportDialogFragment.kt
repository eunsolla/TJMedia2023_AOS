package com.verse.app.ui.dialogfragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.databinding.DialogFragmentReportBinding
import com.verse.app.model.community.CommunityLoungeData
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 차단, 신고 Dialog Fragment
 *
 * Created by juhongmin on 2023/05/21
 */
@AndroidEntryPoint
class ReportDialogFragment :
    BaseBottomSheetDialogFragment<DialogFragmentReportBinding, FragmentViewModel>() {
    override val layoutId: Int = R.layout.dialog_fragment_report
    override val viewModel: FragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    data class ReportData(
        val manageCode: String = "", // 카테고리 관리자 코드
        val contentsCode: String = "",
        val memberCode: String = ""
    ) {
        constructor(data: CommunityLoungeData) : this(
            manageCode = data.code,
            contentsCode = "",
            memberCode = data.memberCode
        )
    }

    private var reportData: ReportData? = null

    private var listener: Listener? = null

    interface Listener {
        fun onBlockConfirm(data: ReportData)
        fun onReportConfirm(data: ReportData)
    }

    fun setData(data: CommunityLoungeData): ReportDialogFragment {
        reportData = ReportData(data)
        return this
    }

    fun setListener(listener: Listener): ReportDialogFragment {
        this.listener = listener
        return this
    }

    override fun onStart() {
        super.onStart()
        if (dialog is BottomSheetDialog) {
            (dialog as BottomSheetDialog).runCatching {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvBlock.setOnClickListener {
                reportData?.let { listener?.onBlockConfirm(it) }
                dismiss()
            }

            tvReport.setOnClickListener {
                reportData?.let { listener?.onReportConfirm(it) }
                dismiss()
            }
        }
    }

    fun simpleShow(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "ReportDialogFragment")
            }
        } catch (ex: Exception) {
        }
    }

    override fun onClick(v: View?) {
        //
    }
}