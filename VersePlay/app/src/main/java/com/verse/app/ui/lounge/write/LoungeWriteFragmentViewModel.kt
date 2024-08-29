package com.verse.app.ui.lounge.write

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.lounge.LoungeData
import com.verse.app.model.lounge.LoungeGalleryData
import com.verse.app.model.weblink.WebLinkData
import com.verse.app.ui.dialog.LoungeLinkDialog
import com.verse.app.ui.lounge.viewholders.LoungeGalleryViewHolder
import com.verse.app.usecase.PostLoungeUseCase
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.WebLinkProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 라운지 상세 > [작성]
 *
 * Created by juhongmin on 2023/05/18
 */
@HiltViewModel
class LoungeWriteFragmentViewModel @Inject constructor(
    private val fileProvider: FileProvider,
    private val webLinkProvider: WebLinkProvider,
    private val postLoungeUseCase: PostLoungeUseCase
) : FragmentViewModel(),
    GalleryBottomSheetDialog.Listener,
    LoungeGalleryViewHolder.Listener,
    LoungeLinkDialog.Listener {

    private val _startGalleryDialogEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startGalleryDialogEvent: LiveData<Int> get() = _startGalleryDialogEvent
    private val _startLinkUrlDialogEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startLinkUrlDialogEvent: LiveData<Unit> get() = _startLinkUrlDialogEvent

    private val _startFinishEvent: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Boolean> get() = _startFinishEvent

    private val _startInvalidLoungeEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startInvalidLoungeEvent: LiveData<Unit> get() = _startInvalidLoungeEvent

    // [s] Parameters
    val contentsText: MutableLiveData<String> by lazy { MutableLiveData() }
    private val _uploadImageList: ListLiveData<LoungeGalleryData> by lazy { ListLiveData() }
    val uploadImageList: ListLiveData<LoungeGalleryData> get() = _uploadImageList
    var currentImageCount: Int = 0

    val linkData: MutableLiveData<WebLinkData> by lazy { MutableLiveData() }

    var currentLinkData: String = ""

    val linkUrl: LiveData<String> get() = Transformations.map(linkData) { it.url }
    val linkImageUrl: LiveData<String> get() = Transformations.map(linkData) { it.imageUrl }
    val linkTitle: LiveData<String> get() = Transformations.map(linkData) { it.title }
    val linkIconUrl: LiveData<String> get() = Transformations.map(linkData) { it.iconUrl }

    val startCheckProhibit: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 금칙어 포함 여부 확인
    val startCheckPrivateAccount: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 비공개 계정 여부 확인

    val showInvalidUrlPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 유효하지 않은 URL

    val deleteLinkData: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // LinkData 삭제

    val moveToLinkUrl: SingleLiveEvent<String> by lazy { SingleLiveEvent() } // 링크 URL 이동 처리
    // [e] Parameters

    val contentsTextLength: LiveData<String>
        get() = Transformations.map(contentsText) {
            val realText = it.trim()
                .replace(" ", "")
                .replace("\n", "")
            "${realText.length}"
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
            code = null,
            contents = contentsText,
            imageList = uploadImageList.data(),
            linkUri = linkUri
        )
        postLoungeUseCase(data)
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                if(it.status == HttpStatusType.SUCCESS.status){
                    if (it.fgProhibitYn == AppData.Y_VALUE) {
                        startCheckProhibit.call()
                    } else {
                        startFinishEvent(true)
                    }
                    // 비공개계정 판단 여부
                }
            }, failure = {
                DLogger.d("ERROR $it")
                startFinishEvent(false)
            })
    }

    fun startFinishEvent(isSuccess: Boolean) {
        _startFinishEvent.value = isSuccess

        if (isSuccess) {
            RxBus.publish(RxBusEvent.LoungeRefreshEvent())
        }
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
                currentLinkData = url
            }, failure = {
                showInvalidUrlPopup.call()
            })
    }

    fun moveToLinkUrl(url: String) {
        moveToLinkUrl.value = url
    }
}
