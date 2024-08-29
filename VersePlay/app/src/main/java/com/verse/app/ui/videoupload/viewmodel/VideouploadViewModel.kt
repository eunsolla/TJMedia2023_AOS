package com.verse.app.ui.videoupload.viewmodel

import android.os.Environment
import androidx.core.os.EnvironmentCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.MediaType
import com.verse.app.contants.PartType
import com.verse.app.contants.ShowContentsType
import com.verse.app.contants.SingType
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.handleNetworkErrorRetry
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.encode.EncodeData
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.provider.SingPathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideouploadViewModel @Inject constructor(
    val singPathProvider: SingPathProvider,
    val loginManager: LoginManager,
    val apiService: ApiService,
    val resourceProvider: ResourceProvider,

    ) : ActivityViewModel(), GalleryBottomSheetDialog.Listener {


    //페이지 구분 앨범/피드 컨텐츠
    private val _pageType: MutableLiveData<VideoUploadPageType> by lazy { MutableLiveData() }
    val pageType: LiveData<VideoUploadPageType> get() = _pageType

    // 닫기
    val startFinishPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    //피드 컨텐츠 업로드 대상 정보
    private val _encodeData: MutableLiveData<EncodeData> by lazy { MutableLiveData() }
    val encodeData: LiveData<EncodeData> get() = _encodeData

    //앨범 컨텐츠 업로드 대상 정보
    private val _albumData: MutableLiveData<String> by lazy { MutableLiveData() }
    val albumData: LiveData<String> get() = _albumData

    //닉네임
    val nikName: NonNullLiveData<String> by lazy {
        NonNullLiveData("").apply {
            this.value = run {
                loginManager.getUserLoginData()?.let {
                    it.memNk
                } ?: run {
                    ""
                }
            }
        }
    }

    //서비스 call
    val startService: MutableLiveData<MutableList<EncodeData>> by lazy { MutableLiveData() }

    //댓글 허용 여부
    val postReply: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }

    //부르기 문구
    val songScoreTxt: NonNullLiveData<String> by lazy {
        NonNullLiveData(resourceProvider.getString(R.string.upload_completed_star_grade))
    }

    //나만
    val postOnlyme: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }

    //친구
    val postFriends: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }

    //전체
    val postEveryone: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }

    //업로드 뷰 show / hide
    val showUploadProgress: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }

    //업로드 완료
    val isUploadComplete: MutableLiveData<Boolean> by lazy { MutableLiveData(false) }

    //동영상 다시 선택 갤러리 이동
    val startGalleryDialog: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    //컨텐츠 내용 입력값
    val uploadMessage: NonNullLiveData<String> by lazy { NonNullLiveData("") }       //내용 입력값
    val hashTag: NonNullLiveData<String> by lazy { NonNullLiveData("") }                 //내용 입력값

    private val _checkEncoding: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }          //인코딩 서비스 체크
    val checkEncoding: LiveData<Unit> get() = _checkEncoding

    //썸네일
    private val _curThumbnailPath: MutableLiveData<String> by lazy { MutableLiveData() }
    val curThumbnailPath: LiveData<String> get() = _curThumbnailPath

    private val _message: MutableLiveData<Int> by lazy { MutableLiveData() }
    val message: LiveData<Int> get() = _message

    val startCheckProhibit: SingleLiveEvent<String> by lazy { SingleLiveEvent() } // 금칙어 포함 여부 확인

    fun start() {

        val pageType: VideoUploadPageType? = savedStateHandle[ExtraCode.UPLOAD_PAGE_TYPE]
        val encodeData: EncodeData? = savedStateHandle[ExtraCode.SING_ENCODE_ITEM]
        val albumData: String? = savedStateHandle[ExtraCode.ALBUM_SELECTED_ITEM]

        if (pageType == null || (pageType == VideoUploadPageType.ALBUM && albumData.isNullOrEmpty()) || (pageType == VideoUploadPageType.SING_CONTENTS && encodeData == null)) {
            onClose()
            return
        }
        pageType?.let {

            _pageType.value = it

            if (it == VideoUploadPageType.ALBUM) {
                //앨범은 한건.
                albumData?.let { it ->
                    setAlbumData(albumData)
                }
            } else {
                encodeData?.let {

                    _encodeData.value = it

                    if (it.isOff) {
                        setUploadResult(true)
                        return
                    }
                    val imgSong = if (it.mediaType == MediaType.AUDIO.code) {
                        it.songMainData?.let { songMainData ->
                            songMainData.albImgPath
                        } ?: run { "" }
                    } else {
                        it.videoMp4Path
                    }
                    setThumbnail(imgSong)
                }
            }
        } ?: run {
            onClose()
        }
    }

    /**
     * 현재 이미지 썸네일
     */
    private fun setThumbnail(path: String) {
        _curThumbnailPath.value = path
    }

    /**
     * 다시선택
     */
    fun startGalleryDialog() {
        startGalleryDialog.call()
    }

    fun checkProhibitWord(contents: String) {
        apiService.checkProhibitWord(contents)
            .compose(handleNetworkErrorRetry())
            .applyApiScheduler()
            .request({ res ->
                DLogger.d("checkProhibitWord=> ${res}")

                if (res.status == HttpStatusType.SUCCESS.status && res.fgProhibitYn == AppData.Y_VALUE) {
                    startCheckProhibit.value = contents
                } else {
                    checkSongEncodeService()
                }
            }, { error ->
                DLogger.d("Throwable ${error}")
            })
    }

    fun checkSongEncodeService() {
        _checkEncoding.call()
    }

    /**
     * 피드업로드
     */
    fun startUploadFiles() {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        //메세지 내용
        if (uploadMessage.value.isNullOrEmpty()) {
            showToastIntMsg.value = R.string.post_text_empty
            return
        }

        //콘텐츠 노출 여부
        if (getShowContentsType() == ShowContentsType.NONE.code) {
            showToastIntMsg.value = R.string.post_show_contents_need_select
            return
        }

        DLogger.d("upload message=> ${uploadMessage.value} / ${hashTag.value}")

        //앨범
        if (pageType.value == VideoUploadPageType.ALBUM) {


            val dataList = mutableListOf<EncodeData>()

            _albumData.value?.let { orgPath ->

                if (singPathProvider.initAlbumVideoPath(orgPath)) {

                    if (!singPathProvider.getResultVideoAlbumPath().isNullOrEmpty()) {

                        val encodeData = EncodeData().apply {
                            singType = SingType.NORMAL.code
                            mediaType = MediaType.VIDEO.code
                            partType = PartType.PART_A.code
                            feedDesc = uploadMessage.value
                            feedTag = getTags(hashTag.value)
                            showContentsType = getShowContentsType()
                            useReply = getReply()
                            encodeThumbPath = singPathProvider.getEncodeThumbPath()
                            encodeOriginVideoWebmPath = singPathProvider.getEncodeOriginVideoPath()
                            encodeHighlightPath = singPathProvider.getEncodeHighlightPath()
                            albumDirPath = singPathProvider.getPrefixAlbumInfo()
                            uploadType = singPathProvider.getPageType()
                            encodeDirPath = singPathProvider.getDirEncodePath()
                            videoMp4Path = singPathProvider.getResultVideoAlbumPath()
                            songName = orgPath
                            waterMarkPath = singPathProvider.getWaterMarkPath()
                        }

                        _encodeData.value = encodeData

                        dataList.add(encodeData)
                    } else {
                        deleteDir(singPathProvider.getPrefixAlbumInfo())
                        DLogger.d("Album video is null")
                    }
                } else {
                    deleteDir(singPathProvider.getPrefixAlbumInfo())
                    DLogger.d("Album video is null")
                }

                if (!dataList.isNullOrEmpty() && dataList.size > 0) {
                    startService.value = dataList
                    showUploadProgress(true)
                } else {
                    deleteDir(singPathProvider.getPrefixAlbumInfo())
                    DLogger.d("Album video is null")
                }
            } ?: run {
                deleteDir(singPathProvider.getPrefixAlbumInfo())
            }

        } else {
            //업로드 데이터
            encodeData.value?.let {
                it.feedDesc = uploadMessage.value
                it.feedTag = getTags(hashTag.value)
                it.showContentsType = getShowContentsType()
                it.useReply = getReply()
                val dataList = mutableListOf<EncodeData>()
                dataList.add(it)
                startService.value = dataList
                showUploadProgress(true)
            } ?: run {
                showToastIntMsg.value = R.string.encode_fail
            }
        }
    }

    fun deleteDir(path: String) {
        showToastIntMsg.value = R.string.encode_fail
        if (path.isNotEmpty()) {
            singPathProvider.deleteUploadDir(path)
        }
    }

    /**
     * 업로드 Progress
     */
    fun showUploadProgress(state: Boolean) {
        showUploadProgress.postValue(state)
    }

    fun setUploadResult(state: Boolean) {
        isUploadComplete.postValue(state)
    }

    /**
     * 댓글
     */
    fun onClickReply() {
        postReply.value = !postReply.value
    }

    /**
     * 콘텐츠 노출 설정
     */
    fun onClickContents(type: ShowContentsType) {

        when (type) {
            //나만
            ShowContentsType.PRIVATE -> {
                postOnlyme.value = !postOnlyme.value
                postFriends.value = false
                postEveryone.value = false
            }
            //친구
            ShowContentsType.ALLOW_FRIENDS -> {
                postFriends.value = !postFriends.value
                postOnlyme.value = false
                postEveryone.value = false
            }
            //전체
            ShowContentsType.ALLOW_ALL -> {
                postEveryone.value = !postEveryone.value
                //전체 ON 이면 친구 ON 시켜줌
                if (postEveryone.value) {
                    postFriends.value = postEveryone.value
                }
                postOnlyme.value = false
            }

            else -> {}
        }
    }

    /**
     * 닫기
     */
    fun onClose() {
        startFinish.call()
    }

    fun onCancelUpload(){
        startFinishPopup.call()
    }

    /**
     * 공개 / 비공개 여부
     */
    private fun getShowContentsType(): String {

        if (postEveryone.value) {
            return ShowContentsType.ALLOW_ALL.code
        }
        if (postFriends.value) {
            return ShowContentsType.ALLOW_FRIENDS.code
        }
        if (postOnlyme.value) {
            return ShowContentsType.PRIVATE.code
        }

        return ShowContentsType.NONE.code
    }

    /**
     *  댓글 허용 여부
     */
    private fun getReply(): String {
        return if (postReply.value) AppData.Y_VALUE else AppData.N_VALUE
    }

    /**
     * get Tag
     */
    private fun getTags(text: String): String {

        val regex = "(#[ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9-_]+)(?:#[ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9-_]+)*".toRegex()

        val matchResult = regex.findAll(text)

        var hashTags = ""

        matchResult.forEach {
            hashTags = it.value
        }

        return hashTags.trim()
    }

    /**
     * 앨범 데이터 Set
     */
    private fun setAlbumData(path: String) {
        if (path.isEmpty()) return
        DLogger.d("setAlbumData setAlbumData -> ${path}")
        _albumData.value = path
        setThumbnail(path)
    }

    override fun onGalleryConfirm(imageList: List<String>) {
        if (imageList.isEmpty()) return
        setAlbumData(imageList[0])
    }
    override fun onGalleryDismiss() {}

}

