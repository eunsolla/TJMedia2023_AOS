package com.verse.app.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Description :  Fragment
 *
 * Created by jhlee on 2023-01-31
 */
abstract class BaseDialogFragment<Binding : ViewDataBinding> : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: Binding? = null
    val binding get() = _binding!!

    abstract val layoutId: Int

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
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}