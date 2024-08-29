package com.verse.app.ui.community.event

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentCommunityEventBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 커뮤니티 > [이벤트]
 *
 * Created by juhongmin on 2023/05/16
 */
@AndroidEntryPoint
class CommunityEventFragment :
    BaseFragment<FragmentCommunityEventBinding, CommunityEventFragmentViewModel>() {

    override val layoutId: Int = R.layout.fragment_community_event
    override val viewModel: CommunityEventFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private val adapter: PagerAdapter by lazy { PagerAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vp.adapter = adapter
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                CommunityEventIngFragment.newInstance()
            } else {
                CommunityEventEndFragment.newInstance()
            }
        }
    }

    companion object {
        fun newInstance(): Fragment = CommunityEventFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onLoadingDismiss()
    }
}
