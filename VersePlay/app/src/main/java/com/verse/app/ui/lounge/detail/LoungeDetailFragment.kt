package com.verse.app.ui.lounge.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FragmentLoungeDetailBinding
import com.verse.app.extension.dp
import com.verse.app.extension.startAct
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.ui.decoration.VerticalDecorator
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialogfragment.ModifytDialogFragment
import com.verse.app.ui.lounge.LoungeRootActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/20
 */
@AndroidEntryPoint
class LoungeDetailFragment : BaseFragment<FragmentLoungeDetailBinding, LoungeDetailFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_lounge_detail
    override val viewModel: LoungeDetailFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            rvContents.addItemDecoration(VerticalDecorator(16.dp))
            tvLikeCount.text = "0"
            tvCommentCount.text = "0"
        }
        with(viewModel) {

            startCheckPrivateAccount.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }

            startFinishEvent.observe(viewLifecycleOwner) {
                requireActivity().finish()
            }

            startFinishModifyEvent.observe(viewLifecycleOwner){
                showLoungeDeleteCompletedDialog()
            }

            startModifyPageEvent.observe(viewLifecycleOwner) {
                showModifyDialog(it)
            }

            moveToLinkUrl.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    requireActivity().startActivity(intent)
                }
            }

            start()

            initStart()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,onBackPressedCallback)
    }


    private fun showModifyDialog(data: String) {
        ModifytDialogFragment()
            .setData(data)
            .setListener(object : ModifytDialogFragment.Listener{
                override fun onModifyConfirm(code: String) {
                    moveToLoungeModify(code)
                }
                override fun onDeleteConfirm(code: String) {
                    viewModel.deleteMyLounge(code)
                }
            })
            .simpleShow(childFragmentManager)
    }

    private fun showLoungeDeleteCompletedDialog() {
        CommonDialog(requireContext())
            .setContents(R.string.lounge_delete_completed)
            .setPositiveButton(R.string.str_confirm)
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        requireActivity().finish()
                    }
                }
            })
            .show()
    }


    private fun moveToLoungeModify(code: String) {
        requireActivity().finish()
        requireActivity().startAct<LoungeRootActivity> {
            putExtra(ExtraCode.WRITE_LOUNGE_DATA, LoungeIntentModel(code))
        }
    }
}