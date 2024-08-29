package com.verse.app.ui.vote.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.Config
import com.verse.app.contants.DynamicLinkPathType
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.databinding.FragmentVoteEndBinding
import com.verse.app.extension.shareDynamicLink
import com.verse.app.ui.vote.viewmodel.VoteEndFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@AndroidEntryPoint
class VoteEndFragment : BaseFragment<FragmentVoteEndBinding, VoteEndFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_vote_end
    override val viewModel: VoteEndFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(BR.requestManager, Glide.with(this))
        with(viewModel) {

            startFinishEvent.observe(viewLifecycleOwner) {
                requireActivity().finish()
            }

            startShareEvent.observe(viewLifecycleOwner) {
                requireActivity().shareDynamicLink(
                    path = DynamicLinkPathType.MAIN.name,
                    targetPageCode = LinkMenuTypeCode.LINK_COMMUNITY_VOTE.code,
                    title = it.title,
                    description = getString(R.string.str_try_vote),
                    imgUrl = Config.BASE_FILE_URL + it.imagePath
                )
            }

            start()
        }
    }
}