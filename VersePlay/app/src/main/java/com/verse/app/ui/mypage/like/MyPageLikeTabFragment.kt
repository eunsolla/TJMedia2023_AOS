package com.verse.app.ui.mypage.like

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FMyPageLikeRootBinding
import com.verse.app.extension.getFragment
import com.verse.app.model.enums.SortEnum
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 마이페이지 > [좋아요]
 *
 * Created by juhongmin on 2023/05/29
 */
@AndroidEntryPoint
class MyPageLikeTabFragment :
    BaseFragment<FMyPageLikeRootBinding, MyPageLikeTabFragmentViewModel>() {
    override val layoutId: Int = R.layout.f_my_page_like_root
    override val viewModel: MyPageLikeTabFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    @Inject
    lateinit var resProvider: ResourceProvider

    private var adapter: PagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (adapter == null) {
            binding.vp.adapter = PagerAdapter()
        }

        with(viewModel) {
            startFilterDialogEvent.observe(viewLifecycleOwner) {
                showFilterDialog(it)
            }
        }
    }

    override fun onDestroyView() {
        binding.vp.adapter = null
        super.onDestroyView()
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                MyPageLikeUserPerformanceFragment.newInstance(arguments)
            } else {
                MyPageLikeUserNormalFragment.newInstance(arguments)
            }
        }
    }

    private fun showFilterDialog(pos: Int) {
        // 공연
        if (pos == 0) {
            val fragment = getFragment(pos, binding.vp) as? MyPageLikeUserPerformanceFragment
            if (fragment != null) {
                val currentSort = fragment.viewModel.getCurrentSort()
                FilterDialogFragment()
                    .setBtnOneName(
                        requireActivity().getString(R.string.filter_order_by_latest),
                        currentSort == SortEnum.DESC
                    )
                    .setBtnTwoName(
                        requireActivity().getString(R.string.filter_order_by_old),
                        currentSort == SortEnum.ASC
                    )
                    .setListener(fragment.viewModel)
                    .simpleShow(childFragmentManager)
            }
        } else {
            // 개인 소장
            val fragment = getFragment(pos, binding.vp) as? MyPageLikeUserNormalFragment
            if (fragment != null) {
                val currentSort = fragment.viewModel.getCurrentSort()
                FilterDialogFragment()
                    .setBtnOneName(
                        requireActivity().getString(R.string.filter_order_by_latest),
                        currentSort == SortEnum.DESC
                    )
                    .setBtnTwoName(
                        requireActivity().getString(R.string.filter_order_by_old),
                        currentSort == SortEnum.ASC
                    )
                    .setListener(fragment.viewModel)
                    .simpleShow(childFragmentManager)
            }
        }
    }

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            return MyPageLikeTabFragment().apply {
                arguments = bundle
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onLoadingDismiss()
    }
}
