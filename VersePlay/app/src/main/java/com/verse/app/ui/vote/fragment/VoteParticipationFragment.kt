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
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.databinding.FragmentVoteParticipationBinding
import com.verse.app.extension.shareDynamicLink
import com.verse.app.extension.startAct
import com.verse.app.model.vote.VoteIntentModel
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.vote.VoteRootActivity
import com.verse.app.ui.vote.viewmodel.VoteParticipationFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@AndroidEntryPoint
class VoteParticipationFragment :
    BaseFragment<FragmentVoteParticipationBinding, VoteParticipationFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_vote_participation
    override val viewModel: VoteParticipationFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(BR.requestManager, Glide.with(this))
        with(viewModel) {

            startFinishEvent.observe(viewLifecycleOwner) {
                requireActivity().finish()
            }

            startShareEvent.observe(viewLifecycleOwner) {
                // share
                requireActivity().shareDynamicLink(
                    path = DynamicLinkPathType.MAIN.name,
                    targetPageCode = LinkMenuTypeCode.LINK_COMMUNITY_VOTE.code,
                    title = it.title,
                    description = getString(R.string.str_try_vote),
                    imgUrl = Config.BASE_FILE_URL + it.imagePath
                )
            }

            startConfirmDialogEvent.observe(viewLifecycleOwner) {
                showVoteConfirm(it)
            }

            startVoteDisableEvent.observe(viewLifecycleOwner) {
                showDisableVoteMessage()
            }

            startMoveToEndPageEvent.observe(viewLifecycleOwner) {
                moveToDetailPage(it)
            }

            start()
        }
    }

    private fun showVoteConfirm(selectPos: Int) {
        CommonDialog(requireContext())
            .setContents(R.string.str_vote_selected_confirm_message)
            .setPositiveButton(R.string.str_try_vote)
            .setNegativeButton(R.string.str_cancel)
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        viewModel.onSelectedVoteConfirm(selectPos)
                    } else {
                        viewModel.onSelectedCancel()
                    }
                }
            })
            .show()
    }

    private fun showDisableVoteMessage() {
        CommonDialog(requireContext())
            .setContents(R.string.str_vote_disable_message)
            .setPositiveButton(R.string.str_confirm)
            .show()
    }

    private fun moveToDetailPage(code: String) {
        val model = VoteIntentModel(
            type = VoteIntentModel.Type.END,
            code = code
        )
        requireActivity().finish()
        requireActivity().startAct<VoteRootActivity> {
            putExtra(ExtraCode.VOTE_DETAIL_CODE, model)
        }
    }
}
