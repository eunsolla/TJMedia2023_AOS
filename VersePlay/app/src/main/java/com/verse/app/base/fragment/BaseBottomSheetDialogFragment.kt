package com.verse.app.base.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.extension.setReHeight
import com.verse.app.utility.DLogger

/**
 * Description :  Fragment
 *
 * Created by jhlee on 2023-01-31
 */
abstract class BaseBottomSheetDialogFragment<Binding : ViewDataBinding, VM : BaseViewModel> : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: Binding? = null
    val binding get() = _binding!!

    abstract val layoutId: Int
    abstract val viewModel: VM
    abstract val bindingVariable: Int
    private var isActivityViewModel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return DataBindingUtil.inflate<Binding>(
            inflater,
            layoutId,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            setVariable(bindingVariable, viewModel)
            _binding = this

            runCatching {
                ViewModelProvider(requireActivity())[viewModel::class.java]
            }.onFailure {
                // viewModel -> Activity ViewModel 이 아니다.
                DLogger.d("viewModel -> Activity ViewModel 이 아니다. ")
                isActivityViewModel = false
            }.onSuccess {
                // viewModel -> Activity ViewModel 인경우.
                DLogger.d("viewModel ->  Activity ViewModel 이다. ")
                isActivityViewModel = true
            }
        }.root
    }

    /**
     * BottomSheet 높이값 강제로 재조정 하는 함수
     * onShow 에서 해당 함수를 호출합니다.
     * BottomSheet 높이값 설정 하고 BottomSheetBehavior Config 셋팅할수 있도록 리턴
     * @return BottomSheetBehavior
     */
    protected fun setBottomSheetReHeight(
        dialogInterface: DialogInterface,
        height: Int
    ): BottomSheetBehavior<View>? {
        return try {
            setBottomSheetReHeight(dialogInterface as BottomSheetDialog, height)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * BottomSheet 높이값 강제로 재조정 하는 함수
     * onShow 에서 해당 함수를 호출합니다.
     * BottomSheet 높이값 설정 하고 BottomSheetBehavior Config 셋팅할수 있도록 리턴
     * @return BottomSheetBehavior
     */
    protected fun setBottomSheetReHeight(
        bottomSheetDialog: BottomSheetDialog,
        height: Int
    ): BottomSheetBehavior<View>? {
        return try {
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
            bottomSheet.setReHeight(height)
            BottomSheetBehavior.from(bottomSheet)
        } catch (ex: Exception) {
            null
        }
    }
}