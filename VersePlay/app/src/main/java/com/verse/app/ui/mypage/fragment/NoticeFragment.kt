package com.verse.app.ui.mypage.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentNotiBinding
import com.verse.app.ui.mypage.viewmodel.NoticeViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeFragment  :
    BaseFragment<FragmentNotiBinding, NoticeViewModel>(){
    override val layoutId: Int = R.layout.fragment_noti
    override val viewModel: NoticeViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            binding.vHeader.setHeaderTitle(getString(R.string.setting_announcement))
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }


    override fun onDestroy() {
        DLogger.d("### NotiFragment onDestroy")
        super.onDestroy()
    }

}