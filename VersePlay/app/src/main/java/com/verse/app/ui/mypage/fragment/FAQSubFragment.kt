package com.verse.app.ui.mypage.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentFaqSubBinding
import com.verse.app.extension.popBackStackParentFragment
import com.verse.app.ui.mypage.viewmodel.FAQViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : faq 서브
 *
 * Created by esna on 2023-04-23
 */
@AndroidEntryPoint
class FAQSubFragment : BaseFragment<FragmentFaqSubBinding, FAQViewModel>() {

    override val layoutId: Int = R.layout.fragment_faq_sub
    override val viewModel: FAQViewModel by activityViewModels()
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
            binding.vHeader.setHeaderTitle(subFAQTitle.value)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}