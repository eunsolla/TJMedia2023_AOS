package com.verse.app.ui.lounge.write

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.databinding.FragmentLoungeWriteBinding
import com.verse.app.extension.requestDisallowInterceptTouchEvent
import com.verse.app.extension.showSoftKeyboard
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.LoungeLinkDialog
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 라운지 상세 > [작성]
 *
 * Created by juhongmin on 2023/05/18
 */
@AndroidEntryPoint
class LoungeWriteFragment :
    BaseFragment<FragmentLoungeWriteBinding, LoungeWriteFragmentViewModel>() {

    override val layoutId: Int = R.layout.fragment_lounge_write
    override val viewModel: LoungeWriteFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val commentTextLength = viewModel.contentsText.value?.length ?: 0
            if (commentTextLength > 0 || viewModel.currentImageCount > 0 || viewModel.currentLinkData != null) {
                showLoungeBackPressDialog()
            } else {
                requireActivity().finish()
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener { onBackPressedCallback.handleOnBackPressed() }
        binding.etContents.requestDisallowInterceptTouchEvent()

        with(viewModel) {
            startCheckProhibit.observe(viewLifecycleOwner) {
                CommonDialog(requireActivity())
                    .setContents(getString(R.string.str_prohibit_notice))
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                viewModel.startFinishEvent(true)
                            }
                        }
                    })
                    .show()
            }

            startCheckPrivateAccount.observe(viewLifecycleOwner) {
                CommonDialog(requireActivity())
                    .setContents(getString(R.string.comment_private_account_popup))
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }

            startFinishEvent.observe(viewLifecycleOwner) {
                if (it) {
                    CommonDialog(requireContext())
                        .setContents(R.string.lounge_register_completed)
                        .setPositiveButton(R.string.str_confirm)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    requireActivity().finish()
                                }
                            }
                        })
                        .show()
                } else {
                    requireActivity().finish()
                }
            }

            startGalleryDialogEvent.observe(viewLifecycleOwner) {
                handlePermissions(it)
            }

            startLinkUrlDialogEvent.observe(viewLifecycleOwner) {
                showLoungeLinkDialog()
            }

            startInvalidLoungeEvent.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(R.string.lounge_write_invalid_message)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            showInvalidUrlPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setIcon(AppData.POPUP_WARNING)
                    .setContents(R.string.lounge_write_link_invalid)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            deleteLinkData.observe(viewLifecycleOwner) {
                binding.apply {
                    binding.tvLinkUrl1.text = ""
                    binding.tvLinkUrl2.text = ""
                    binding.clLinkLayout.visibility = View.GONE
                }
            }

            moveToLinkUrl.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    requireActivity().startActivity(intent)
                }
            }
        }

        binding.etContents.showSoftKeyboard(500)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }


    private fun handlePermissions(maxPicker: Int) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    showGalleryDialog(maxPicker)
                } else {
                    // 권한 거부 팝업
                    CommonDialog(requireContext())
                        .setContents(resources.getString(R.string.str_permission_denied))
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    private fun showGalleryDialog(maxCount: Int) {
        if (maxCount == 0) {
            CommonDialog(requireContext())
                .setIcon(AppData.POPUP_WARNING)
                .setContents(getString(R.string.gallery_max_count_message, 10))
                .setPositiveButton(R.string.str_confirm)
                .show()
            return
        }
        GalleryBottomSheetDialog()
            .setMaxPicker(maxCount)
            .setListener(viewModel)
            .simpleShow(childFragmentManager)
    }

    private fun showLoungeLinkDialog() {
        if (binding.ivThumb.visibility == View.VISIBLE) {
            LoungeLinkDialog()
                .setListener(binding.tvLinkUrl1.text.toString(), viewModel)
                .simpleShow(childFragmentManager)
        } else {
            LoungeLinkDialog()
                .setListener(binding.tvLinkUrl2.text.toString(), viewModel)
                .simpleShow(childFragmentManager)
        }
    }

    private fun showLoungeBackPressDialog() {
        CommonDialog(requireContext())
            .setContents(R.string.lounge_write_back_press_info)
            .setPositiveButton(R.string.str_lounge_cancle)
            .setNegativeButton(R.string.str_cancel)
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        requireActivity().finish()
                    }
                }
            })
            .show()
    }
}
