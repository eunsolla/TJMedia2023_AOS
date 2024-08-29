package com.verse.app.gallery.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Description : 갤러리 전용 Adapter & ViewHolder
 * 버튼 선택하고 여러가지 이벤트들이 특수해서 따로 둠
 * Created by juhongmin on 2023/05/13
 */
class GalleryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface GalleryListener {

        /**
         * Photo Picker Callback
         * @param isAdd true Picker, false No Picker
         * @param item Click GalleryItem
         */
        fun onPhotoPicker(isAdd: Boolean, item: GalleryItem)

        /**
         * 사진 최대로 선택할수 있는 개수를 조회 하는 함수
         * @return true 최대 선택할수 있는 사진 선택, false 사진 더 선택 가능함
         */
        fun checkMaxPickerCount(): Boolean

        /**
         * 비디오 선택할수 있는 시간을 조회 하는 함수
         * @return true 가능.  false 불가능
         */
        fun checkMaxVideoDuration(duration:Long): Boolean

        /**
         * Listener that can be cleared if clicked in duplicate.
         * @param item Click GalleryItem
         * @return true SamePhoto removeGallery
         */
        fun isCurrentPhoto(item: GalleryItem): Boolean

        fun getGalleryPhotoPicker(): List<GalleryItem>
    }

    class GalleryDiffUtil(
        private val oldList: List<GalleryItem>,
        private val newList: List<GalleryItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.imagePath == newItem.imagePath
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.imagePath == newItem.imagePath
        }
    }

    var viewModel: GalleryBottomSheetDialogViewModel? = null
    private var isVideoType: Boolean = false

    private val dataList: MutableList<GalleryItem> by lazy { mutableListOf() }

    fun setVideoType() {
        isVideoType = true
    }

    /**
     * 데이터가 변경되었을때 이전 데이터들 비교하여 갱신 처리 함수
     * @param newList oldList + 새로운 데이터 리스트
     */
    fun submitList(newList: List<GalleryItem>?) {
        if (newList == null) return

        val diffResult = DiffUtil.calculateDiff(GalleryDiffUtil(dataList, newList))
        dataList.clear()
        dataList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (isVideoType) {
            GalleryVideoViewHolder(parent, viewModel)
        } else {
            GalleryImageViewHolder(parent, viewModel)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (dataList.size > position) {
            runCatching {
                if (holder is GalleryImageViewHolder) {
                    holder.onBindView(dataList[position])
                } else if (holder is GalleryVideoViewHolder) {
                    holder.onBindView(dataList[position])
                }
            }
        }
    }

    /**
     * onBindView Payloads
     */
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.size == 0) {
            this.onBindViewHolder(holder, position)
        } else {
            if (payloads[0] is List<*>) {
                if (holder is GalleryImageViewHolder) {
                    holder.onBindView(payloads[0] as List<Any>)
                } else if (holder is GalleryVideoViewHolder) {
                    holder.onBindView(payloads[0] as List<Any>)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}