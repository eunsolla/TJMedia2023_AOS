package com.verse.app.ui.lounge.modify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.lounge.LoungeData
import com.verse.app.model.lounge.LoungeDetailData
import com.verse.app.model.lounge.LoungeGalleryData
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.model.weblink.WebLinkData
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.dialog.LoungeLinkDialog
import com.verse.app.ui.lounge.viewholders.LoungeGalleryViewHolder
import com.verse.app.usecase.PostLoungeUseCase
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.WebLinkProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 라운지 상세 > [수정]
 *
 * Created by juhongmin on 2023/05/18
 */
@HiltViewModel
class LoungeModifyFragmentViewModel @Inject constructor(
    private val apiService: ApiService,
    private val webLinkProvider: WebLinkProvider,
    private val postLoungeUseCase: PostLoungeUseCase
) : FragmentViewModel(),
    GalleryBottomSheetDialog.Listener,
    LoungeGalleryViewHolder.Listener,
    LoungeLinkDialog.Listener {

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    private val _startModifyCompletedEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startModifyCompletedEvent: LiveData<Unit> get() = _startModifyCompletedEvent

    private val _startInvalidLoungeEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startInvalidLoungeEvent: LiveData<Unit> get() = _startInvalidLoungeEvent

    private val _startGalleryDialogEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startGalleryDialogEvent: LiveData<Int> get() = _startGalleryDialogEvent
    private val _startLinkUrlDialogEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startLinkUrlDialogEvent: LiveData<Unit> get() = _startLinkUrlDialogEvent

    val showInvalidUrlPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 유효하지 않은 URL

    val deleteLinkData: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // LinkData 삭제

    val moveToLinkUrl: SingleLiveEvent<String> by lazy { SingleLiveEvent() } // 링크 URL 이동 처리

    // [s] Parameters
    private var loungeCode: String = ""
    private val originalContentsText: MutableLiveData<String> by lazy { MutableLiveData() }
    val contentsText: MutableLiveData<String> by lazy { MutableLiveData() }
    val contentsTextLength: LiveData<String>
        get() = Transformations.map(contentsText) {
            val realText = it.trim()
                .replace(" ", "")
                .replace("\n", "")
            "${realText.length}"
        }
    private val _uploadImageList: ListLiveData<LoungeGalleryData> by lazy { ListLiveData() }
    val uploadImageList: ListLiveData<LoungeGalleryData> get() = _uploadImageList
    private var currentImageCount: Int = 0
    private val linkData: MutableLiveData<WebLinkData> by lazy { MutableLiveData() }
    val linkUrl: LiveData<String> get() = Transformations.map(linkData) { it.url }
    val linkImageUrl: LiveData<String> get() = Transformations.map(linkData) { it.imageUrl }
    val linkTitle: LiveData<String> get() = Transformations.map(linkData) { it.title }
    val linkIconUrl: LiveData<String> get() = Transformations.map(linkData) { it.iconUrl }
    // [e] Parameters

    val startCheckProhibit: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 금칙어 포함 여부 확인

    fun start() {
        val data: LoungeIntentModel = savedStateHandle[ExtraCode.WRITE_LOUNGE_DATA]!!
        val info = data.modifyInfo ?: return
        loungeCode = info.code
        apiService.fetchLoungeDetail(info.code)
            .map { it.result }
            .doLoading()
            .applyApiScheduler()
            .request(success = { handleInitSuccess(it) }, failure = {
                DLogger.d("ERROR $it")
            })
    }

    private fun handleInitSuccess(data: LoungeDetailData) {
        contentsText.value = data.contents
        originalContentsText.value = contentsText.value
        data.getImageList().forEachIndexed { index, s ->
            _uploadImageList.add(
                LoungeGalleryData(
                    index.toLong(),
                    imagePath = s
                )
            )
        }
        currentImageCount = uploadImageList.size

        if (!data.linkUrl.isNullOrEmpty()) {
            requestWebLink(data.linkUrl)
        }
        onLoadingDismiss()
    }

    private fun requestWebLink(url: String) {
        webLinkProvider.getLinkMeta(url)
            .applyApiScheduler()
            .request(success = {
                linkData.value = it
            })
    }

    fun showGalleryDialog() {
        _startGalleryDialogEvent.value = 10 - currentImageCount
    }

    fun showLinkDialog() {
        _startLinkUrlDialogEvent.call()
    }

    fun deleteLinkUrl() {
        linkData.value = WebLinkData(null, null, null, null, null)
        deleteLinkData.call()
    }

    fun onConfirm() {
        val contentsText = contentsText.value?.trim() ?: ""
        if (contentsText.isEmpty()) {
            _startInvalidLoungeEvent.call()
            return
        }
        val webLinkData = linkData.value
        val linkUri = if (webLinkData != null) {
            if (webLinkData.fullUrl.isNullOrEmpty()) webLinkData.url else webLinkData.fullUrl
        } else {
            null
        }

        val data = LoungeData(
            code = loungeCode,
            contents = contentsText,
            imageList = uploadImageList.data(),
            linkUri = linkUri
        )
        postLoungeUseCase(data)
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                if (it.fgProhibitYn == AppData.Y_VALUE) {
                    startCheckProhibit.call()
                } else {
                    startModifyCompletedEvent()
                }
            }, failure = {
                DLogger.d("ERROR $it")
                _startFinishEvent.call()
            })
    }

    fun startModifyCompletedEvent() {
        _startModifyCompletedEvent.call()
        RxBus.publish(RxBusEvent.LoungeRefreshEvent())
    }

    override fun onGalleryConfirm(imageList: List<String>) {
        DLogger.d("SUCC Gallery $imageList")
        val startUid = System.currentTimeMillis()
        imageList.forEachIndexed { index, s ->
            _uploadImageList.add(LoungeGalleryData(startUid.plus(index), s))
        }

        currentImageCount = uploadImageList.size
    }
    override fun onGalleryDismiss() {}

    override fun onGalleryRemove(data: LoungeGalleryData) {
        DLogger.d("onGalleryRemove $data")
        _uploadImageList.remove(data)

        currentImageCount = uploadImageList.size
    }

    override fun onLoungeLinkConfirm(url: String) {
        DLogger.d("onLoungeLinkConfirm $url")
        webLinkProvider.getLinkMeta(url)
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                linkData.value = it
            })
    }

    fun onFinish() {
        _startFinishEvent.call()
    }

    fun isModifyContents() : Boolean{
        return originalContentsText.value?.trim() == contentsText.value?.trim()
    }

    fun moveToLinkUrl(url: String) {
        moveToLinkUrl.value = url
    }
}
