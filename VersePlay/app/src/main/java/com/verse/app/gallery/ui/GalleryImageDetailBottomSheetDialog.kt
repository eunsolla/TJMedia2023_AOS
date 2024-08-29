package com.verse.app.gallery.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.databinding.DialogGalleryImageDetailBinding
import com.verse.app.extension.setReHeight
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@AndroidEntryPoint
class GalleryImageDetailBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogGalleryImageDetailBinding

    private var imageUrl: String = ""

    @Inject
    lateinit var deviceProvider: DeviceProvider

    fun setImageUrl(imageUrl: String): GalleryImageDetailBottomSheetDialog {
        if (!imageUrl.startsWith(AppData.PREFIX_TJ_SOUND) &&
            !imageUrl.startsWith(AppData.PREFIX_PROFILE) &&
            !imageUrl.startsWith(AppData.PREFIX_FEED) &&
            !imageUrl.startsWith(AppData.CHAT_RES)
        ) {
            this.imageUrl = imageUrl
        } else {
            this.imageUrl = Config.BASE_FILE_URL + imageUrl
        }
        return this
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val behavior = setBottomSheetReHeight(dialog as BottomSheetDialog, getRealContentsHeight())
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        behavior?.skipCollapsed = true
        behavior?.isDraggable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<DialogGalleryImageDetailBinding>(
            inflater,
            R.layout.dialog_gallery_image_detail,
            container,
            false
        ).run {
            binding = this
            this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clBack.setOnClickListener { dismiss() }

        Glide.with(this)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_album_default)
                    .error(R.drawable.ic_album_default)
            )
            .load(imageUrl)
            .into(binding.iv)
    }

    fun simpleShow(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "GalleryImageDetailBottomSheetDialog")
            }
        } catch (ex: Exception) {
            // ignore
        }
    }

    private fun setBottomSheetReHeight(
        bottomSheetDialog: BottomSheetDialog,
        height: Int
    ): BottomSheetBehavior<View>? {
        return try {
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
            bottomSheet.setReHeight(height)
            BottomSheetBehavior.from(bottomSheet)
        } catch (ex: Exception) {
            null
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
}
