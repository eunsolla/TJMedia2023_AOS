package com.verse.app.utility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.contants.Encoded
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.MediaType
import com.verse.app.contants.ProgressVideoType
import com.verse.app.contants.ResourcePathType
import com.verse.app.contants.SingType
import com.verse.app.contants.SingingType
import com.verse.app.contants.UploadProgressAudio
import com.verse.app.contants.UploadProgressVideo
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.onIO
import com.verse.app.extension.onMain
import com.verse.app.extension.onWithContextMain
import com.verse.app.extension.onWithIO
import com.verse.app.extension.parcelableArrayList
import com.verse.app.model.common.GetResourcePathInfo
import com.verse.app.model.encode.EncodeData
import com.verse.app.model.ffmpeg.EncodeDelegateState
import com.verse.app.model.mypage.SubscData
import com.verse.app.model.videoupload.UploadFeedBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.main.MainActivity
import com.verse.app.utility.ffmpegkit.Common
import com.verse.app.utility.ffmpegkit.FFmpegProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SongEncodeService : Service() {

    companion object {
        const val NOTIFICATION_UPLOAD_ID = 1
        const val NOTIFICATION_COMPLETE_ID = 2
        const val TAG = "SongEncodeService"
        const val CHANNEL_ID = "VERSE PLAY"
        const val CHANNEL_NAME = "Upload Notification"
        const val CHANNEL_DESCRIPTION = "VERSE PLAY Upload Service"
    }

    val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    /**
     * 파일 업로드용 Enum Class
     */
    enum class UploadType {
        AUDIO, // 단순 오디오
        VIDEO // 영상 업로드
    }

    @Inject
    lateinit var ffmpegProvider: FFmpegProvider

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var accountPref: AccountPref

    @Inject
    lateinit var fileProvider: FileProvider

    @Inject
    lateinit var loginManager: LoginManager


    private val coroutineExceptionHandler = CoroutineExceptionHandler { ctx, throwable ->
        DLogger.d(
            TAG, "Caught exception: $throwable ${throwable.message} / ${throwable.printStackTrace()}"
        )
        stopService()
    }

    lateinit var job: CoroutineContext

    private val notificationManager get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val encodeDataList by lazy { mutableListOf<EncodeData>() }

    override fun onCreate() {
        registerDefaultNotificationChannel()
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet!")
    }

    /**
     * Start Service
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DLogger.d(TAG, "Service onStartCommand")
        DLogger.d("intent${intent}")

        if (intent?.action == Encoded.SONG_ENCODE_START_SERVICE) {

            intent.parcelableArrayList<EncodeData>(ExtraCode.SING_ENCODE_ITEM)?.let {
                DLogger.d("@@ ExtraCode.SING_ENCODE_ITEM ::: ${it}")
                encodeDataList.addAll(it)
                prepareEncodeSongData()
            } ?: run {
                DLogger.d(TAG, "Service onStartCommand SONG_ENCODE_START_SERVICE is null data")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 준비
     */
    private fun prepareEncodeSongData() {

        // 업로드 / 재업로드 일 경우 리턴
        if (AppData.IS_ENCODE_ING || AppData.IS_REUPLOAD_ING) {
            DLogger.d(TAG, "@@AppData.IS_ENCODE_ING 리턴")
            return
        }

        DLogger.d(TAG, "Service onStartCommand encodeSongData")

        job = Job() + coroutineExceptionHandler

        CoroutineScope(Dispatchers.IO + job).launch {

            val encodeData = async { getEncodeData() }

            encodeData.await()?.let {

                AppData.IS_ENCODE_ING = true

                showNotification(it)
                DLogger.d(TAG, "showNotification(it)${it}")

                //인코딩 시작
                if (it.uploadType == VideoUploadPageType.SING_CONTENTS) {
                    DLogger.d(TAG, "runEncoding(it)${it}")
                    runEncoding(it)

                } else {
                    DLogger.d(TAG, "runAlbumEncode(it)${it}")
                    runAlbumEncode(it)
                }

            } ?: run {
                DLogger.d(TAG, "prepareEncodeSongData Finish Service ")
                stopService()
            }

            DLogger.d(TAG, "!!!! encodeSongData => ${encodeDataList.size} ")
        }

    }

    /**
     * 인코딩 시작
     */
    suspend fun runEncoding(encodeData: EncodeData) {

        when (encodeData.singType) {
            SingType.SOLO.code -> {
                when (encodeData.mediaType) {
                    MediaType.AUDIO.code -> {
                        DLogger.d(TAG, "runEncode SOLO 타입 AUDIO")
                        encodeSoloAudio(encodeData)
                    }

                    MediaType.VIDEO.code -> {
                        encodeSoloVideo(encodeData)
                        DLogger.d(TAG, "runEncode SOLO 타입 VIDEO")
                    }
                }
            }

            SingType.DUET.code -> {
                when (encodeData.mediaType) {
                    MediaType.AUDIO.code -> {
                        DLogger.d(TAG, "runEncode DUET 타입 AUDIO")
                        encodeDuetAudio(encodeData)
                    }

                    MediaType.VIDEO.code -> {
                        DLogger.d(TAG, "runEncode DUET 타입 VIDEO")
                        encodeDuetBattleVideo(encodeData)
                    }
                }
            }

            SingType.BATTLE.code -> {
                when (encodeData.mediaType) {
                    MediaType.AUDIO.code -> {
                        DLogger.d(TAG, "runEncode BATTLE 타입 AUDIO")
                    }

                    MediaType.VIDEO.code -> {
                        encodeDuetBattleVideo(encodeData)
                        DLogger.d(TAG, "runEncode BATTLE 타입 VIDEO")
                    }
                }
            }
        }
    }

    /**
     *  앨범 영상 인코딩 시작
     */
    private suspend fun runAlbumEncode(encodeData: EncodeData) {
        DLogger.d(TAG, "runAlbumEncode ${encodeData}")
        encodeSoloVideo(encodeData)
    }

    /**
     * 솔로 오디오
     */
    private suspend fun encodeSoloAudio(data: EncodeData) {
        try {
            // Upload Progress Start
            val busData = UploadProgressAudio(0.0F)
            RxBus.publish(busData)
            //풀영상
            val originVideoWebm = onWithIO { ffmpegProvider.runOriginVideoWebm(data) }
            //프리뷰
            val highlight = onWithIO { ffmpegProvider.runHighlight(data) }

            //결과
            val result = originVideoWebm && highlight
            DLogger.d(TAG, "encodeSoloAudio  => origin:$originVideoWebm / highlight:${highlight}")
            handleServiceByEncodeState(result, data)
        } catch (e: Exception) {
            handleServiceByEncodeState(false, data)
            DLogger.d(TAG, "encodeSoloAudio Exception =>${e}")
        }
    }

    /**
     * 솔로 비디오
     */
    private suspend fun encodeSoloVideo(data: EncodeData) {
        try {
            // Upload Progress Start
            val busData = UploadProgressVideo(
                type = ProgressVideoType.ORIGIN, progress = 0.0F
            )
            RxBus.publish(busData)
            //풀영상
            val originVideoWebm = onWithIO {
                ffmpegProvider.runOriginVideoWebm(data) {
                    publishOriginProgress(it)
                }
            }

            //프리뷰
            val highlight = onWithIO {
                ffmpegProvider.runHighlight(data) {
                    publishHighlightProgress(it)
                }
            }

            //썸네일
            val thumbnail = onWithIO { ffmpegProvider.runThumbnail(data) }

            //결과
            val result = originVideoWebm && highlight && thumbnail

            DLogger.d(TAG, "encodeSoloVideo  =>origin :${originVideoWebm} / highlight :${highlight} /  thumbnail :${thumbnail} / ")
            handleServiceByEncodeState(result, data)
        } catch (e: Exception) {
            handleServiceByEncodeState(false, data)
            DLogger.d(TAG, "encodeSoloVideo Exception =>${e}")
        }
    }

    /**
     * 듀엣 오디오
     */
    private suspend fun encodeDuetAudio(data: EncodeData) {
        try {
            // Upload Progress Start
            val busData = UploadProgressAudio(0.0F)
            RxBus.publish(busData)

            //풀영상
            val originVideoWebm = onWithIO { ffmpegProvider.runOriginVideoWebm(data) }
            //프리뷰
            val highlight = onWithIO { ffmpegProvider.runHighlight(data) }

            val result = if (data.isInitContents) {
                //최초생성
                //오디오
                val originAudio = onWithIO { ffmpegProvider.runOriginAudioWav(data) }
                //결과
                originVideoWebm && highlight && originAudio
            } else {
                //참여 인경우
                originVideoWebm && highlight
            }

            DLogger.d(TAG, "encodeDuetAudio  =>origin :${originVideoWebm} / highlight :${highlight} /  result :${result} / ")
            handleServiceByEncodeState(result, data)

        } catch (e: Exception) {
            handleServiceByEncodeState(false, data)
            DLogger.d(TAG, "encodeDuetAudio Exception =>${e}")
        }
    }

    /**
     * 듀엣 배틀 비디오
     */
    private suspend fun encodeDuetBattleVideo(data: EncodeData) {
        try {
            // Upload Progress Start
            val busData = UploadProgressVideo(
                type = ProgressVideoType.ORIGIN, progress = 0.0F
            )
            RxBus.publish(busData)

            val originVideoWebm = onWithIO {
                if (data.isInitContents) {
                    //최초생성
                    ffmpegProvider.runOriginVideoWebm(data) { publishOriginProgress(it) }
                } else {

                    val removeAudioFromVideo = async { ffmpegProvider.removeAudioFromVideo(data) }
                    DLogger.d(TAG, "removeAudioFromVideo!!!!  ${removeAudioFromVideo.await()}")
                    if (removeAudioFromVideo.await()) {
                        DLogger.d(TAG, "removeAudioFromVideo!!!!  runJoinOriginVideoWebm goooooo  ${removeAudioFromVideo.await()}")
                        //참여
                        ffmpegProvider.runJoinOriginVideoWebm(data) { publishOriginProgress(it) }
                    } else {
                        false
                    }
                }
            }

            //프리뷰
            val highlight = onWithIO { ffmpegProvider.runHighlight(data) { publishHighlightProgress(it) } }
            //썸네일
            val thumbnail = onWithIO { ffmpegProvider.runThumbnail(data) }
            //오디오
            val originAudio = onWithIO { ffmpegProvider.runOriginAudioWav(data) }
            //결과
            val result = originVideoWebm && highlight && thumbnail && originAudio
            DLogger.d(TAG, "encodeDuetBattleVideo  =>origin :${originVideoWebm} / highlight :${highlight} /  thumbnail :${thumbnail} audio :${originAudio}/ ")
            handleServiceByEncodeState(result, data)

        } catch (e: Exception) {
            handleServiceByEncodeState(false, data)
            DLogger.d(TAG, "encodeDuetBattleVideo Exception =>${e}")
        }
    }

    /**
     * 결과
     */
    private fun handleServiceByEncodeState(isSuccess: Boolean, encodeData: EncodeData) {

        DLogger.d(TAG, "[s] Encode 결과 ==============================================")
        //DLogger.d(TAG, "encodeData =>${encodeData}")
        DLogger.d(TAG, "타입 : ${encodeData.singType}")
        DLogger.d(TAG, "미디어 : ${encodeData.mediaType}")
        DLogger.d(TAG, "파트 : ${encodeData.partType}")
        DLogger.d(TAG, "노래명 : ${encodeData.songName}")
        DLogger.d(TAG, "내용 : ${encodeData.feedDesc}")
        DLogger.d(TAG, "별점 : ${encodeData.score}")
        DLogger.d(TAG, "tag : ${encodeData.feedTag}")
        DLogger.d(TAG, "uploadType : ${encodeData.uploadType}")
        DLogger.d(TAG, "구간여부 : ${encodeData.isSection}")
        DLogger.d(TAG, "최초 생성 여부 : ${encodeData.isInitContents}")
        DLogger.d(TAG, "편집후 저장 여부 : ${encodeData.isSaveAndFinish}")

        DLogger.d(TAG, "encodeDirPath : ${encodeData.encodeDirPath}")
        DLogger.d(TAG, "encodeHighlightPath : ${encodeData.encodeHighlightPath}")
        DLogger.d(TAG, "encodeThumbPath : ${encodeData.encodeThumbPath}")
        DLogger.d(TAG, "encodeOriginVideoWebmPath : ${encodeData.encodeOriginVideoWebmPath}")
        DLogger.d(TAG, "encodeOriginAudioWavPath : ${encodeData.encodeOriginAudioWavPath}")

        DLogger.d(TAG, "인코딩 결과 ====> ${isSuccess}")

        DLogger.d(TAG, "encodeData=>  ${encodeData}")
        DLogger.d(TAG, "[E] Encode 결과 ==============================================")

        encodeData.isEncodedCompleted = isSuccess

        if (isSuccess) {
            //call api
            requestResourcePath(encodeData)
        } else {
            //실패시 저장 또는 삭제
            saveData(encodeData)
        }
    }

    /**
     * 1.유형/타입별 실제 파일 존재 여부 체크
     * 2. true : 내부 pref 저장 , false : sing dtm dir , 업로드 LIST에서 제거
     */
    private fun saveData(encodeData: EncodeData) = onIO {

        showCompleteNotification(encodeData, false)

        val isFile = async { checkFiles(encodeData) }
        RxBus.publish(RxBusEvent.SingUploadProgressEvent.UploadEnd())

        if (loginManager.isLogin()) {
            DLogger.d(TAG, "@@ AppData.IS_REUPLOAD_ING : ${AppData.IS_REUPLOAD_ING}")
            DLogger.d(TAG, "@@ EncodeFailEvent 호출")
            RxBus.publish(RxBusEvent.EncodeFailEvent(encodeData = encodeData, isFile = isFile.await()))
        }
        //피드 컨텐츠
        if (encodeData.uploadType == VideoUploadPageType.SING_CONTENTS) {

            if (isFile.await()) {
                //2023-07-24 내부 저장 막음.
//                addEncodeData(encodeData)
//                //인코딩 내부 데이터 삭제
                removeEncodeDir(encodeData)
            } else {
//                //제거
                removeSingInfo(encodeData)
            }
        } else {
            // 230912
            // 앨범 영상 (일반영상) 일 경우도 파일 유무 체크해서 remove하는 것으로 로직 변경
            if (!isFile.await()) {
                removeSingInfo(encodeData)
            }
        }

        //list에서 제거
        removeEncodeDataList()

        stopService()
        //체크 후 대기열에 있으면 인코딩 시작
//        prepareEncodeSongData()
    }

    /**
     * get Upload Path
     */
    private fun requestResourcePath(encodeData: EncodeData) {
        DLogger.d(TAG, "START requestResourcePath CALL")

        apiService.getResourcePath(resType = ResourcePathType.FEED.code).applyApiScheduler().subscribe({ res ->
            if (res.status == HttpStatusType.SUCCESS.status && res.result != null) {
                startSingUpload(res.result, encodeData)
            } else {
                //실패
                saveData(encodeData)
                DLogger.d(TAG, "fail getResourcePath")
            }
        }, {
            //실패
            saveData(encodeData)
            DLogger.d(TAG, "error getResourcePath=> ${it}")
        }).addTo(compositeDisposable)

    }

    /**
     * API CALL - 피드 업로드
     */
    private fun requestUploadFeed(encodeData: EncodeData, resPath: GetResourcePathInfo) {
        DLogger.d(TAG, "START requestUploadFeed CALL")
        apiService.uploadFeed(
            UploadFeedBody(
                songMngCd = encodeData.songMainData?.songMngCd ?: "",
                orgFeedMngCd = encodeData.songMainData?.orgFeedMngCd ?: "",
                paTpCd = encodeData.singType,
                mdTpCd = encodeData.mediaType,
                singPartType = encodeData.partType,
                singPoint = encodeData.singPoint.toString(),
                mrVol = encodeData.instVolume.toInt().toString(),
                micVol = encodeData.micVolume.toInt().toString(),
                singTpCd = if (encodeData.isSection) SingingType.SECTION.code else SingingType.ALL.code,
                secStartTime = encodeData.sectionStartMs.toString(),
                secFinishTime = encodeData.sectionEndMs.toString(),
                feedDesc = encodeData.feedDesc,
                feedTag = encodeData.feedTag,
                useReply = encodeData.useReply,
                showContentsType = encodeData.showContentsType,
                feedMngCd = resPath.feedMngCd,
                thumbPicPath = resPath.thumbPicPath,
                orgConPath = resPath.orgConPath,
                highConPath = resPath.highConPath,
                audioConPath = resPath.audioConPath
            )
        ).applyApiScheduler().doOnSuccess { res ->
            DLogger.d(TAG, "Success requestUploadFeed=> ${res}")
            if (res.status == HttpStatusType.SUCCESS.status && res.result != null) {
                uploadSuccess(encodeData)
            } else {
                //실패
                saveData(encodeData)
            }
        }.doOnError {
            //실패
            saveData(encodeData)
            DLogger.d(TAG, "fail requestUploadFeed=> ${it}")
        }.subscribe().addTo(compositeDisposable)
    }


    /**
     * 부르기,앨범 영상 Azure 로 업로드
     */
    private fun startSingUpload(resPath: GetResourcePathInfo, encodeData: EncodeData) {

        CoroutineScope(Dispatchers.Default + job).launch {
            //인코딩된 파일 체크
            if (checkEncodedFiles(encodeData)) {

                val resultUpload = async { uploadEncodedFiles(encodeData, resPath) }
                if (resultUpload.await()) {
                    DLogger.d(TAG, "UPLOAD SUCCESS!!!")
                    delay(50)
                    publishUploadSuccess()
                    requestUploadFeed(encodeData, resPath)
                } else {
                    //실패
                    saveData(encodeData)
                }
            } else {
                //실패
                saveData(encodeData)
            }
        }
    }

    /**
     * 성공 후 로직 수행
     */
    private fun uploadSuccess(encodeData: EncodeData) = onIO {
        //RxBus.publish(RxBusEvent.MyPageUploadRefreshEvent(type = encodeData.uploadType))
        showCompleteNotification(encodeData, true)
        removeSingInfo(encodeData)
        stopService()
    }

    /**
     * 솔로인경우 Mix,MP4 파일 여부 체크
     * 듀엣배츨 참여인경우  Mix,MP4,orgPath(생성자컨텐츠) 파일 여부 체크
     */
    private suspend fun checkFiles(encodeData: EncodeData): Boolean {

        when (encodeData.singType) {
            SingType.SOLO.code -> {
                when (encodeData.mediaType) {

                    MediaType.AUDIO.code -> {
                        DLogger.d(TAG, "CheckFile SOLO 타입 AUDIO")
                        return Common.isFileExists(encodeData.mixPath)
                    }

                    MediaType.VIDEO.code -> {
                        DLogger.d(TAG, "CheckFile SOLO 타입 VIDEO")
                        val isMix = Common.isFileExists(encodeData.mixPath)
                        val isMp4 = Common.isFileExists(encodeData.videoMp4Path)
                        return isMix && isMp4
                    }
                }
            }

            SingType.BATTLE.code, SingType.DUET.code -> {
                when (encodeData.mediaType) {
                    MediaType.AUDIO.code -> {
                        DLogger.d(TAG, "CheckFile DUET 타입 AUDIO")
                        val isMix = Common.isFileExists(encodeData.mixPath)
                        val isMp4 = Common.isFileExists(encodeData.videoMp4Path)
                        return isMix && isMp4
                    }

                    MediaType.VIDEO.code -> {
                        DLogger.d(TAG, "CheckFile DUET 타입 VIDEO")
                        //최초 생성인 경우 오디오(mix) / 영상(바누바) 체크

                        if (encodeData.isInitContents) {
                            val isMix = Common.isFileExists(encodeData.mixPath)
                            val isMp4 = Common.isFileExists(encodeData.videoMp4Path)
                            return isMix && isMp4
                        } else {

                            val isMix = Common.isFileExists(encodeData.mixPath)
                            val isMp4 = Common.isFileExists(encodeData.videoMp4Path)
                            val isOrgCon = Common.isFileExists(encodeData.orgConPath)
                            return isMix && isMp4 && isOrgCon
                        }
                    }
                }
            }

            SingType.NORMAL.code -> {
                DLogger.d(TAG, "CheckFile NORMAL 타입 VIDEO")
                val isMp4 = Common.isFileExists(encodeData.videoMp4Path)
                DLogger.d(TAG, "${isMp4}")

                return isMp4
            }
        }

        return false
    }

    /**
     * 인코딩 완성된 파일 있는지 체크
     */
    private suspend fun checkEncodedFiles(encodeData: EncodeData): Boolean {

        when (encodeData.singType) {
            //앨범 or 솔로
            SingType.NORMAL.code, SingType.SOLO.code -> {
                when (encodeData.mediaType) {

                    MediaType.AUDIO.code -> {

                        val isOriginVideoWebm = Common.isFileExists(encodeData.encodeOriginVideoWebmPath)
                        val isHighlight = Common.isFileExists(encodeData.encodeHighlightPath)

                        DLogger.d(
                            TAG, "checkEncodedFiles SOLO 타입 AUDIO ${isOriginVideoWebm} /  ${isHighlight}"
                        )

                        return isOriginVideoWebm && isHighlight
                    }

                    MediaType.VIDEO.code -> {
                        val isOriginVideoWebm = Common.isFileExists(encodeData.encodeOriginVideoWebmPath)
                        val isHighlight = Common.isFileExists(encodeData.encodeHighlightPath)
                        val thumbNail = Common.isFileExists(encodeData.encodeThumbPath)
                        DLogger.d(
                            TAG, "checkEncodedFiles SOLO 타입 VIDEO ${isOriginVideoWebm} /  ${isHighlight} / ${thumbNail}"
                        )
                        return isOriginVideoWebm && isHighlight && thumbNail
                    }
                }
            }

            SingType.BATTLE.code, SingType.DUET.code -> {
                when (encodeData.mediaType) {
                    MediaType.AUDIO.code -> {

                        val isOriginVideoWebm = Common.isFileExists(encodeData.encodeOriginVideoWebmPath)
                        val isHighlight = Common.isFileExists(encodeData.encodeHighlightPath)

                        var result = if (encodeData.isInitContents) {
                            //최초생성
                            //오디오
                            val originAudio = Common.isFileExists(encodeData.encodeOriginAudioWavPath)
                            DLogger.d(
                                TAG, "checkEncodedFiles DUET 타입 AUDIO ${isOriginVideoWebm} /  ${isHighlight} / ${originAudio}"
                            )
                            //결과
                            isOriginVideoWebm && isHighlight && originAudio
                        } else {
                            //참여 인경우
                            DLogger.d(
                                TAG, "checkEncodedFiles DUET 참여 타입 AUDIO ${isOriginVideoWebm} /  ${isHighlight}"
                            )
                            isOriginVideoWebm && isHighlight
                        }
                        return result
                    }

                    MediaType.VIDEO.code -> {
                        val isOriginVideoWebm = Common.isFileExists(encodeData.encodeOriginVideoWebmPath)
                        val isHighlight = Common.isFileExists(encodeData.encodeHighlightPath)
                        val thumbNail = Common.isFileExists(encodeData.encodeThumbPath)
                        val originAudio = Common.isFileExists(encodeData.encodeOriginAudioWavPath)

                        DLogger.d(
                            TAG, "checkEncodedFiles DUET/BATTLE 참여 타입 VIDEO ${isOriginVideoWebm} /  ${isHighlight} / ${thumbNail} / ${originAudio}"
                        )

                        return isOriginVideoWebm && isHighlight && thumbNail && originAudio
                    }
                }
            }
        }
        return false
    }

    /**
     * 파일 업로드 처리하느 함수
     * @param filePath 파일 경로
     * @param azurePath 업로드 파일 경로
     * @param mimeType ContentsType
     * @param uploadType 업로드 타입 [UploadType.AUDIO], [UploadType.VIDEO]
     * @param startProgress 초기 업로드 진행률 업로드 파일이 많아지면 초기 진행률 값은 변경되어야 한다
     * @param progressWeight 업로드 진행률 가중치 100.0F / N
     */
    private suspend fun runUploadProgress(
        filePath: String, azurePath: String, mimeType: String, uploadType: UploadType, startProgress: Float, progressWeight: Float
    ): Pair<Boolean, Float> {
        return withContext(Dispatchers.IO) {
            try {
                delay(1000)
                var progress = 0.0F
                val result = fileProvider.onUploadFiles(filePath, azurePath, mimeType) {
                    val tempProgress = (it.toFloat() * progressWeight) / 100.0F
                    if (progress <= tempProgress) {
                        progress = tempProgress
                        publishUploadEvent(uploadType, startProgress.plus(progress))
                    }
                }
                return@withContext result to startProgress.plus(progress)
            } catch (ex: Exception) {
                return@withContext false to 100.0F
            }
        }
    }

    /**
     * 각 타입에 맞게 RxBusEvent 처리 하는 함수
     * @param type 업로드 타입, [UploadType.AUDIO], [UploadType.VIDEO]
     * @param progress 진행률 0.0 ~ 100.0
     */
    private fun publishUploadEvent(type: UploadType, progress: Float) {
        val busEvent: RxBusEvent.SingUploadProgressEvent = if (type == UploadType.AUDIO) {
            UploadProgressAudio(progress)
        } else {
            UploadProgressVideo(
                type = ProgressVideoType.UPLOAD, progress = progress
            )
        }
        DLogger.d("UploadEvent $busEvent")
        RxBus.publish(busEvent)
    }

    /**
     * 업로드 Path 준비
     */
    private suspend fun uploadEncodedFiles(
        encodeData: EncodeData, resPath: GetResourcePathInfo
    ): Boolean {
        return onWithIO {
            when (encodeData.singType) {
                SingType.NORMAL.code, SingType.SOLO.code -> {
                    uploadNormalFiles(encodeData, resPath)
                }

                SingType.BATTLE.code, SingType.DUET.code -> {
                    uploadDuetFiles(encodeData, resPath)
                }

                else -> {
                    false
                }
            }
        }
    }


    /**
     * 파일 업로드 기본 타입
     * @param encodeData 인코딩된 데이터
     * @param resPath 업로드할 파일 데이터 모델
     */
    private suspend fun uploadNormalFiles(
        encodeData: EncodeData, resPath: GetResourcePathInfo
    ): Boolean {
        var result = false
        if (encodeData.mediaType == MediaType.AUDIO.code) {
            val progressWeight = 100.0F / 2.0F
            var currentProgress = 0.0F
            val origin = runUploadProgress(
                encodeData.encodeOriginVideoWebmPath, resPath.orgConPath, fileProvider.getMimeTypeOctetStream(), UploadType.AUDIO, currentProgress, progressWeight
            )
            currentProgress = origin.second
            val highlight = runUploadProgress(
                encodeData.encodeHighlightPath, resPath.highConPath, fileProvider.getMimeTypeOctetStream(), UploadType.AUDIO, currentProgress, progressWeight
            )
            DLogger.d(TAG, "uploadEncodedFiles SOLO 타입 AUDIO $origin / $highlight")
            result = origin.first && highlight.first
        } else if (encodeData.mediaType == MediaType.VIDEO.code) {
            val progressWeight = 100.0F / 3.0F
            var currentProgress = 0.0F
            val origin = runUploadProgress(
                encodeData.encodeOriginVideoWebmPath, resPath.orgConPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )
            currentProgress = origin.second
            val highlight = runUploadProgress(
                encodeData.encodeHighlightPath, resPath.highConPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )
            currentProgress = highlight.second
            val thumbnail = runUploadProgress(
                encodeData.encodeThumbPath, resPath.thumbPicPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )

            DLogger.d(TAG, "uploadEncodedFiles SOLO 타입 VIDEO $origin / $highlight / $thumbnail")
            result = origin.first && highlight.first && thumbnail.first
        }

        return result
    }

    /**
     * 파일 업로드 2명이서 부르는 타입
     * @param encodeData 인코딩된 데이터
     * @param resPath 업로드할 파일 데이터 모델
     */
    private suspend fun uploadDuetFiles(
        encodeData: EncodeData, resPath: GetResourcePathInfo
    ): Boolean {
        var result = false
        if (encodeData.mediaType == MediaType.AUDIO.code) {
            val progressWeight = 100.0F / if (encodeData.isInitContents) 3.0F else 2.0F
            var currentProgress = 0.0F
            val origin = runUploadProgress(
                encodeData.encodeOriginVideoWebmPath, resPath.orgConPath, fileProvider.getMimeTypeOctetStream(), UploadType.AUDIO, currentProgress, progressWeight
            )
            currentProgress = origin.second
            val highlight = runUploadProgress(
                encodeData.encodeHighlightPath, resPath.highConPath, fileProvider.getMimeTypeOctetStream(), UploadType.AUDIO, currentProgress, progressWeight
            )
            currentProgress = highlight.second
            if (encodeData.isInitContents) {
                //최초생성
                //오디오
                val originAudio = runUploadProgress(
                    encodeData.encodeOriginAudioWavPath, resPath.audioConPath, fileProvider.getMimeTypeOctetStream(), UploadType.AUDIO, currentProgress, progressWeight
                )
                DLogger.d(
                    TAG, "uploadEncodedFiles DUET 타입 AUDIO $origin / $highlight / $originAudio"
                )
                //결과
                result = origin.first && highlight.first && originAudio.first
            } else {
                //참여 인경우
                result = origin.first && highlight.first
            }
        } else if (encodeData.mediaType == MediaType.VIDEO.code) {
            val progressWeight = 100.0F / 4.0F
            var currentProgress = 0.0F
            val origin = runUploadProgress(
                encodeData.encodeOriginVideoWebmPath, resPath.orgConPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )
            currentProgress = origin.second
            val highlight = runUploadProgress(
                encodeData.encodeHighlightPath, resPath.highConPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )
            currentProgress = highlight.second
            val thumbnail = runUploadProgress(
                encodeData.encodeThumbPath, resPath.thumbPicPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )
            currentProgress = thumbnail.second
            val originAudio = runUploadProgress(
                encodeData.encodeOriginAudioWavPath, resPath.audioConPath, fileProvider.getMimeTypeOctetStream(), UploadType.VIDEO, currentProgress, progressWeight
            )
            DLogger.d(
                TAG, "uploadEncodedFiles DUET/BATTLE 참여 타입 VIDEO $origin / $highlight / $thumbnail / $originAudio"
            )
            result = origin.first && highlight.first && thumbnail.first && originAudio.first
        }
        return result
    }

    /**
     *  내부에 저장된 encode 데이터 get
     */
    private fun getPrefEncodeDataList(): MutableList<EncodeData> {
        return accountPref.getEncodedData()
    }

    /**
     * get targetData
     */
    private suspend fun getEncodeData(): EncodeData? {
        if (!encodeDataList.isNullOrEmpty() && encodeDataList.size > 0) {
            DLogger.d(TAG, "getEncodeData size=>${encodeDataList.size}")
            return encodeDataList[0]
        }
        return null
    }

    /**
     * add data
     */
    private suspend fun addEncodeData(encodeData: EncodeData) {

        val encodeDataList = getPrefEncodeDataList()

        val check = encodeData in encodeDataList

        DLogger.d(TAG, "addEncodeData check=> ${check}")

        if (!check) {
            DLogger.d(TAG, "addEncodeData pref 저장=> ${encodeData.songName}")
            encodeDataList.add(encodeData)
            accountPref.setEncodedData(encodeDataList)
        } else {
            DLogger.d(TAG, "이미 저장된 부르기 정보 있음=> ${encodeData.songName}")
        }
    }

    fun find(values: List<SubscData>, value: String): Boolean {
        for (e in values) {
            if (e.subscTpCd == value) {
                return true
            }
        }
        return false
    }

    /**
     * sing dtm , data list 에서 삭제
     */
    private suspend fun removeSingInfo(encodeData: EncodeData) {
        //sing_dtm 폴더 및 하위 삭제
        removeSingDir(encodeData)
        //list 삭제
        removeEncodeDataList()
    }


    /**
     * Sing Encode dir 삭제
     */
    private suspend fun removeEncodeDir(encodeData: EncodeData) {
        //sing_dtm 폴더 및 하위 삭제
        Common.removeFiles(encodeData.encodeDirPath, "")
    }

    /**
     * Sing dtm dir 삭제
     */
    private suspend fun removeSingDir(encodeData: EncodeData) {

        val dir = if (encodeData.uploadType == VideoUploadPageType.SING_CONTENTS) {
            encodeData.singDirPath
        } else {
            encodeData.albumDirPath
        }
        //n_dtm 폴더 및 하위 삭제
        Common.removeFiles(dir)
    }

    /**
     * targetData remove
     */
    private suspend fun removeEncodeDataList() {
        if (!encodeDataList.isNullOrEmpty() && encodeDataList.size > 0) {
            encodeDataList.removeAt(0)
        }
    }

    /**
     * Show Noti Bar
     */
    private suspend fun showNotification(data: EncodeData) {
        onWithContextMain {
            if (loginManager.getUserLoginData().isShowingUplPrg == true) {
                startForeground(NOTIFICATION_UPLOAD_ID, createUpLoadingNotification(data))
            }
        }
    }

    private fun showCompleteNotification(data: EncodeData, isSuccess: Boolean) {

        onMain {
            val isShow = if (isSuccess) {
                loginManager.getUserLoginData().isShowingUplSuccess == true
            } else {
                loginManager.getUserLoginData().isShowingUplFail == true
            }
            if (isShow) {
                notificationManager.notify(NOTIFICATION_COMPLETE_ID, createCompleteNotification(data, isSuccess))
            }

            //VideouploadActivity 성공or실패 유무 전송
            RxBus.publish(RxBusEvent.EncodeEvent(isSuccess = isSuccess, encodeData = data))
        }
    }


    /**
     * 업로드 시작 Notification
     */
    private fun createUpLoadingNotification(data: EncodeData) = NotificationCompat.Builder(this, CHANNEL_ID).apply {
        setContentTitle(data.songMainData?.songNm)
        setContentText(resourceProvider.getRes().getString(R.string.encode_ing))
        setSmallIcon(R.drawable.app_icon)
        setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_icon))
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setCategory(Notification.CATEGORY_SERVICE)

        setContentIntent(
            PendingIntent.getActivity(
                this@SongEncodeService, 0, Intent(this@SongEncodeService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }, PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    /**
     * 업로드 결과 완료 or 실패 Notification
     */
    private fun createCompleteNotification(data: EncodeData, isSuccess: Boolean) = NotificationCompat.Builder(this, CHANNEL_ID).apply {

        if (isSuccess) {
            setContentTitle(data.songMainData?.songNm)
            setContentText(resourceProvider.getRes().getString(R.string.encode_success))
        } else {
            setContentTitle(data.songMainData?.songNm)
            setContentText(resourceProvider.getRes().getString(R.string.encode_fail))
        }
        setSmallIcon(R.drawable.app_icon)
        setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_icon))
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setContentIntent(
            PendingIntent.getActivity(
                this@SongEncodeService, 0, Intent(this@SongEncodeService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }, PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    /**
     * Create Channel
     */
    private fun registerDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createDefaultNotificationChannel())
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun createDefaultNotificationChannel() = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
        description = CHANNEL_DESCRIPTION
        this.setShowBadge(true)
        this.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
    }

    override fun stopService(name: Intent?): Boolean {
        DLogger.d(TAG, "Service stopService")
        onClearCompositeDisposable()
        return super.stopService(name)
    }

    override fun onDestroy() {
        DLogger.d(TAG, "Service onDestroy")
        DLogger.d(TAG, "${AppData.IS_ENCODE_ING}")

        if (AppData.IS_ENCODE_ING) {
            onIO {
                getEncodeData()?.let {
                    saveData(it)
                }
            }
        }

        onClearCompositeDisposable()
        FFmpeg.cancel()
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        DLogger.d(TAG, "Service onTrimMemory level=> $level")
//        onClearCompositeDisposable()
        super.onTrimMemory(level)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        DLogger.d(TAG, "Service onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }

    private fun onClearCompositeDisposable() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }

        if (::job.isInitialized) {
            if (job != null) {
                job.cancel()
            }
        }

        AppData.IS_ENCODE_ING = false
    }

    /**
     * Origin Video Progress Publish
     * @param data Encoding State
     */
    private fun publishOriginProgress(data: EncodeDelegateState) {
        val busEvent = UploadProgressVideo(
            type = ProgressVideoType.ORIGIN, progress = data.progress
        )
        RxBus.publish(busEvent)
    }

    /**
     * Highlight Video Progress Publish
     * @param data Encoding State
     */
    private fun publishHighlightProgress(data: EncodeDelegateState) {
        val busEvent = UploadProgressVideo(
            type = ProgressVideoType.HIGHLIGHT, progress = data.progress
        )
        RxBus.publish(busEvent)
    }

    /**
     * 비디오 업로드 성공시 프로그래스 전체적으로 종료하는 함수
     */
    private fun publishUploadSuccess() {
        RxBus.publish(RxBusEvent.SingUploadProgressEvent.UploadEnd())
    }

    private fun stopService() {
        if (::job.isInitialized) {
            if (job != null) {
                job.cancel()
            }
        }
        AppData.IS_ENCODE_ING = false
        stopSelf()
    }
}