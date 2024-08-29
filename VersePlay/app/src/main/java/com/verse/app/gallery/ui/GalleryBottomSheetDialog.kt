package com.verse.app.gallery.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.contants.AppData
import com.verse.app.databinding.DialogFragmentGalleryBinding
import com.verse.app.extension.dp
import com.verse.app.ui.decoration.GridDecorator
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 갤러리 화면 BottomSheet
 *
 * Created by juhongmin on 2023/05/13
 */
@AndroidEntryPoint
class GalleryBottomSheetDialog :
    BaseBottomSheetDialogFragment<DialogFragmentGalleryBinding, GalleryBottomSheetDialogViewModel>() {

    override val layoutId: Int = R.layout.dialog_fragment_gallery
    override val viewModel: GalleryBottomSheetDialogViewModel by viewModels()
    override val bindingVariable: Int = BR.vm

    interface Listener {
        fun onGalleryConfirm(imageList: List<String>)
        fun onGalleryDismiss()
    }

    private var galleryConfirmListener: Listener? = null

    @Inject
    lateinit var deviceProvider: DeviceProvider

    @Inject
    lateinit var resProvider: ResourceProvider

    private val adapter: GalleryAdapter by lazy { GalleryAdapter() }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val behavior = setBottomSheetReHeight(dialog as BottomSheetDialog, getRealContentsHeight())
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        behavior?.skipCollapsed = true
        behavior?.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.getIntentData(GalleryBottomSheetDialogViewModel.IS_VIDEO_TYPE, false)) {
            adapter.setVideoType()
        }
        adapter.viewModel = viewModel
        binding.rvContents.addItemDecoration(GridDecorator(3, 1.dp, false))
        binding.rvContents.adapter = adapter
        viewModel.isSelectedCheck.value = viewModel.maxCount == 1

        with(viewModel) {
            dataList.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

            isGalleryNull.observe(viewLifecycleOwner) {
                binding.vEmpty.visibility = View.VISIBLE
                binding.rvContents.visibility = View.GONE
            }

            startCloseEvent.observe(viewLifecycleOwner) {
                dismiss()
            }

            startConfirmEvent.observe(viewLifecycleOwner) {
                galleryConfirmListener?.onGalleryConfirm(it)
                dismiss()
            }

            startShowMaxDialog.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setIcon(AppData.POPUP_WARNING)
                    .setContents(it.toString())
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            startConfirmInvalidEvent.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setIcon(AppData.POPUP_WARNING)
                    .setContents(it)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            start()
        }
    }

    override fun onClick(v: View?) {
        // ignore
    }

    /**
     * "확인" 버튼 선택시 이벤트 처리하는 리스너
     */
    fun setListener(listener: Listener): GalleryBottomSheetDialog {
        galleryConfirmListener = listener
        return this
    }

    /**
     * 사진을 선택할수 있는 최대 개수
     */
    fun setMaxPicker(cnt: Int): GalleryBottomSheetDialog {
        if (arguments == null) {
            arguments = Bundle().apply {
                putInt(GalleryBottomSheetDialogViewModel.MAX_COUNT, cnt)
            }
        } else {
            arguments?.putInt(GalleryBottomSheetDialogViewModel.MAX_COUNT, cnt)
        }
        return this
    }

    /**
     * 비디오 선택할수 있는 최대 시간
     */
    fun setMaxDuration(duration: Long): GalleryBottomSheetDialog {
        if (arguments == null) {
            arguments = Bundle().apply {
                putLong(GalleryBottomSheetDialogViewModel.MAX_DURATION, duration)
            }
        } else {
            arguments?.putLong(GalleryBottomSheetDialogViewModel.MAX_DURATION, duration)
        }
        return this
    }

    fun setVideoType(): GalleryBottomSheetDialog {
        if (arguments == null) {
            arguments = Bundle().apply {
                putBoolean(GalleryBottomSheetDialogViewModel.IS_VIDEO_TYPE, true)
            }
        } else {
            arguments?.putBoolean(GalleryBottomSheetDialogViewModel.IS_VIDEO_TYPE, true)
        }
        return this
    }

    /**
     * 최소 N 개 선택에 대한 안내 문구 셋팅하는 함수
     * @param str 안내 문구
     * @see R.string.gallery_invalid_confirm_message Default
     */
    fun setConfirmInvalidText(str: String): GalleryBottomSheetDialog {
        if (arguments == null) {
            arguments = Bundle().apply {
                putString(GalleryBottomSheetDialogViewModel.CONFIRM_INVALID, str)
            }
        } else {
            arguments?.putString(GalleryBottomSheetDialogViewModel.CONFIRM_INVALID, str)
        }
        return this
    }

    /**
     * 최대 N 개 이상 선택시 안내 문구 셋팅 하는 함수
     * @param str 안내 문구
     * @see R.string.gallery_max_count_message Default
     */
    fun setMaxSelectText(str: CharSequence): GalleryBottomSheetDialog {
        if (arguments == null) {
            arguments = Bundle().apply {
                putCharSequence(GalleryBottomSheetDialogViewModel.MAX_SELECTED, str)
            }
        } else {
            arguments?.putCharSequence(GalleryBottomSheetDialogViewModel.MAX_SELECTED, str)
        }
        return this
    }

    fun simpleShow(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "GalleryBottomSheetDialog")
            }
        } catch (ex: Exception) {
            // ignore
        }
    }

    /**
     * 상태바, 네비게이션바를 제외한 실제 콘텐츠 높이
     */
    private fun getRealContentsHeight(): Int {
        var deviceRealHeight = deviceProvider.getDeviceHeight()
        if (deviceProvider.isNavigationBar()) {
            deviceRealHeight -= deviceProvider.getNavigationBarHeight()
        }
        deviceRealHeight -= deviceProvider.getStatusBarHeight()
        return deviceRealHeight
    }

    override fun onDetach() {
        if(viewModel.startConfirmEvent.value.isNullOrEmpty()) {
            galleryConfirmListener?.onGalleryDismiss()
        }
        super.onDetach()
    }
}
