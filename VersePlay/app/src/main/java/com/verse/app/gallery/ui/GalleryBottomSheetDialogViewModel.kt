package com.verse.app.gallery.ui

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.lifecycle.LiveData
import com.verse.app.R
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.extension.applyApiScheduler
import com.verse.app.gallery.core.GalleryProvider
import com.verse.app.gallery.core.GalleryQueryParameter
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * Description : Gallery BottomSheet ViewModel
 *
 * Created by juhongmin on 2023/05/13
 */
@HiltViewModel
class GalleryBottomSheetDialogViewModel @Inject constructor(
    private val galleryProvider: GalleryProvider,
    private val resProvider: ResourceProvider
) : FragmentViewModel(),
    GalleryAdapter.GalleryListener {

    companion object {
        const val MAX_COUNT = "a"
        const val MAX_DURATION = "e"
        const val IS_VIDEO_TYPE = "b"
        const val CONFIRM_INVALID = "c"
        const val MAX_SELECTED = "d"
    }

    private val _startCloseEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startCloseEvent: LiveData<Unit> get() = _startCloseEvent
    private val _startConfirmEvent: SingleLiveEvent<List<String>> by lazy { SingleLiveEvent() }
    val startConfirmEvent: LiveData<List<String>> get() = _startConfirmEvent
    private val _startShowMaxDialog: SingleLiveEvent<CharSequence> by lazy { SingleLiveEvent() }
    val startShowMaxDialog: SingleLiveEvent<CharSequence> get() = _startShowMaxDialog

    private val _startConfirmInvalidEvent: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val startConfirmInvalidEvent: LiveData<String> get() = _startConfirmInvalidEvent

    private val _dataList: ListLiveData<GalleryItem> by lazy { ListLiveData() }
    val dataList: ListLiveData<GalleryItem> get() = _dataList

    // [s] Parameters
    val maxCount: Int by lazy { savedStateHandle.get<Int>(MAX_COUNT) ?: 10 }
    val maxDuration: Long by lazy { savedStateHandle.get<Long>(MAX_DURATION) ?: 90000 }
    val pageModel: PagingModel by lazy { PagingModel() }
    private var cursor: Cursor? = null
    private var dataCount: Int = -1
    private var prevImagePath: String = ""
    private val queryParameter: GalleryQueryParameter by lazy { GalleryQueryParameter() }
    private val photoPickerMap: MutableMap<String, GalleryItem> by lazy { mutableMapOf() }
    private var fmtMaxConfirm: String = ""
    private var strConfirmInvalid: String = ""
    private var fmtMaxDuration: String = ""
    // [e]] Parameters

    val isSelectedCheck: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() } // 선택 시 숫자인지, 체크인지 판단값 체크일경우 true
    val isGalleryNull: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 갤러리에 하나도 없을경우

    fun start() {
        val isVideoType = getIntentData(IS_VIDEO_TYPE, false)
        queryParameter.isVideoType = isVideoType
        if (isVideoType) {
            queryParameter.addColumns(MediaStore.Video.Media.DURATION)
        }
        fmtMaxConfirm = getIntentData<String>(MAX_SELECTED)
            ?: resProvider.getString(R.string.gallery_max_count_message)
        strConfirmInvalid = getIntentData<String>(CONFIRM_INVALID)
            ?: resProvider.getString(R.string.gallery_invalid_confirm_message)
        fmtMaxDuration = getIntentData<String>(MAX_DURATION)
            ?: resProvider.getString(R.string.video_upload_max_duration)

        galleryProvider.fetchGallery(queryParameter)
            .map { setCursor(it) }
            .flatMap { reqPhotoList(it) }
            .applyApiScheduler()
            .doOnSuccess {
                if (it.isNotEmpty()){
                    _dataList.addAll(it)
                } else {
                    isGalleryNull.call()
                }
            }
            .doFinally { handleCursorState() }
            .subscribe().addTo(compositeDisposable)
    }

    fun onLoadPage() {
        reqPhotoList(cursor)
            .applyApiScheduler()
            .doOnSuccess { _dataList.addAll(it) }
            .doFinally { handleCursorState() }
            .subscribe().addTo(compositeDisposable)
    }

    fun onClose() {
        _startCloseEvent.call()
    }

    fun onConfirm() {
        if (photoPickerMap.isEmpty()) {
            _startConfirmInvalidEvent.value = strConfirmInvalid
        } else {
            _startConfirmEvent.value = photoPickerMap.map { it.value.imagePath }.toList()
        }
    }

    private fun reqPhotoList(cursor: Cursor?): Single<List<GalleryItem>> {
        return if (cursor != null) {
            Single.just(cursor)
                .doOnSubscribe { pageModel.isLoading = true }
                .map { it.toUiModels() }
        } else {
            handleCursorState()
            Single.just(emptyList())
        }
    }

    private fun setCursor(cursor: Cursor): Cursor {
        this.cursor = cursor
        dataCount = cursor.count
        return cursor
    }

    /**
     * Cursor to GalleryUiMoel
     */
    private fun Cursor?.toUiModels(pageSize: Int = 100): List<GalleryItem> {
        val list = mutableListOf<GalleryItem>()
        if (this == null) {
            pageModel.isLoading = false
            pageModel.isLast = true
            return list
        }

        for (idx in 0 until pageSize) {
            if (moveToNext()) {
                runCatching {
                    if (queryParameter.isVideoType) {
                        handleVideoUiModel(this, list)
                    } else {
                        handleImageUiModel(this, list)
                    }
                }
            } else {
                pageModel.isLoading = false
                pageModel.isLast = true
                break
            }
        }
        return list
    }

    /**
     * 갤러리 이미지 UiModel 처리하는 함수
     */
    private fun handleImageUiModel(cursor: Cursor, list: MutableList<GalleryItem>) {
        val mediaId = try {
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        } catch (ex: IllegalArgumentException) {
            0
        }
        val contentId = cursor.getLong(mediaId)
        val uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentId.toString()
        )

        // 기존에 선택한 아이템이라면 주소값 넘김
        val galleryUiModel = photoPickerMap[uri.toString()]
        if (galleryUiModel != null) {
            list.add(galleryUiModel)
        } else {
            list.add(GalleryItem.Image(uri.toString()))
        }
    }

    /**
     * 갤러리 비디오 UiModel 처리하는 함수
     */
    private fun handleVideoUiModel(cursor: Cursor, list: MutableList<GalleryItem>) {
        val mediaId = try {
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        } catch (ex: IllegalArgumentException) {
            0
        }
        val contentId = cursor.getLong(mediaId)
        val uri = Uri.withAppendedPath(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            contentId.toString()
        )
        val durationId = try {
            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        } catch (ex: IllegalArgumentException) {
            0
        }
        val duration = cursor.getLongOrNull(durationId) ?: 0L
        DLogger.d("VideoDuration $duration")

        // 기존에 선택한 아이템이라면 주소값 넘김
        val galleryUiModel = photoPickerMap[uri.toString()]
        if (galleryUiModel != null) {
            list.add(galleryUiModel)
        } else {
            list.add(
                GalleryItem.Video(
                    imagePath = uri.toString(),
                    duration = duration
                )
            )
        }
    }


    /**
     * 작업 완료후 이후 페이징 처리 유무 처리 하는 함수
     */
    private fun handleCursorState() {
        if (dataCount == dataList.size) {
            pageModel.isLoading = false
            pageModel.isLast = true
            cursor = null
        } else {
            pageModel.isLoading = false
        }
    }

    /**
     * 사진 선택하기전 이미 선택된 데이터들 SelectNum 셋팅 하는 함수
     */
    private fun handleSortPhotoPicker(isAdd: Boolean, model: GalleryItem) {
        if (isAdd) {
            model.selectedNum = "${photoPickerMap.size.plus(1)}"
            photoPickerMap[model.imagePath] = model
            prevImagePath = model.imagePath
        } else {
            photoPickerMap.remove(model.imagePath)
            var idx = 1
            photoPickerMap.forEach { entry ->
                entry.value.selectedNum = idx.toString()
                idx++
            }
            prevImagePath = ""
        }
    }

    override fun onPhotoPicker(isAdd: Boolean, item: GalleryItem) {
        handleSortPhotoPicker(isAdd, item)
    }

    override fun checkMaxPickerCount(): Boolean {
        val result = photoPickerMap.size >= maxCount
        if (result) {
            _startShowMaxDialog.value = String.format(fmtMaxConfirm, maxCount)
        }
        return result
    }

    override fun checkMaxVideoDuration(duration: Long): Boolean {
        val result = duration > maxDuration
        if (result) {
            val times = getTime(maxDuration)
            _startShowMaxDialog.value = String.format(fmtMaxDuration, times.first, times.second)
        }
        return result
    }

    override fun isCurrentPhoto(item: GalleryItem): Boolean {
        return prevImagePath == item.imagePath
    }

    override fun getGalleryPhotoPicker(): List<GalleryItem> {
        return photoPickerMap.map { it.value }
    }

    private fun getTime(ms: Long): Pair<Long, Long> {
        val seconds = ms / 1000
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return mn to sec
    }
}
