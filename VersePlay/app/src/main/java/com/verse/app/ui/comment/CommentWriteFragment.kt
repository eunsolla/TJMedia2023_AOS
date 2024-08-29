package com.verse.app.ui.comment

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentCommentWriteBinding
import com.verse.app.extension.showKeyboard
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 댓글 남기기 Fragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class CommentWriteFragment : BaseFragment<FragmentCommentWriteBinding, CommentViewModel>() {
    override val layoutId: Int = R.layout.fragment_comment_write
    override val viewModel: CommentViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {

            binding.apply {
                showEditDialog.value?.let {
                    if (!it.second.isNullOrEmpty() && !it.third.isNullOrEmpty()) {
                        edtText.hint = String.format(
                            getString(R.string.str_comment_re_hint),
                            it.third
                        )
                    } else {
                        edtText.hint = getString(R.string.str_comment_hint)
                    }
                    edtText.requestFocus()
                    showKeyboard(requireContext(), binding.edtText)
                }
            }
        }
    }
}