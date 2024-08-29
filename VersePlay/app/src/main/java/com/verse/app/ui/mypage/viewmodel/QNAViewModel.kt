package com.verse.app.ui.mypage.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.*
import com.verse.app.extension.applyApiScheduler
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.gallery.ui.GalleryImageEditBottomSheetDialog
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.common.GetResourcePathInfo
import com.verse.app.model.common.InquiryTypeData
import com.verse.app.model.common.SelectBoxData
import com.verse.app.model.mypage.GetInquiryCategoryListData
import com.verse.app.model.mypage.GetInquiryListData
import com.verse.app.model.param.InquiryBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class QNAViewModel @Inject constructor(
    val apiService: ApiService,
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val fileProvider: FileProvider
) : ActivityViewModel(), GalleryBottomSheetDialog.Listener,
    GalleryImageEditBottomSheetDialog.Listener {
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 뒤로가기

    var fgProhibitYn: Boolean = false

    // 나의 1:1 문의 내역 조회
    private val _myQNAList: ListLiveData<GetInquiryListData> by lazy { ListLiveData() }
    val myQNAList: ListLiveData<GetInquiryListData> get() = _myQNAList
    val myQnaContents: NonNullLiveData<String> by lazy { NonNullLiveData("") }       // 문의 카테고리

    private val _qnaCategory: ListLiveData<GetInquiryCategoryListData> by lazy { ListLiveData() }
    val qnaCategory: ListLiveData<GetInquiryCategoryListData> get() = _qnaCategory

    // qna 타이틀
    private val _qnaTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val qnaTitle: MutableLiveData<String> get() = _qnaTitle

    private val _attUri: MutableLiveData<String> by lazy { MutableLiveData() }
    val attUri: MutableLiveData<String> get() = _attUri

    private val _qnaCode: MutableLiveData<String> by lazy { MutableLiveData() }
    val qnaCode: MutableLiveData<String> get() = _qnaCode

    var selectBoxDataArrayList: ArrayList<SelectBoxData> = ArrayList()
    val editQNAText: NonNullLiveData<String> by lazy { NonNullLiveData("") }       // 내용
    val myEmail: NonNullLiveData<String> by lazy { NonNullLiveData("") }       // 이메일

    val showSelectBox: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val delete: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val callGalleryDialog: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val reqCompletedInquiryInfo: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    val startEditAttachImage: SingleLiveEvent<Uri> by lazy { SingleLiveEvent() }      // Edit startEditAttachImage

    lateinit var inquiryBody: InquiryBody

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * 1:1 문의 내역 조회
     * 추후 개발 예정인 api
     */
    fun requestMyQnAData() {
        apiService.getInquiryList(
            pageNum = 1,
            sortType = SortType.DESC.name
        ).applyApiScheduler()
            .doLoading()
            .request({ res ->
                DLogger.d("Success requestMyQnAData -> ${res}")
                _myQNAList.value = res.result.dataList
            }, {
                DLogger.d("Error Report-> ${it.message}")
            })
    }

    fun onComplete() {
        if (_attUri.value != null) {
            uploadAttImg(_attUri.value!!)
        } else {
            inquiryBody = InquiryBody()
            requestInquiryInfo()
        }
    }

    /**
     *  의견보내기 -> 카테고리 리스트
     */
    fun setCategoryDataFromServer() {
        apiService.getInquiryCategoryList()
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                _qnaCategory.value = res.result.dataList

                for (i in 0 until res.result.dataList.size) {
                    val inquiryTypeData = InquiryTypeData()
                    inquiryTypeData.contents = res.result.dataList[i].bctgMngNm
                    inquiryTypeData.bctgMngCd = res.result.dataList[i].bctgMngCd
                    selectBoxDataArrayList.add(inquiryTypeData)
                }

            }, {
                DLogger.d("Error setCategoryDataFromServer -> ${it.message}")
            })
    }

    fun showSelectBox() {
        showSelectBox.call()
    }

    fun delete() {
        delete.call()
    }

    /**
     * 앨범에서 선택
     */
    fun callAlbum() {
        callGalleryDialog.call()
    }

    fun requestInquiryInfo() {

        val versionName = deviceProvider.getVersionName()
        val androidVersion = deviceProvider.getAndroidVersion()

        apiService.requestInquiryInfo(
            InquiryBody(
                attImagePath = inquiryBody.attImagePath,
                bctgMngCd = qnaCode.value.toString(),
                csContent = editQNAText.value,
                csMngCd = inquiryBody.csMngCd,
                recEmail = myEmail.value,
                osVersion = androidVersion,
                appVersion = versionName,
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    if (res.fgProhibitYn == AppData.Y_VALUE) {
                        fgProhibitYn = true
                    }

                    reqCompletedInquiryInfo.call()
                }
            })

    }

    override fun onGalleryConfirm(imageList: List<String>) {
        DLogger.d("SUCC Gallery $imageList")
        val str = imageList[0]
        val uri = Uri.parse(str)

        startEditAttachImage.value = uri
    }
    override fun onGalleryDismiss() {}

    /**
     * 사진 경로 가져오는 api
     */
    fun uploadAttImg(realUri: String) {

        apiService.getResourcePath(
            resType = ResourcePathType.QNA.code
        )
            .map {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    inquiryBody = InquiryBody(
                        attImagePath = it.result.attImgPath,
                        csMngCd = it.result.csMngCd,
                    )

                    it.result
                } else {
                    throw IllegalArgumentException("IllegalArgumentException")
                }
            }
            .flatMap { handleProfile(realUri, it) }
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                DLogger.d("uploadAttImg 업로드 완료 이미지 경로 => ${it.first} / ${it.second}")
                requestInquiryInfo()
            }, failure = {
                DLogger.d("fail uploadAttImg ${it}")
            })

    }

    private fun handleProfile(
        realUri: String,
        data: GetResourcePathInfo
    ): Single<Pair<String, String>> {
        val uploadPath = data.attImgPath
        val imageUri = Uri.parse(Config.BASE_FILE_URL.plus(uploadPath))
        return fileProvider.requestCameraPictureUpload(realUri, uploadPath)
            .map { Config.BASE_FILE_URL.plus(imageUri.path) to it }
    }

    override fun onEditConfirm(imagePath: String) {
        _attUri.value = imagePath
    }
}