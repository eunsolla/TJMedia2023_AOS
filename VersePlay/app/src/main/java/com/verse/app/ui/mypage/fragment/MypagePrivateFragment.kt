package com.verse.app.ui.mypage.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentMypagePrivateBinding
import com.verse.app.extension.viewPagerNotifyPayload
import com.verse.app.ui.mypage.viewmodel.MypagePrivateViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :  비공개 컨텐츠   Fragment
 *
 * Created by jhlee on 2023-06-08
 */
@AndroidEntryPoint
class MypagePrivateFragment : BaseFragment<FragmentMypagePrivateBinding, MypagePrivateViewModel>() {

    override val layoutId: Int = R.layout.fragment_mypage_private
    override val viewModel: MypagePrivateViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {

            refresh.observe(viewLifecycleOwner) {
                binding.vpPrivate.adapter?.notifyDataSetChanged()
            }
        }
    }
}