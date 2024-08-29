package com.verse.app.gallery.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.databinding.ItemGalleryImageBinding
import com.verse.app.extension.getFragmentActivity

/**
 * Description : 갤러리 이미지 전용 ViewHolder
 *
 * Created by juhongmin on 2023/05/13
 */
class GalleryImageViewHolder(
    parent: ViewGroup,
    private val viewModel: GalleryBottomSheetDialogViewModel? = null
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_gallery_image,
        parent, false
    )
) {

    val binding: ItemGalleryImageBinding by lazy { ItemGalleryImageBinding.bind(itemView) }

    init {
        bindingRequestManager()

        val deviceWidth = itemView.resources.displayMetrics.widthPixels
        binding.setVariable(BR.overrideSize, deviceWidth / 3)

        binding.ivThumb.setOnClickListener {
            handleAddPhotoClick()
        }

        binding.clSelectNum.setOnClickListener {
            handleRemovePhotoClick()
        }

        binding.clSelectCheck.setOnClickListener {
            handleRemovePhotoClick()
        }

        binding.ivFullScreen.setOnClickListener {
            showImageDetailDialog()
        }
    }

    fun onBindView(item: GalleryItem) {
        binding.setVariable(BR.item, item)
    }

    /**
     * Payload BindView
     * @param payloads Payload DataList
     */
    fun onBindView(payloads: List<Any>) {
        binding.item?.runCatching {
            for (idx: Int in payloads.indices) {
                val item = payloads[idx]
                if (item is GalleryItem.Image) {
                    if (item.imagePath == this.imagePath) {
                        onBindView(item)
                        break
                    }
                }
            }
        }
    }

    /**
     * Glide.RequestManager 가 필요한 ViewHolder 에서 호출하는 함수
     */
    private fun bindingRequestManager() {
        itemView.getFragmentActivity()?.let {
            binding.setVariable(BR.requestManager, Glide.with(it))
        }
    }

    /**
     * Perform Add Photo Click Function
     */
    private fun handleAddPhotoClick() {
        binding.item?.runCatching {
            // 선택 하는 경우
            if (viewModel?.isSelectedCheck?.value == true) {
                isSelectCheck = true

                if (!isSelected) {
                    // Max Size
                    if (viewModel.checkMaxPickerCount()) {
                        return@runCatching
                    }

                    // 추가인 경우 나머지 갱신 처리할 필요가 없음
                    isSelected = true
                    viewModel.onPhotoPicker(true, this)
                    binding.invalidateAll()
                } else {
                    handleRemovePhotoClick()
                }

            } else {
                isSelectCheck = false
                if (!isSelected) {
                    // Max Size
                    if (viewModel?.checkMaxPickerCount() == true) {
                        return@runCatching
                    }

                    // 추가인 경우 나머지 갱신 처리할 필요가 없음
                    isSelected = true
                    viewModel?.onPhotoPicker(true, this)
                    binding.invalidateAll()
                } else {
                    handleRemovePhotoClick()
                }
            }
        }
    }

    /**
     * Perform Remove Photo Click
     */
    private fun handleRemovePhotoClick() {
        binding.item?.runCatching {
            if (isSelected) {
                isSelected = false
                viewModel?.onPhotoPicker(false, this)
                rangeNotifyPayload(this)
            }
        }
    }

    /**
     * 선택한 아이템들만 갱신 처리하도록 하는 함수
     * @param changeItem Current Selected Item
     */
    private fun rangeNotifyPayload(changeItem: GalleryItem) {
        if (itemView.parent is RecyclerView) {
            val layoutManager = (itemView.parent as RecyclerView).layoutManager
            if (layoutManager is GridLayoutManager) {
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                val notifyList = mutableListOf<GalleryItem>()
                notifyList.add(changeItem)
                viewModel?.getGalleryPhotoPicker()?.forEach { notifyList.add(it) }

                // 현재 보여지고 있는 뷰에 대해서만 갱신 처리
                bindingAdapter?.notifyItemRangeChanged(
                    firstPosition,
                    lastPosition.plus(1),
                    notifyList
                )
            }
        }
    }

    private fun showImageDetailDialog() {
        binding.item?.runCatching {
            val activity = itemView.getFragmentActivity() ?: return@runCatching
            GalleryImageDetailBottomSheetDialog()
                .setImageUrl(imagePath)
                .simpleShow(activity.supportFragmentManager)
        }
    }
}