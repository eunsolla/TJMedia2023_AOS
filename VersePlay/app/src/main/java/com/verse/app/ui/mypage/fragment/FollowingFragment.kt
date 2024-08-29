package com.verse.app.ui.mypage.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentFollowingBinding
import com.verse.app.extension.popBackStackFragment
import com.verse.app.ui.mypage.viewmodel.MypageFollowListViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowingFragment : BaseFragment<FragmentFollowingBinding, MypageFollowListViewModel>() {

    override val layoutId: Int = R.layout.fragment_following
    override val viewModel: MypageFollowListViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().popBackStackFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            pageTabState(1)
        }

    }

    override fun onDestroy() {
        DLogger.d("### ReportSubFragment onDestroy")
        super.onDestroy()
    }
}