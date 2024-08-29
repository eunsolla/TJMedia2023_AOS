package com.verse.app.ui.community.vote

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentCommunityVoteEndBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 커뮤니티 > 투표 > [마감]
 *
 * Created by juhongmin on 2023/05/17
 */
@AndroidEntryPoint
class CommunityVoteEndFragment :
    BaseFragment<FragmentCommunityVoteEndBinding, CommunityVoteEndFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_community_vote_end
    override val viewModel: CommunityVoteEndFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
    }

    companion object {
        fun newInstance(): Fragment = CommunityVoteEndFragment()
    }
}