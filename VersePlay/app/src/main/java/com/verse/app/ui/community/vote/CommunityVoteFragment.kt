package com.verse.app.ui.community.vote

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentCommunityVoteBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 커뮤니티 > [투표]
 *
 * Created by juhongmin on 2023/05/16
 */
@AndroidEntryPoint
class CommunityVoteFragment :
    BaseFragment<FragmentCommunityVoteBinding, CommunityVoteFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_community_vote
    override val viewModel: CommunityVoteFragmentViewModel by viewModels()
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
                CommunityVoteParticipationFragment.newInstance()
            } else {
                CommunityVoteEndFragment.newInstance()
            }
        }
    }

    companion object {
        fun newInstance(): Fragment = CommunityVoteFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onLoadingDismiss()
    }
}