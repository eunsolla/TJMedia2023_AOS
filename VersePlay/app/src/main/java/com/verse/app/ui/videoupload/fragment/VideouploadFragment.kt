package com.verse.app.ui.videoupload.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.databinding.FragmentVideouploadBinding
import com.verse.app.extension.onMain
import com.verse.app.extension.showKeyboard
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.videoupload.viewmodel.VideouploadViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.regex.Pattern


@AndroidEntryPoint
class VideouploadFragment : BaseFragment<FragmentVideouploadBinding, VideouploadViewModel>(),
    CommonDialog.Listener {

    override val layoutId: Int = R.layout.fragment_videoupload
    override val viewModel: VideouploadViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    val MAX_INPUT_TEXT_SIZE: Int = 150
    var isShowCommonDialog: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 피드 노트 작성 시 #입력하지 못하도록 필터 적용
            val feedNoteHashTagFilter =
                InputFilter { source, start, end, dest, dstart, dend ->
                    val ps: Pattern = Pattern.compile("[#]*$")
                    if (ps.matcher(source).matches()) {
                        ""
                    } else null
                }

            binding.etMessage.filters = arrayOf(feedNoteHashTagFilter)
            onMain {
                delay(500)
                etMessage.requestFocus()
                showKeyboard(requireContext(), etMessage)
            }
        }


        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (binding.etMessage.text.length > MAX_INPUT_TEXT_SIZE) {
                    binding.etMessage.setText(binding.etMessage.text.substring(0, MAX_INPUT_TEXT_SIZE))
                    binding.etMessage.setSelection(binding.etMessage.text.length)

                    if (!isShowCommonDialog) {
                        isShowCommonDialog = true

                        CommonDialog(requireContext())
                            .setListener(object : CommonDialog.Listener {
                                override fun onClick(which: Int) {
                                    isShowCommonDialog = false
                                }
                            })
                            .setContents(R.string.str_post_text_popup)
                            .setIcon(AppData.POPUP_WARNING)
                            .setPositiveButton(R.string.str_confirm)
                            .show()
                    }
                }
            }
        })

        binding.etHash.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (binding.etHash.text.length > MAX_INPUT_TEXT_SIZE) {
                    binding.etHash.setText(binding.etHash.text.substring(0, MAX_INPUT_TEXT_SIZE))
                    binding.etHash.setSelection(binding.etHash.text.length)

                    if (!isShowCommonDialog) {
                        isShowCommonDialog = true

                        CommonDialog(requireContext())
                            .setListener(object : CommonDialog.Listener {
                                override fun onClick(which: Int) {
                                    isShowCommonDialog = false
                                }
                            })
                            .setContents(R.string.str_post_text_popup)
                            .setIcon(AppData.POPUP_WARNING)
                            .setPositiveButton(R.string.str_confirm)
                            .show()
                    }
                }
            }
        })

    }

    override fun onClick(which: Int) {
        TODO("Not yet implemented")
    }
}