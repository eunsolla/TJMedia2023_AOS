package com.verse.app.gallery.ui

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.contants.ShowImageDetailType
import com.verse.app.databinding.DialogGalleryImageEditBinding
import com.verse.app.extension.setReHeight
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.crop.CompressFile
import com.verse.app.ui.mypage.crop.CompressFile.IMGTYPE_JPG
import com.verse.app.ui.mypage.crop.CompressFile.IMGTYPE_PNG
import com.verse.app.ui.mypage.crop.CroppedShape
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@AndroidEntryPoint
class GalleryImageEditBottomSheetDialog(val showType: String) : BottomSheetDialogFragment(), DialogInterface.OnKeyListener {

    private lateinit var binding: DialogGalleryImageEditBinding

    private var imageUrl: String = ""

    private var uri: Uri? = null

    private var imageBitmap: Bitmap? = null

    var resultImg: File? = null

    private var editConfirmListener: Listener? = null

    @Inject
    lateinit var deviceProvider: DeviceProvider

    interface Listener {
        fun onEditConfirm(imagePath: String)
    }

    fun setImageUrl(imageUri: Uri): GalleryImageEditBottomSheetDialog {
        this.uri = imageUri

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
        return DataBindingUtil.inflate<DialogGalleryImageEditBinding>(
            inflater,
            R.layout.dialog_gallery_image_edit,
            container,
            false
        ).run {
            binding = this
            this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clBack.setOnClickListener {
            showCancelPopupDialog()
        }

        dialog?.let {
            it.setOnKeyListener(this)
        }

        requireActivity().runOnUiThread(Runnable {
            if (Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    this.uri
                )
                binding.ivCrop.setImageBitmap(bitmap)
            } else {
                val source = ImageDecoder.createSource(
                    requireActivity().contentResolver,
                    this.uri!!
                )
                val bitmap = ImageDecoder.decodeBitmap(source)
                binding.ivCrop.setImageBitmap(bitmap)
            }
        })

        initOverlay()

        binding.tvConfirm.setOnClickListener {
            if (showType == ShowImageDetailType.EDIT_FR_PROFILE.code) {
                setProfileImage()
            } else if (showType == ShowImageDetailType.EDIT_BG_PROFILE.code) {
                setBackgroundImage()
            } else if (showType == ShowImageDetailType.EDIT_ATTACH_IMAGE.code) {
                setBackgroundImage()
            } else {
                setProfileImage()
            }

            if (editConfirmListener != null) {
                if (resultImg != null) {
                    editConfirmListener!!.onEditConfirm(resultImg!!.path)
                }
            }
        }
    }

    /**
     * "확인" 버튼 선택시 이벤트 처리하는 리스너
     */
    fun setListener(listener: Listener): GalleryImageEditBottomSheetDialog {
        editConfirmListener = listener
        return this
    }

    fun initOverlay() {
        if (showType == ShowImageDetailType.EDIT_FR_PROFILE.code) {
            binding.ivCrop.init(CroppedShape.HOLE)
        } else if (showType == ShowImageDetailType.EDIT_BG_PROFILE.code) {
            binding.ivCrop.init(CroppedShape.SQUARE)
        }  else if (showType == ShowImageDetailType.EDIT_ATTACH_IMAGE.code) {
            binding.ivCrop.init(CroppedShape.SQUARE)
        } else {
            binding.ivCrop.init(CroppedShape.HOLE)
        }
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

    fun setBackgroundImage() {
        // 파일형식에 따라 압축양 다르게 해야함.
        var imgType = -1
        imgType = IMGTYPE_JPG
        try {
            resultImg = File.createTempFile("profile_temp", ".jpg", requireActivity().cacheDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        resultImg = CompressFile.compressFile(
            binding.ivCrop.croppedBitmapForBackground,
            600,
            600,
            imgType,
            resultImg
        )

        dismiss()
    }


    fun setProfileImage() {
        var imgType = -1
        imgType = IMGTYPE_PNG
        try {
            resultImg = File.createTempFile("profile_temp", ".jpg", requireActivity().cacheDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (resultImg != null) {
            resultImg =
                CompressFile.compressFile(binding.ivCrop.croppedBitmap, 300, 300, imgType, resultImg)

        }

        dismiss()
    }

    override fun onKey(p0: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action ==KeyEvent.ACTION_UP) {
            showCancelPopupDialog()
            return true
        }
        return false
    }

    fun showCancelPopupDialog() {
        CommonDialog(requireContext())
            .setIcon(AppData.POPUP_WARNING)
            .setContents(R.string.videoupload_picker_cancle_gallery)
            .setPositiveButton(R.string.str_confirm)
            .setNegativeButton(R.string.str_cancel)
            .setListener(
                object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (which == 1) {
                            dismiss()
                        }
                    }

                })
            .show()
    }
}
