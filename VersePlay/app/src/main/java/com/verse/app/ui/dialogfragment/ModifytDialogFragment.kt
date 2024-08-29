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
import com.verse.app.databinding.DialogFragmentModifyBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 수정, 삭제 Dialog Fragment
 *
 * Created by jhlee on 2023-06-15
 */
@AndroidEntryPoint
class ModifytDialogFragment : BaseBottomSheetDialogFragment<DialogFragmentModifyBinding, FragmentViewModel>() {
    override val layoutId: Int = R.layout.dialog_fragment_modify
    override val viewModel: FragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    data class ModifyData(
        val mngCode: String = ""
    )

    private var modifytData: ModifyData? = null

    private var listener: Listener? = null

    interface Listener {
        fun onModifyConfirm(data: String)
        fun onDeleteConfirm(data: String)
    }

    fun setData(code: String): ModifytDialogFragment {
        modifytData = ModifyData(code)
        return this
    }

    fun setListener(listener: Listener): ModifytDialogFragment {
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
            tvModify.setOnClickListener {
                modifytData?.let { it.mngCode?.let { code -> listener?.onModifyConfirm(code) } }
                dismiss()
            }

            tvDelete.setOnClickListener {
                modifytData?.let { it.mngCode?.let { code -> listener?.onDeleteConfirm(code) } }
                dismiss()
            }
        }
    }

    fun simpleShow(fm: FragmentManager) {
        try {
            if (!isAdded) {
                super.show(fm, "ModifytDialogFragment")
            }
        } catch (ex: Exception) {
        }
    }

    override fun onClick(v: View?) {
        //
    }
}