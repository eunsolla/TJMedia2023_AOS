package com.verse.app.ui.sing.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.SingType
import com.verse.app.databinding.FragmentPartInfoBinding
import com.verse.app.extension.customGetSerializable
import com.verse.app.ui.sing.viewmodel.SingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 파트,솔로,듀엣 선택
 *
 * Created by jhlee on 2023-04-06
 */
@AndroidEntryPoint
class PartInfoFragment : BaseFragment<FragmentPartInfoBinding, SingViewModel>() {

    override val layoutId: Int = R.layout.fragment_part_info
    override val viewModel: SingViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {

            arguments?.let {
                val curType = it.customGetSerializable<SingType>(ExtraCode.SING_TYPE)
                binding.singType = curType
            }

        }
    }

    override fun onDetach() {
        super.onDetach()
    }
}