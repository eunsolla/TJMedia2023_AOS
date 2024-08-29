package com.verse.app.ui.mypage.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentMembershipPeriodBinding
import com.verse.app.ui.mypage.viewmodel.MemberShipViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 멤버십 관리 기간 이용권
 *
 * Created by jsaprk on 2023/05/21
 */
@AndroidEntryPoint
class MemberShipPeriodFragment :
    BaseFragment<FragmentMembershipPeriodBinding, MemberShipViewModel>() {
    override val layoutId: Int = R.layout.fragment_membership_period
    override val viewModel: MemberShipViewModel by activityViewModels<MemberShipViewModel>()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            refreshSubscItemView.observe(viewLifecycleOwner) {
                binding.rvSubsc.adapter?.let {
                    it.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        fun newInstance(): Fragment = MemberShipPeriodFragment()
    }

    fun getFragment(): MemberShipPeriodFragment {
        return this
    }
}
