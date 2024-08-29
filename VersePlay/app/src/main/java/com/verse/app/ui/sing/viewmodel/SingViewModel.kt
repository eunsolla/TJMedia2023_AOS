package com.verse.app.ui.sing.viewmodel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ChallengeType
import com.verse.app.contants.Config
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.MediaType
import com.verse.app.contants.PartType
import com.verse.app.contants.SingEffectType
import com.verse.app.contants.SingPageType
import com.verse.app.contants.SingType
import com.verse.app.contants.SingingCommandType
import com.verse.app.contants.SingingErrorType
import com.verse.app.contants.SingingPartType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.multiNullCheck
import com.verse.app.extension.onDefault
import com.verse.app.extension.onIO
import com.verse.app.extension.onMain
import com.verse.app.extension.onWithContextIO
import com.verse.app.extension.onWithContextMain
import com.verse.app.extension.parcelable
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.encode.EncodeData
import com.verse.app.model.param.SingHistoryBody
import com.verse.app.model.sing.SingIntentModel
import com.verse.app.model.song.SongMainData
import com.verse.app.model.sp.SoundFilterData
import com.verse.app.model.xtf.XTF_DTO
import com.verse.app.model.xtf.XTF_EVENT_DTO
import com.verse.app.model.xtf.XTF_LYRICE_DTO
import com.verse.app.model.xtf.XTF_SECTION_DTO
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.sing.fragment.SectionFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.exo.ExoStyledPlayerView
import com.verse.app.utility.ffmpegkit.FFmpegProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.provider.SingPathProvider
import com.verse.app.utility.provider.SuperpoweredProvider
import com.verse.app.widget.views.TJTextView
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext


/**
 * Description : Sing ViewModel
 *
 * Created by jhlee on 2023-03-29
 */
@HiltViewModel
class SingViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val apiService: ApiService,
    private val resourceProvider: ResourceProvider,
    private val deviceProvider: DeviceProvider,
    private val loginManager: LoginManager,
    private val fileDownloadProvider: FileProvider,
    private val superpoweredProvider: SuperpoweredProvider,
    val singPathProvider: SingPathProvider,
    private val ffMpegProvider: FFmpegProvider
) : ActivityViewModel(), SectionFragment.Listener {

    companion object {
        const val ONE_MINUTE = 60000 //1분 경과
        const val FIFTEEN_SECONDS = 15000 // 15초 경과
        const val FORTY_FIVE_SECONDS = 45000 // 45초 경과
        const val VIDEO_WIDTH = 640  //바누바 width
        const val VIDEO_HEIGHT = 1380 //바누바 height
        const val SYNC_DEFAULT_PLUS = 20 //싱크 기본값
        const val SYNC_GAP = 10 //싱크 기준값
        const val SYNC_MAX = 600 //싱크 최대치
        const val PREVIEW_GAP = 30000 // 프리뷰 기준값
        const val RV_ROW_VIDEO = 4 // 비디오 row 기본값
        const val RV_ROW_AUDIO = 6 // 오디오 row 기본값
        const val SINGING_SCROLL_POSITION = 2 //부르기 스크롤 기준 값
        const val TAG = "[Singing...]"
    }

    //곡 정보 param
    //솔로 ,듀엣 ,배틀 부르기 타입
    private val _singTypeParam: MutableLiveData<String> by lazy { MutableLiveData() }
    val singTypeParam: LiveData<String> get() = _singTypeParam

    //음원 관리 코드
    private val _songMngCdParam: MutableLiveData<String> by lazy { MutableLiveData() }

    //피드 관리 코드
    private val _feedMngCdParam: MutableLiveData<String> by lazy { MutableLiveData() }

    //피드 녹화/녹음 관리 코드
    private val _feedMdTpCdParam: MutableLiveData<String> by lazy { MutableLiveData() }

    //연관 음원
    val moveToRelatedSoundSource: SingleLiveEvent<String> by lazy { SingleLiveEvent() }

    //45초 부른후 종료 팝업
    private val _showNotWholeSongPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val showNotWholeSongPopup: LiveData<Unit> get() = _showNotWholeSongPopup

    //네트워크 상태
    private val _isNetWork: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isNetWork: LiveData<Boolean> get() = _isNetWork

    //VIP popup
    private val _showVipPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val showVipPopup: LiveData<Unit> get() = _showVipPopup

    //곡 정보 api
    private val _songMainData: MutableLiveData<SongMainData> by lazy { MutableLiveData() }
    val songMainData: LiveData<SongMainData> get() = _songMainData

    //곡 정보 보기
    val isInitContents: NonNullLiveData<Boolean> by lazy {
        NonNullLiveData(_singTypeParam.value == SingType.SOLO.code)
    }

    //완곡 가능 여부
    private val _isWholeSong: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val isWholeSong: LiveData<Boolean> get() = _isWholeSong

    //Fragment 전환
    val singPageType = NonNullLiveData<SingPageType>(SingPageType.PREPARE)

    //닫기
    val finish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    //이어폰 팝업
    val showEarphonePopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    //정지
    val showPausePopup: MutableLiveData<Boolean> by lazy { MutableLiveData(false) }

    //곡 정보 보기
    val songInfoState: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }

    //가사 정보 rv refresh
    val refreshLyricsView: MutableLiveData<Pair<TJ_RV_STATE, Int>> by lazy { MutableLiveData() }

    // 솔로 하단 노래 가사 Blur
    val toggleLyricsBlur: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    //내 프로필 이미지
    private val _myImgProfile: NonNullLiveData<String> by lazy {
        NonNullLiveData(
            loginManager.getUserLoginData()?.pfFrImgPath ?: ""
        )
    }
    val myImgProfile: LiveData<String> get() = _myImgProfile

    //vp swipe false
    val isVpSwipe = NonNullLiveData(false)

    private val _pageList: ListLiveData<SingType> by lazy { ListLiveData() }
    val pageList: ListLiveData<SingType> get() = _pageList

    // [e] Part ViewPager
    private val _curSingType: MutableLiveData<SingType> by lazy { MutableLiveData() }//solo ,duet, battle
    val curSingType: LiveData<SingType> get() = _curSingType

    val currentPos: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }      //viewpager 현재 포지션값
    val prevPos: NonNullLiveData<Int> by lazy { NonNullLiveData(-1) }         //viewpager 이전 포지션값
    val currentState: MutableLiveData<Int> by lazy { MutableLiveData() }                    //viewpager 상태

    //현재 솔로/듀엣/배틀 구분
    private val _off: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }   //off 선택
    val off: LiveData<Boolean> get() = _off

    val curMediaType: NonNullLiveData<String> by lazy { NonNullLiveData(MediaType.NONE.code) }   //현재 녹음/녹화
    val curPartType: NonNullLiveData<String> by lazy { NonNullLiveData("") }                      //A or B Part


    //[s]SuperPowered =====================================================================
    val soundFilterDataList: ListLiveData<SoundFilterData> by lazy {
        ListLiveData<SoundFilterData>().apply {
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_auto_tune))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_record))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_concert_hall))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_singing_room))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_megaphone))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_cave))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_squirrel))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_recording_studio))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_helium))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_robot))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_ghost))
            add(SoundFilterData(name = "", icon = R.drawable.selector_so_echo))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_reverb))
            add(SoundFilterData(name = "", icon = R.drawable.selector_sp_voicechanger))
        }
    }
    //[e]SuperPowered =====================================================================

    //[s] recoding ========================================================================
    private val oriXtfData: MutableLiveData<XTF_DTO> by lazy { MutableLiveData() }
    val curXtfData: MutableLiveData<XTF_DTO> by lazy { MutableLiveData() }
    val isStartRecording: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }   //녹화/녹음 시작 여부
    val showErrorDialog: SingleLiveEvent<SingingErrorType> by lazy { SingleLiveEvent() }          //에러 메세지
    val curMrAndXtfPath: NonNullLiveData<Triple<Boolean, String, String>> by lazy {                 //true(mr ok, xtf ok) , mr path, xtf path
        NonNullLiveData<Triple<Boolean, String, String>>(Triple(false, "", ""))
    } //MR XTF PATH
    val curOrgVideoAndAudioPath: NonNullLiveData<Triple<Boolean, String, String>> by lazy {                 //true(orgVideo ok, orgAudio ok)
        NonNullLiveData<Triple<Boolean, String, String>>(Triple(false, "", ""))
    } //MR XTF PATH
    val curSoundFilter: NonNullLiveData<Int> by lazy { NonNullLiveData(-1) }
    val showSoundFilter: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) } //SP Sound Filter show
    val showVolume: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }  //볼륨 show
    val showSection: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }  //구간 show
    val isSection: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }  //구간 설정 여부
    val sectionCurStartIndex: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }  //구간 설정 시작 시간
    val sectionCurEndIndex: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }  //구간 설정 종료 시간
    private val sectionCurStartMs: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }  //구간 설정 시작 시간
    private val sectionCurEndMs: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }  //구간 설정 종료 시간
    private val isSaveAndFinish: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }  //1분 경과 후 저장 편집 여부
    val showSync: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }  //싱크
    val showPreview: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }  //프리뷰 시간
    val curTotalMs: NonNullLiveData<Double> by lazy { NonNullLiveData(0.0) }           // 총 ms
    val totalMsText: NonNullLiveData<Pair<Int, Int>> by lazy { NonNullLiveData(0 to 0) }       // 총 ms 텍스트 값
    val isFinishIntroEvent = NonNullLiveData(false) //인트로 이벤트 종료 여부         //인트로 종료 여부
    val currentEvent: MutableLiveData<XTF_EVENT_DTO> by lazy { MutableLiveData() }    // 현재 이벤트
    val scrollPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(-1) }    //스크롤 이동 값

    private val _exoView: MutableLiveData<ExoStyledPlayerView> by lazy { MutableLiveData() }
    val exoView: MutableLiveData<ExoStyledPlayerView> get() = _exoView

    private val _isLoading: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val isLoading: LiveData<Boolean> get() = _isLoading

    //tj 음원 여부
    val isTjSong: Boolean by lazy {
        _songMainData.value?.let {
            it.isTJSound
        } ?: run {
            true
        }
    }
    var tjTextMap: MutableMap<Int, TJTextView> =
        mutableMapOf()                              //가사 정보 Map
    val progressPersent: NonNullLiveData<Int> by lazy { NonNullLiveData(0) } //프로그래스 값
    val progressText: NonNullLiveData<Pair<Int, Int>> by lazy { NonNullLiveData(0 to 0) } //부르기 경과 시간 텍스트 값
    val isOneMinute: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) } //1분 경과 유무
    private val _curSingingPartType: NonNullLiveData<SingingPartType> by lazy {
        NonNullLiveData(
            SingingPartType.DEFAULT
        )
    } //현재 진행중인 파트

    val curSingingPartType: NonNullLiveData<SingingPartType> get() = _curSingingPartType
    var isSingingFinished: Boolean = false // 종료 여부

    var tmpCurrentMs: Double = 0.0
    var tmpCurrentSingingPosition: Int =
        0                                                                  //현재 Ms 비교값
    var currentEventIndex: Int = 0// 이벤트 인덱스
    val isRecordCompleted: NonNullLiveData<Pair<Boolean, Boolean>> by lazy { NonNullLiveData(false to false) } //record complete 여부
    var volumeFadeDisposable: Disposable? = null
    var volumeFadeState = false
    var continuDisposable: Disposable? = null
    //[e] recoding ========================================================================

    //[s] 프리뷰 ========================================================================
    val isStartSync: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }                          //프리뷰 시작 여부
    val isSyncPlayingState: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }                          //프리뷰 시작 여부
    val curSyncMs: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }                                          //싱크 설정값
    val curPreViewMs: NonNullLiveData<Double> by lazy { NonNullLiveData(0.0) }                                //프리뷰 값 ms
    val curStartPreview: NonNullLiveData<Pair<Int, Int>> by lazy { NonNullLiveData(0 to 0) }              //프리뷰  설정값
    val curEndPreview: NonNullLiveData<Pair<Int, Int>> by lazy { NonNullLiveData(0 to (PREVIEW_GAP / 1000)) }              //프리뷰  설정값
    val curInstVolume: NonNullLiveData<Int> by lazy { NonNullLiveData(5) }              //mr 볼륨값
    val curMicVolume: NonNullLiveData<Int> by lazy { NonNullLiveData(5) }              //마이크 볼륨 값
    val liftState: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }              //true :resume , false : pause

    //프리뷰 seekbar 조작 여부 플래그
    var isEditing: Boolean = false

    //45초 경과시 부르기 적재 여부
    var isRequestHistory: Boolean = false


    private val _encodeData: MutableLiveData<EncodeData> by lazy { MutableLiveData(EncodeData()) }
    val encodeData: MutableLiveData<EncodeData> get() = _encodeData

    private val coroutineExceptionHandler = CoroutineExceptionHandler { ctx, throwable ->
        DLogger.d("Caught exception: $throwable")
        showErrorDialog.value = SingingErrorType.SINGING_EXCEPTION.apply {
            fromServerErrMsg = run {
                throwable.message?.let {
                    it
                } ?: run {
                    resourceProvider.getString(R.string.network_popup_undefined)
                }
            }
        }
    }
    lateinit var singingJob: CoroutineContext

    private val _startUploadPage: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startUploadPage: LiveData<Unit> get() = _startUploadPage

    private val _challengeType: MutableLiveData<ChallengeType> by lazy { MutableLiveData() }
    val challengeType: LiveData<ChallengeType> get() = _challengeType


    /**
     * RV 상태
     */
    enum class TJ_RV_STATE {
        NONE,
        RESIZE,
        REMOVE,
        DRAW,
        LINE_FINISHED,
    }


    fun start() {

        val intentDatab = getIntentData<Bundle>(ExtraCode.SINGING_SING_DATA)
        val intentData = intentDatab?.parcelable<SingIntentModel>(ExtraCode.SING_INTENT_MODEL)
        if (intentData == null) {
            finish.call()
            return
        }

        val singType: String = intentData.singType
        val songMngCd: String = intentData.songMngCd
        val feedMdTpCd: String = if (singType != SingType.SOLO.code) {
            intentData.feedMdTpCd
        } else {
            ""
        }
        val feedMngCd: String = if (singType != SingType.SOLO.code) {
            intentData.feedMngCd
        } else {
            ""
        }

        if (singType == SingType.SOLO.code) {
            //솔로 두가지 필수값
            if (singType.isNullOrEmpty() || songMngCd.isNullOrEmpty()) {
                finish.call()
                return
            }
        } else {
            //듀엣/배틀 세가지 필수값
            if (singType.isNullOrEmpty() || songMngCd.isNullOrEmpty() || feedMngCd.isNullOrEmpty()) {
                finish.call()
                return
            }
        }

        _singTypeParam.value = singType
        _songMngCdParam.value = songMngCd
        _feedMngCdParam.value = feedMngCd
        _feedMdTpCdParam.value = feedMdTpCd

        //get song info
        fetchSongInfo()
    }

    fun initStartRefresh() {
        RxBus.listen(RxBusEvent.RefreshDataEvent::class.java).subscribe({
            fetchSongInfo()
        }, {
            //error
        }).addTo(compositeDisposable)
    }


    /**
     * 곡 정보 Show / Hide
     */
    fun onShowSongInfo() {
        songInfoState.value = !songInfoState.value
    }

    /**
     * 부르기 준비
     * 1.초기화
     * 2.xtf로드
     * 3. sp init
     */
    fun prepareSong() {
        onMain {
            launch { onClearSing() }.join()
            launch { loadXtfDto() }.join()
            DLogger.d(TAG, "loadXtfDto")
            launch { initForAudio() }
            DLogger.d(TAG, "initForAudio")
        }
    }

    /**
     * 프리뷰 화면 준비
     */
    fun prepareSync(pv: ExoStyledPlayerView) {
        onMain {
            launch { onClearSing() }.join()
            launch { onClearSync() }.join()

            delay(1000)

            if (curMediaType.value == MediaType.VIDEO.code) {
                launch {
                    initPlayer(pv, singPathProvider.getResultVideoPath())
                }.join()
            }

//            launch { initForAudio() }
            setIsLoading(false)
            //부르기 이력 적재
            onLoadingDismiss()
        }
    }

    /**
     * Rv 가사 정보
     */
    fun setTjTextView(textView: TJTextView, position: Int) {
        if (!tjTextMap.containsKey(position)) {
            tjTextMap[position] = textView
        }
    }

    /**
     * Part ViewPager 스크롤 상태에 대한 값
     * @param state ViewPager2.State
     */
    fun onTopPageState(state: Int) {
        currentState.value = state
    }

    /**
     * Part ViewPager < ,> 이동
     * @param isLeft true -> 왼쪽, false -> 오른쪽
     */
    fun moveTopViewPager(isLeft: Boolean) {
        if (isLeft) {
            currentPos.value = currentPos.value.minus(1)
        } else {
            currentPos.value = currentPos.value.plus(1)
        }
    }

    /**
     * 현재 부르기 타입
     * 솔로/듀엣/OFF
     */
    fun onSingType(singType: SingType) {
        DLogger.d("onSingType ${singType.code}")
        _curSingType.value = singType

        onMain {
            onClearSingSelectedVariable()
        }

        onMain {
            if (singType != SingType.BATTLE) {
                if(curMediaType.value != MediaType.AUDIO.code){
                    delay(200)
                    curMediaType.value = MediaType.AUDIO.code
                }
            }else{
                curMediaType.value = MediaType.NONE.code
            }
        }
    }

    /**
     * 미디어  타입
     * 솔로/듀엣/OFF
     */
    fun onMediaType(mediaType: MediaType) {
        //솔로로 부르기 진입시에만 선택 가능
        if (isInitContents.value) {
            if (curSingType.value == SingType.BATTLE) {
                curMediaType.value = MediaType.VIDEO.code
            } else {
                curMediaType.value = mediaType.code
            }
        } else {
            curMediaType.value = mediaType.code
        }
    }


    /**
     * 미디어  타입
     * 솔로/듀엣/OFF
     */
    fun setOff() {
        _off.value = !_off.value
    }

    /**
     * 파트
     * A
     * B
     */
    fun onPartType(partType: PartType) {
        curPartType.value = partType.code
    }

    /**
     * 도전  타입
     * 구간
     * 전곡
     * A 파트
     * B 파트
     * 씽패스로 도전
     * 씽패스 A 파트로 도전
     * 씽패스 B 파트로 도전
     */
    fun onChallengeType(challengeType: ChallengeType) {

        if (curMrAndXtfPath.value.first) {

            val checking = run {
                if (curSingType.value == SingType.SOLO) {
                    curMediaType.value.isNotEmpty() && curMediaType.value != MediaType.NONE.code
                } else {
                    curMediaType.value.isNotEmpty() && curMediaType.value != MediaType.NONE.code && curPartType.value.isNotEmpty()
                }
            }

            if (checking) {
                _challengeType.value = challengeType
                _songMainData.value?.let {
                    if (!it.isWholeSong) {
                        _showVipPopup.call()
                    } else {
                        moveToPage(SingPageType.SING_ING)
                    }
                }
            } else {
                //임시 문구
                showToastIntMsg.value = R.string.str_singing_need_part_select
            }

        } else {
            DLogger.d("MR or XTF 준비 안 됨 ${curMrAndXtfPath.value}")
        }
    }

    /**
     * 편집 후 종료
     */
    fun onSingingEditAndSave() {
        isSingingFinished = true
        isSaveAndFinish.value = true
        sectionCurStartMs.value = 0
        sectionCurEndMs.value = tmpCurrentMs.toInt()
    }

    /**
     * Fragment 페이지 전환
     * @param targetPage : SECTION 구간부르기
     * @param targetPage : SING_ING 부르기
     * @param targetPage : SYNC_SING 싱크
     * @param targetPage : UPLOAD_FEED업로드중
     * @param targetPage : UPLOAD_FEED_COMPLETE 업로드 완료
     */
    fun moveToPage(targetPage: SingPageType) {

        if (targetPage == SingPageType.UPLOAD_FEED) {

            if (isStartSync.value) {
                //프리뷰 job cancel
                pauseSync()
            }

            onMain {

                onLoadingShow()

                //인코딩 정보 Set
                multiNullCheck(_songMainData.value, _curSingType.value) { mainData, singType ->
                    _encodeData.value?.let {
                        it.isInitContents = isInitContents.value
                        it.syncMs = curSyncMs.value
                        it.singType = singType.code
                        it.totalMs = curTotalMs.value.toInt()
                        it.instVolume = curInstVolume.value * 0.1
                        it.micVolume = curMicVolume.value * 0.1
                        it.mediaType = curMediaType.value
                        it.partType = curPartType.value
                        it.previewStartMs = curPreViewMs.value.toInt()
                        it.isSaveAndFinish = isSaveAndFinish.value
                        it.isSection = isSection.value
                        it.sectionStartMs = sectionCurStartMs.value
                        it.sectionEndMs = sectionCurEndMs.value
                        it.songName = mainData.songNm
                        it.songMainData = mainData
                        it.score = getScore()
                        it.isOff = _off.value
                        //[s]파일 경로
                        it.mrMp3Path = singPathProvider.getMrMp3Path()
                        it.mrWavPath = singPathProvider.getMrWavPath()
                        it.videoMp4Path = singPathProvider.getResultVideoPath()
                        it.mixPath = singPathProvider.getMixFilePath()
                        it.mrSectionWavPath = singPathProvider.getMrSectionWavPath()
                        it.encodeThumbPath = singPathProvider.getEncodeThumbPath()
                        it.encodeHighlightPath = singPathProvider.getEncodeHighlightPath()
                        it.encodeOriginVideoWebmPath = singPathProvider.getEncodeOriginVideoPath()
                        it.encodeOriginAudioWavPath = singPathProvider.getEncodeOriginAudioPath()
                        it.encodeDirPath = singPathProvider.getDirEncodePath()
                        it.singDirPath = singPathProvider.getPrefixSingInfo()
                        it.uploadType = singPathProvider.getPageType()
                        it.orgConPath = if (!isInitContents.value) curOrgVideoAndAudioPath.value.second else ""
                        it.reOrgConPath = if (!isInitContents.value) singPathProvider.getReOrgConPath() else ""
                        it.orgAudioPath = if (!isInitContents.value) curOrgVideoAndAudioPath.value.third else ""
                        it.waterMarkPath = singPathProvider.getWaterMarkPath()

                        onWithContextIO {

                            DLogger.d(TAG, " encodeData=> ${encodeData.value}")
                            //mr -> wav
                            val state = async { ffMpegProvider.onResultWav(it) }

                            if (state.await()) {
                                DLogger.d(TAG, " success mr to wav")
                                val mixState = async {

                                    DLogger.d(TAG, " mixParam==============================================")
                                    DLogger.d(TAG, " voice path=>${singPathProvider.getResultVoicePath()}")
                                    DLogger.d(TAG, " wav path=> ${singPathProvider.getMrWavPath()}")
                                    DLogger.d(TAG, " mix path=> ${it.mixPath}")
                                    DLogger.d(TAG, " sound filter=> ${curSoundFilter.value}")
                                    DLogger.d(TAG, " sync ms=> ${curSyncMs.value.toDouble()}")
                                    DLogger.d(TAG, " sample rate=> ${deviceProvider.getOutputSampleRate().toInt()}")
                                    DLogger.d(TAG, " startMs=> ${it.sectionStartMs.toDouble()}")
                                    DLogger.d(TAG, " inst volume=> ${it.instVolume.toFloat()}")
                                    DLogger.d(TAG, " mic volume=> ${it.micVolume.toFloat()}")
                                    DLogger.d(TAG, "  isSaveAndFinish.value=> ${isSaveAndFinish.value}")
                                    DLogger.d(TAG, " mixParam==============================================")

                                    val mrPath = if (!isSection.value && !isSaveAndFinish.value) {
                                        singPathProvider.getMrWavPath()
                                    } else {
                                        singPathProvider.getMrSectionWavPath()
                                    }

                                    //보이스+필터  mix 파일 생성
                                    superpoweredProvider.runMixFilterProcess(
                                        singPathProvider.getResultVoicePath(),
                                        mrPath,
                                        it.mixPath,
                                        curSoundFilter.value,
                                        curSyncMs.value.toDouble(),
                                        superpoweredProvider.getSampleRate().toInt(),
                                        it.sectionStartMs.toDouble(),
                                        it.instVolume.toFloat(),
                                        it.micVolume.toFloat()
                                    )
                                }

                                if (mixState.await()) {
                                    DLogger.d(TAG, " success mix")
                                    onWithContextMain {
                                        onLoadingDismiss()
                                        singPathProvider.createEncodeDir()
                                        _startUploadPage.call()
                                    }
                                } else {
                                    failMixHandle()
                                    DLogger.d(TAG, " faill mix")
                                }
                            } else {
                                failMixHandle()
                                DLogger.d(TAG, " faill mr to wav")
                            }
                        }
                    }
                } ?: run {
                    failMixHandle()
                }
            }
        } else if (targetPage == SingPageType.OFF_FEED_COMPLETE) {
            encodeData.value?.let {
                it.score = getScore()
                it.mediaType = curMediaType.value
                it.isOff = _off.value
            }

            onLoadingDismiss()
            _startUploadPage.call()
        } else {
            singPageType.value = targetPage
        }
    }

    /**
     * 구간 부르기
     */
    fun showSectionDialog() {
        if (curMediaType.value.isNotEmpty()) {
            showSection.value = true
        } else {
            showToastIntMsg.value = R.string.str_singing_need_part_select
        }
    }

    /**
     * mix fail
     */
    fun failMixHandle() {
        onMain {
            onLoadingDismiss()
            showToastIntMsg.value = R.string.network_popup_undefined
            moveToPage(SingPageType.PREPARE)
        }
    }

    /**
     * 페이지 닫기
     */
    fun onClose() {
        finish.call()
    }

    /**
     * 부르기 시작
     */
    private fun runSinging() = onMain(singingJob) {

        //[s]시작 처리========================
        DLogger.d(TAG, "runSinging currentEvent ${currentEvent.value} ${currentEventIndex} / ${sectionCurStartMs.value}")

        //시작
        onStartRecord()

        if (isSection.value && sectionCurStartIndex.value > 0) {
            launch { superpoweredProvider.setCurrentMs(sectionCurStartMs.value.toDouble()) }.join()
            //불륨 3초 간격 커지게
            if (curMicVolume.value > 0) {
                runMrFadeInOrOut(true)
            }
        }
        //인트로 이벤트
        if (currentEventIndex > -1) {
            singingEventLoop().join()
            DLogger.d(TAG, "event finish")
            //인트로 이벤트 종료 플래그
            onFinishEvent(true)
        }
        //텔롭
        singingLoop().join()
        //[s]완곡 후 처리========================
        DLogger.d(TAG, "finished record")
        onLoadingShow()
        setIsLoading(true)

        //녹음 종료
        launch { onStopVoiceRecord() }.join()

        //슈퍼파워드 정상 해제 체크
//        val check = async { checkStopRecordLoop() }
//        DLogger.d(TAG, "finished checkStopRecord Loop ${check.await()}")

        //완곡
        if (_isWholeSong.value) {
            DLogger.d(TAG, "완곡,구간,편집후 저장 종료")

            //완곡 이력 적재
            requestSingingCompletedHistory()

            //OFF 모드로 부르고 종료
            if (_off.value || curMediaType.value == MediaType.NONE.code) {
                delay(1000)
                moveToPage(SingPageType.OFF_FEED_COMPLETE)
            } else {
                //1분 저장시 현재 curms로 total 값 수정
                if (isSaveAndFinish.value) {
                    curTotalMs.value = tmpCurrentMs
                } else {
                    if (isSection.value) {
                        curTotalMs.value = (sectionCurEndMs.value - sectionCurStartMs.value).toDouble()
                    }
                }

                delay(200)
                //프리뷰 이동
                moveToPage(SingPageType.SYNC_SING)
            }
        } else {
            DLogger.d(TAG, "45초 종료")
            //45초
            onLoadingDismiss()
            _showNotWholeSongPopup.call()
        }
    }

    fun setIsLoading(state: Boolean) {
        _isLoading.value = state
    }

    private fun runMrFadeInOrOut(isIn: Boolean) {

        if (isIn == volumeFadeState || curInstVolume.value == 0) {
            return
        }

        val sectionIntroEventMs = (3000 / 1000)
        val targetVolume = (curInstVolume.value.toFloat() / sectionIntroEventMs.toFloat()) * 0.1
        val maxValue = curInstVolume.value

        DLogger.d(TAG, "## 볼룸 info =>  ${targetVolume} / ${maxValue}")

        closeVolumeDisposable()

        volumeFadeState = isIn


        if (isIn) {

            var curValue = 0

            //0으로 시작
            superpoweredProvider.setMrVolume(0f)

            volumeFadeDisposable = Observable.timer(1, TimeUnit.SECONDS)
                .repeat(1)
                .subscribeOn(Schedulers.io())
                .subscribe { sec ->
                    Observable.timer(50, TimeUnit.MILLISECONDS)
                        .repeat(30)
                        .map { 1 }
                        .subscribeOn(Schedulers.io())
                        .subscribe { ms ->
                            curValue += ms
                            superpoweredProvider.setMrVolume(((targetVolume * curValue) * 0.1).toFloat())
                        }
                }
        } else {

            volumeFadeDisposable = Observable.timer(1, TimeUnit.SECONDS)
                .repeat(3)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Observable.timer(100, TimeUnit.MILLISECONDS)
                        .repeat(10)
                        .subscribeOn(Schedulers.io())
                        .subscribe { ms ->

                            if (superpoweredProvider.getMrVolume() <= 0.0f) {
                                return@subscribe
                            }
                            var curVolume: Float = superpoweredProvider.getMrVolume()

                            curVolume -= (targetVolume * 0.1).toFloat()

                            if (curVolume <= 0.0f) {
                                curVolume = 0.0f
                            }

                            superpoweredProvider.setMrVolume(curVolume)

                            if (superpoweredProvider.getMrVolume() <= 0.0f) {
                                closeVolumeDisposable()
                            }
                        }.addTo(compositeDisposable)
                }
        }
    }

    private suspend fun checkStopRecordLoop(): Boolean {
        do {
            DLogger.d(TAG, " isRecordStop 체크중... ${superpoweredProvider.isRecordStop()}")
            delay(500)
        } while (!superpoweredProvider.isRecordStop())
        return true
    }

    /**
     * 이어폰 팝업
     */
    fun showEarphonePopup() {
        if (!isNetwork()) {
            return
        }
        if (!isStartRecording.value) {
            showEarphonePopup.call()
        }
    }

    /**
     * 녹음/녹화 시작
     */
    fun onPlay() {

        if (!isNetwork()) {
            return
        }

        if (!isStartRecording.value) {
            singingJob = Job() + coroutineExceptionHandler
            DLogger.d(TAG, "onPlay=>  ${isStartRecording.value} / ${curPartType.value}")
            isStartRecording.value = !isStartRecording.value
            runSinging()
        }
    }

    /**
     * 프리뷰 재생
     */
    private suspend fun syncPlayingLoop() = onIO(singingJob) {
        while (isActive) {

            if (!isSingingFinished) {
                if (tmpCurrentMs != superpoweredProvider.getCurrentMs() && !isEditing) {
                    tmpCurrentMs = superpoweredProvider.getCurrentMs()
                    updateSyncLoop(tmpCurrentMs)
                }
            } else {
                DLogger.d(TAG, "finished syncSingingLoop cancel")
                this.cancel()
            }
        }
    }

    /**
     * 프리뷰 재생 시작
     */
    private fun runSync() = onMain(singingJob) {
        launch { initForAudio() }.join()
        if (isSection.value && sectionCurStartIndex.value > 0) {
            launch { superpoweredProvider.setCurrentMs(sectionCurStartMs.value.toDouble()) }.join()
        }
        async { onPlayVideo() }
        async { onPlayVoice() }
        syncPlayingLoop().join()
        pauseSync()
        onResetSync()
    }

    suspend fun onResetSync() {
        DLogger.d("onResetSync")
        tmpCurrentMs = 0.0
        progressPersent.value = 0
        progressText.value = Pair(0, 0)
        seekToPlayer(0)
        isStartSync.value = false
        isSingingFinished = false
        cancelJob()
        superpoweredProvider.clearAll()
    }


    fun pauseSyncToggle() = onMain {
        onContinueVoiceRecord()
        onContinueExoVideo()
        setSyncPlayingState(superpoweredProvider.isPlaying())
    }

    /**
     * 프리뷰 정지
     */
    fun pauseSync() = onMain {
        onPauseVoiceRecord()
        onPauseVoiceAndVideo()
    }

    /**
     * 프리뷰 페이지 플레이 시작
     */
    fun onPlaySync(state: Boolean) {
        DLogger.d(TAG, "onPlaySync=> ${isStartSync.value} !!")
        if (!isStartSync.value) {
            isStartSync.value = true
            singingJob = Job() + coroutineExceptionHandler
            setSyncPlayingState(true)
            runSync()
        } else {
            pauseSyncToggle()
        }
    }

    /**
     * 정지
     */
    fun onStop() {
        if (!superpoweredProvider.isPlaying()) {
            DLogger.d(TAG, "sp isPaused")
            return
        }
        //인트로 카운트 완료 후 종료 가능
        finish.call()
    }

    /**
     * 초기값 Clear
     */
    fun startPrepare() {
        clearAll()
    }

    fun clearAll() {
        onMain {
            launch { onClearSing() }.join()
            launch { onClearSync() }.join()
            launch { onClearVariable() }.join()
        }
    }

    /**
     * get song info
     */
    private fun fetchSongInfo() {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        _songMngCdParam.value?.let {
            //songMngcd : 음원관리코드
            //feedMngCd  :  피드관리코드(듀엣 및 배틀 참여 인 경우만)
            apiService.fetchSongInfo(songMngCd = it, feedMngCd = _feedMngCdParam.value ?: "")
                .applyApiScheduler()
                .request({ res ->

                    if (res.httpStatus == HttpStatusType.SUCCESS.code && res.status == HttpStatusType.SUCCESS.status) {

                        res.result.let { songMainData ->

                            when (_singTypeParam.value) {

                                SingType.SOLO.code -> {
                                    //set pager list item
                                    //솔로 참여인 경우
                                    _pageList.value = run {
                                        val tmpDataList = mutableListOf<SingType>()
                                        if (songMainData.isSolo) {
                                            tmpDataList.add(SingType.SOLO)        //솔로
                                            curMediaType.value = MediaType.AUDIO.code
                                        }
                                        if (songMainData.isDuet) {
                                            tmpDataList.add(SingType.DUET)        //듀엣
                                            curMediaType.value = MediaType.AUDIO.code
                                        }
                                        //배틀 플래그 + 완곡 가능이면
                                        if (songMainData.isBattle && songMainData.isWholeSong) {
                                            tmpDataList.add(SingType.BATTLE)        //배틀
                                        }
                                        tmpDataList
                                    }
                                }

                                SingType.DUET.code,
                                SingType.BATTLE.code -> {

                                    if (_singTypeParam.value == SingType.DUET.code) {
                                        //vp 듀엣만
                                        _pageList.value = mutableListOf(SingType.DUET)

                                        if (_feedMdTpCdParam.value == MediaType.VIDEO.code) {
                                            onMediaType(MediaType.VIDEO)
                                        } else {
                                            onMediaType(MediaType.AUDIO)
                                        }

                                    } else {
                                        //vp 배틀만
                                        _pageList.value = mutableListOf(SingType.BATTLE)
                                        onMediaType(MediaType.VIDEO)
                                    }

                                    //원곡자 파트에 따라 내 파트 결정
                                    if (songMainData.singPart == PartType.PART_A.code) {
                                        onPartType(PartType.PART_B)
                                    } else {
                                        onPartType(PartType.PART_A)
                                    }
                                }

                                else -> {
                                    //일반 그룹 제외
                                    finish.call()
                                }
                            }

                            //첫번째 position Set
                            _pageList.value.let {
                                _curSingType.value = it.first()
                            }

                            //곡 메인 데이터
                            _songMainData.value = songMainData
                            //완곡 가능 여부 ( t 가능 , f 45초)
                            _isWholeSong.value = songMainData.isWholeSong
                        }

                    } else {
                        if (res.message.isNullOrEmpty()) {
                            showErrorDialog.value = SingingErrorType.FAIL_SONG_DATA
                        } else {
                            showErrorDialog.value = SingingErrorType.FAIL_SONG_DATA.apply {
                                fromServerErrMsg = res.message
                            }
                        }
                    }
                    DLogger.d("Success fetchSongInfo= > ${it}")
                }, {
                    showErrorDialog.value = SingingErrorType.FAIL_SONG_DATA
                    DLogger.d("Error fetchSongInfo= > ${it}")
                })
        }
    }

    /**
     * MR / XTF 다운로드
     */
    private fun fetchDownloadMrAndJson(songMainData: SongMainData) {

        //최초 생성 mr,xtf 파일 체크
        val checkMrXtfFiles = isAlreadyMrXtfFiles(songMainData.songId)

        DLogger.d("fetchDownloadMrAndJson ${isInitContents.value}")

//        if (isInitContents.value) {
//            if (checkMrXtfFiles.first) {
//                curMrAndXtfPath.value = checkMrXtfFiles
//                DLogger.d("fetchDownloadMrAndJson MR/XTF 이미 다운로드된 상태")
//                //                showCheckVipPopup()
//                return
//            }
//        }


        val folderPath = singPathProvider.getDirSongInfoPath() + singPathProvider.getSongId()
        val mrPath = singPathProvider.getMrFileNameMP3()
        val xtfPath = singPathProvider.getJsonFileName()
        val videoPath = singPathProvider.getOrgVideoFileName()
        val audioPath = singPathProvider.getOrgAudioFileName()

        var resMrPath = ""
        var resXtfPath = ""
        var resVideoPath = ""
        var resAudioPath = ""

        DLogger.d("DOWNLOAD==================================================")

        //START Download Files
        val mr = fileDownloadProvider.onDownload(Config.BASE_FILE_URL + songMainData.sdScPath) {
//            DLogger.d("download mr=> ${it} / ${it.hashCode()}")
        }.subscribeOn(Schedulers.io())
        val xtf = fileDownloadProvider.onDownload(Config.BASE_FILE_URL + songMainData.jsonSubPath) {
//            DLogger.d("download xtf=> ${it} / ${it.hashCode()}")
        }.subscribeOn(Schedulers.io())

        if (isInitContents.value) {

            DLogger.d("fetchDownloadMrAndJson MR / XTF만 다운로드")

            val list = mutableListOf<Pair<Int, Single<ResponseBody>>>()
            list.add(0 to mr)
            list.add(1 to xtf)

            Observable.fromIterable(list)
                .concatMap { target ->
                    target.second.delay(1, TimeUnit.SECONDS).map { target.first to it }
                        .toObservable()
                }
                .applyApiScheduler()
                .request({ response ->
                    if (response.first == 0) {
                        resMrPath = fileDownloadProvider.saveToFile(response.second, folderPath, mrPath)
                    } else if (response.first == 1) {
                        resXtfPath = fileDownloadProvider.saveToFile(response.second, folderPath, xtfPath)

                        onMain {

                            val encodeData = EncodeData(isInitContents = isInitContents.value, mrMp3Path = resMrPath, mrWavPath = singPathProvider.getMrWavPath())

                            val state = async { ffMpegProvider.onConvertMrToWav(encodeData) }

                            if (state.await()) {
                                curMrAndXtfPath.value = Triple(
                                    !resMrPath.isNullOrEmpty() && !resXtfPath.isNullOrEmpty(),
                                    resMrPath,
                                    resXtfPath
                                )
                            } else {
                                showErrorDialog.value = SingingErrorType.UNDEFINED_DOWNLOAD
                            }
                        }
//                        showCheckVipPopup()
                    }

                }, {
                    showErrorDialog.value = SingingErrorType.UNDEFINED_DOWNLOAD
                    DLogger.d("fetchDownloadMrAndJson error  ${it}")
                })

        } else {
            DLogger.d("fetchDownloadMrAndJson MR / XTF / ORG VIDEO / ORG AUDIO 다운로드")

            val orgCon = fileDownloadProvider.onDownload(Config.BASE_FILE_URL + songMainData.orgConPath) {
//                DLogger.d("download orgCon=> ${it} / ${it.hashCode()} ")
            }.subscribeOn(Schedulers.io())
            val orgAudioCon = fileDownloadProvider.onDownload(Config.BASE_FILE_URL + songMainData.audioConPath) {
//                    DLogger.d("download orgAudioCon=> ${it} / ${it.hashCode()}")
            }.subscribeOn(Schedulers.io())

            val list = mutableListOf<Pair<Int, Single<ResponseBody>>>()
            list.add(0 to mr)
            list.add(1 to xtf)
            list.add(2 to orgCon)
            list.add(3 to orgAudioCon)

            Observable.fromIterable(list)
                .concatMap { target ->
                    target.second.delay(1, TimeUnit.SECONDS).map { target.first to it }
                        .toObservable()
                }
                .applyApiScheduler()
                .request({ response ->
                    if (response.first == 0) {
                        resMrPath = fileDownloadProvider.saveToFile(response.second, folderPath, mrPath)
                    } else if (response.first == 1) {
                        resXtfPath = fileDownloadProvider.saveToFile(response.second, folderPath, xtfPath)
                    } else if (response.first == 2) {
                        resVideoPath = fileDownloadProvider.saveToFile(response.second, folderPath, videoPath)
                    } else if (response.first == 3) {
                        resAudioPath = fileDownloadProvider.saveToFile(response.second, folderPath, audioPath)


                        onMain {

                            val encodeData = EncodeData(isInitContents = isInitContents.value, orgAudioPath = resAudioPath, mrWavPath = singPathProvider.getMrWavPath())

                            val state = async { ffMpegProvider.onConvertMrToWav(encodeData) }

                            if (state.await()) {
                                curMrAndXtfPath.value = Triple(
                                    !resMrPath.isNullOrEmpty() && !resXtfPath.isNullOrEmpty(),
                                    resMrPath,
                                    resXtfPath
                                )
                                curOrgVideoAndAudioPath.value = Triple(
                                    !resVideoPath.isNullOrEmpty() && !resAudioPath.isNullOrEmpty(),
                                    resVideoPath,
                                    resAudioPath
                                )
                            }
                        }
                    }
                }, {
                    showErrorDialog.value = SingingErrorType.UNDEFINED_DOWNLOAD
                })
        }
    }

    /**
     * 유료 회원이 아니면 팝업 노출
     */
    private fun showCheckVipPopup() {
        _songMainData.value?.let {
            // 유료회원이 아니면 팝업
            if (!it.isPaid) {
                _showVipPopup.call()
                return
            }
        }
    }

    /**
     * mr/xtf 파일  체크
     * @return true / mr path / xtf path
     */
    private fun isAlreadyMrXtfFiles(songId: String): Triple<Boolean, String, String> {
        val checkMrPath = fileDownloadProvider.isFileExists(
            singPathProvider.getDirSongInfoPath() + songId,
            singPathProvider.getMrFileNameWAV()
        )
        val checkXtfPath = fileDownloadProvider.isFileExists(
            singPathProvider.getDirSongInfoPath() + songId,
            singPathProvider.getJsonFileName()
        )
        return Triple(
            !checkMrPath.isNullOrEmpty() && !checkXtfPath.isNullOrEmpty(),
            checkMrPath,
            checkXtfPath
        )
    }

    /**
     * Org audio/video 파일  체크
     * @return true / mr path / xtf path
     */
    private fun isAlreadyOrgFiles(songId: String): Triple<Boolean, String, String> {
        val checkOrgVideoPath = fileDownloadProvider.isFileExists(
            singPathProvider.getDirSongInfoPath() + songId,
            singPathProvider.getOrgVideoFileName()
        )
        val checkOrgAudioPath = fileDownloadProvider.isFileExists(
            singPathProvider.getDirSongInfoPath() + songId,
            singPathProvider.getOrgAudioFileName()
        )
        return Triple(
            !checkOrgVideoPath.isNullOrEmpty() && !checkOrgAudioPath.isNullOrEmpty(),
            checkOrgVideoPath,
            checkOrgAudioPath
        )
    }


    /**
     * MR/JSON 녹음/녹화 경로 초기화
     */
    fun initPaths() {
        _songMainData.value?.let {
            singPathProvider.clearPaths()
            singPathProvider.initSingingPath(it.songId.toString())
            //mr, xtf ,orgVideo, orgAudio get
            fetchDownloadMrAndJson(it)
        } ?: run {
            showErrorDialog.value = SingingErrorType.FAIL_SONG_DATA
        }
    }

    /**
     * Superpowered init
     */
    fun initForAudio() {
//        val mrPath = singPathProvider.getDirSongInfoPath() + "46009/temp_mr.mp3"
//        val voice = singPathProvider.getDirSingInfoPath() + "2023_05_03_09_52_42/temp_voice.wav"

        curMrAndXtfPath.value?.let {

            DLogger.d(TAG, "initForAudio")

            val isRecord = singPageType.value == SingPageType.SING_ING

            superpoweredProvider.initialize(isRecord)

//            val mr = if (isInitContents.value) {
//                //첫 생성은 MR 반주 파일로
//                it.second
//            } else {
//                //참여 인 경우 org audio를 MR로
//                curOrgVideoAndAudioPath.value.third
//            }

            superpoweredProvider.openMRFile(singPathProvider.getMrWavPath(), 0, 0)

            if (singPageType.value != SingPageType.SYNC_SING) {
                superpoweredProvider.onStartRecord(
                    singPathProvider.getTempVoicePath(),
                    singPathProvider.getTempRecordVoicePath()
                )
                setSpEnableVoice(deviceProvider.isHeadSet())
            } else {
                superpoweredProvider.openVoiceFile(singPathProvider.getResultVoicePath(), 0, 0)

                //voice sync
                if (curSyncMs.value > 0) {
                    superpoweredProvider.setCurrentVoiceMs(
                        curSyncMs.value.toDouble(),
                        sectionCurStartMs.value.toDouble()
                    )
                }
            }

            //반주 볼륨
            onChangeMRVolume(curInstVolume.value)
            //마이크 볼륨
            onChangeMikeVolume(curMicVolume.value)
            //적용된 필터
            superpoweredProvider.setFilterType(curSoundFilter.value)

            // =========================================================================================
        }
    }


    /**
     * 네트워크상태
     */
    fun setNetWorkState(state: Boolean) {
        _isNetWork.value = state
    }

    /**
     * 헤드셋 연결 상태  Set
     */
    fun setSpEnableVoice(isVoice: Boolean) {
        superpoweredProvider.setEnableVoice(isVoice)
        showPauseAfterPopup()
    }

    /**
     * 이어하기 3초 시작
     */
    private suspend fun makeCountinueEvent() {

        repeat(3) {

            if (it == 0) {
                val tmpEvent = XTF_EVENT_DTO().apply {
                    this.type = SingingCommandType.COUNT_3
                }
                currentEvent.value = tmpEvent
            }

            if (it == 1) {
                val tmpEvent = XTF_EVENT_DTO().apply {
                    this.type = SingingCommandType.COUNT_2
                }
                currentEvent.value = tmpEvent
            }
            if (it == 2) {
                val tmpEvent = XTF_EVENT_DTO().apply {
                    this.type = SingingCommandType.COUNT_1
                }
                currentEvent.value = tmpEvent
            }

            delay(1000)
        }
    }

    /**
     * 인트로 이벤트 Loop
     */
    private suspend fun singingEventLoop() = onDefault(singingJob) {
        while (isActive) {
            if (tmpCurrentMs != superpoweredProvider.getCurrentMs()) {
                tmpCurrentMs = superpoweredProvider.getCurrentMs()

                curXtfData.value?.let { xto ->
                    xto.events.let { events ->

                        currentEvent.value?.eventTime?.let { curEventTime ->

                            if (curEventTime <= tmpCurrentMs) {

                                val eventSize = events.size

                                for (eventIdx in currentEventIndex until eventSize) {

                                    if (events[currentEventIndex].eventTime <= tmpCurrentMs) {
                                        if (events[currentEventIndex].type == SingingCommandType.COUNT_0) {
                                            this.cancel()
                                        } else {
                                            if (events[currentEventIndex].eventTime == curEventTime) {
                                                currentEventIndex++
                                                break
                                            } else {
                                                onWithContextMain {
                                                    currentEvent.value = events[currentEventIndex]
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            DLogger.d(TAG, "event finish 00")
                            //정보 없음.
                            this.cancel()
                        }
                        launch { updateSingingLoop(tmpCurrentMs) }
                    }
                }
            }
        }
    }

    /**
     *  텔롭 처리 Loop
     */
    private suspend fun singingLoop() = onIO(singingJob) {
        while (isActive) {
            curXtfData.value?.let { xto ->
                xto.sections[0].let {
                    if (!isSingingFinished) {
                        //같은 currentMs 리턴
                        if (tmpCurrentMs != superpoweredProvider.getCurrentMs()) {
                            tmpCurrentMs = superpoweredProvider.getCurrentMs()
                            onLyrics(tmpCurrentMs)
                            updateSingingLoop(tmpCurrentMs)
                        }
                    } else {
                        DLogger.d(TAG, "finished record cancel")
                        this.cancel()
                    }
                }
            }

            // 2023.07.06 일부 단말에서 코루틴 Cancel 오동작 방어 코드 적용
            delay(30)
        }
    }

    /**
     * 현재 경과 시간 포멧
     */
    fun getTime(ms: Int): Pair<Int, Int> {
        val seconds = ms / 1000
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return mn to sec
    }

    suspend fun onLyrics(ms: Double) {

        curXtfData.value?.sections?.get(0)?.lyrics?.let { lyrics ->

            for (i in tmpCurrentSingingPosition until lyrics.size) {

                val data = lyrics[i]

                if (ms >= data.finishTime) {
                    if (data.textWidth != data.currentWidth) {
                        data.currentWidth = data.textWidth
                        onDraw(tmpCurrentSingingPosition, data.partType, TJ_RV_STATE.LINE_FINISHED)
                        onScroll(tmpCurrentSingingPosition + 1)
                    }
                    continue
                }

                for (j in data.events.indices.reversed()) {

                    val info = data.events[j]

                    if (info.eventTime <= ms) {

                        if (ms <= info.eventTime + info.duration) {

                            tmpCurrentSingingPosition = i

                            val t = ms.toInt() - info.eventTime
                            val ratio = t.toFloat() / info.duration.toFloat() // %
                            val newWidth = (info.startWidth + info.gapWidth * ratio).toInt()

                            if (newWidth == data.currentWidth) {
                                continue
                            }

                            if (!info.onVoice && superpoweredProvider.isVoiceOn()) {
                                info.onVoice = true
                            }

                            data.currentWidth = newWidth

                            if (isTjSong) {
                                onDraw(tmpCurrentSingingPosition, data.partType, TJ_RV_STATE.DRAW)
                            } else {
                                if (j == 0) {
                                    onDraw(
                                        tmpCurrentSingingPosition,
                                        data.partType,
                                        TJ_RV_STATE.DRAW
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 텔롭 처리
     */
    suspend fun onDraw(position: Int, partType: SingingPartType, rvState: TJ_RV_STATE) =
        onWithContextMain {
//        if (tjTextMap.containsKey(position)) {
//            tjTextMap[position]?.invalidate()
//        }
            refreshLyricsView.value = rvState to position

            if (_curSingingPartType.value != partType) {
                _curSingingPartType.value = partType
            }
        }

    /**
     * 스크롤 이동 처리
     */
    suspend fun onScroll(position: Int) = onWithContextMain {
        if (position >= SINGING_SCROLL_POSITION && scrollPosition.value != (position - 1)) {
            scrollPosition.value = position - 1
        }
    }

    /**
     * 부르기 텔롭 처리
     */
    suspend fun updateSingingLoop(ms: Double) = onWithContextMain {
        if (currentEventIndex == -1 && !isFinishIntroEvent.value) {
            curXtfData.value?.let {
                it.sections[0].lyrics.first().events.first()?.let {
                    if (ms >= it.eventTime) {
                        onFinishEvent(true)
                    }
                }
            }
        }

        if (!isSection.value) {

            val progressValue: Int = (ms / superpoweredProvider.getTotalTime() * 100).toInt()
            val remainingTime = getTime(ms.toInt())

            //일반 부르기
            if (curTotalMs.value <= 0) {
                DLogger.d(TAG, "curTotalMs.value Set ${superpoweredProvider.getTotalTime()}")
                curTotalMs.value = superpoweredProvider.getTotalTime()
            }

            //프로그래스 값
            if (progressPersent.value != progressValue) {
                progressPersent.value = progressValue
            }

            //경과 시간 00:00
            if (progressText.value != remainingTime) {
                progressText.value = remainingTime
            }

            //1분 경과 유무
            if (!isOneMinute.value) {
                isOneMinute.value = ms > ONE_MINUTE
            }

            //이력 적재
            if (!isRequestHistory) {
                requestSingingHistory(ms)
            }

            //무료,유료회원(횟수 없음) 45초 부르면 종료
            if (superpoweredProvider.isFinish() || curTotalMs.value <= (ms + 10) || (!_isWholeSong.value && ms >= FORTY_FIVE_SECONDS)) {
                onPauseRecord()
                isSingingFinished = superpoweredProvider.isFinish()
                this.cancel()
            }

        } else {
            //구간부르기

            val reMs = ms - sectionCurStartMs.value.toDouble()

            val reTotalMs = sectionCurEndMs.value - sectionCurStartMs.value

            val progressValue: Int = (reMs / reTotalMs * 100).toInt()

            val remainingTime = getTime(reMs.toInt())

            if (curTotalMs.value <= 0) {
                curTotalMs.value = reTotalMs.toDouble()
            }

            if (progressPersent.value != progressValue) {
                progressPersent.value = progressValue
            }

            if (progressText.value != remainingTime) {
                progressText.value = remainingTime
            }

            //1분 경과 유무
            if (!isOneMinute.value) {
                isOneMinute.value = reMs > ONE_MINUTE
            }

            //부르기 이력 적재 요청, 45초 경과 시 요청
            if (!isRequestHistory) {
                requestSingingHistory(reMs)
            }

            //마지막 4초 전에 볼륨값 줄임.
            if (reMs >= (reTotalMs - 4000)) {
                runMrFadeInOrOut(false)
            }

            //종료 유무
            //무료,유료회원(횟수 없음) 45초 부르면 종료
            if (reMs >= reTotalMs || (!_isWholeSong.value && reMs >= FORTY_FIVE_SECONDS)) {
                onPauseRecord()
                isSingingFinished = superpoweredProvider.isFinish()
                this.cancel()
            }
        }
    }

    /**
     * 부르기 이력 적재 요청
     * 45초 경과 시 요청
     */
    private fun requestSingingHistory(ms: Double) {
        if (ms >= FIFTEEN_SECONDS) {
            isRequestHistory = true
            _songMainData.value?.let {
                apiService.requestSingHistory(SingHistoryBody(songMngCd = it.songMngCd, paTpCd = _curSingType.value?.code!!, mdTpCd = curMediaType.value))
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { res -> DLogger.d("부르기 이력 적재=> ${res.httpStatus} / ${res.status}") }
                    .subscribe().addTo(compositeDisposable)
            }
        }
    }


    /**
     * 부르기 완료 이력 적재 요청
     * 구간/편집후 저장 제외
     */
    private fun requestSingingCompletedHistory() {

        if (isSection.value || isSaveAndFinish.value) {
            return
        }

        _songMainData.value?.let {

            val singPassYn = if (_challengeType.value == ChallengeType.CHALLENGE_SING_PASS) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }

            apiService.reqeustCompletedSong(SingHistoryBody(songMngCd = it.songMngCd, paTpCd = _curSingType.value?.code!!, mdTpCd = curMediaType.value, fgSingPassYn = singPassYn))
                .subscribeOn(Schedulers.io())
                .doOnSuccess { res -> DLogger.d("부르기 완료 이력 적재=> ${res.httpStatus} / ${res.status}") }
                .subscribe().addTo(compositeDisposable)
        }
    }

    /**
     * 프리뷰 update ui
     */
    suspend fun updateSyncLoop(ms: Double) = onWithContextMain {

        if (!isSection.value) {

            val progressValue: Int = (ms / curTotalMs.value * 100).toInt()
            val remainingTime = getTime(ms.toInt())

            if (curTotalMs.value <= 0) {
                curTotalMs.value = superpoweredProvider.getTotalTime()
            }

            if (progressPersent.value != progressValue) {
//                DLogger.d(TAG, " ####  updateSyncLoop! progressValue @#@!# =>  ${ms} / ${progressValue}")
                progressPersent.value = progressValue
            }

            if (progressText.value != remainingTime) {
                progressText.value = remainingTime
            }

            //종료 유무
            if (superpoweredProvider.isFinish() || ms >= curTotalMs.value) {
//                DLogger.d(TAG, "set UpdateSyncLoop isFinish  !!ㄹ")
                isSingingFinished = true
                this.cancel()
            }
        } else {

            val reMs = ms - sectionCurStartMs.value.toDouble()
            val progressValue: Int = (reMs / curTotalMs.value * 100).toInt()
            val remainingTime = getTime(reMs.toInt())

            if (curTotalMs.value <= 0) {
                curTotalMs.value = superpoweredProvider.getTotalTime()
            }

            if (progressPersent.value != progressValue) {
                progressPersent.value = progressValue
            }

            if (progressText.value != remainingTime) {
                progressText.value = remainingTime
            }

            //종료 유무
            if (reMs >= curTotalMs.value) {
                DLogger.d(TAG, "set UpdateSyncLoop isFinish  !! ${reMs} / ${curTotalMs.value} ")
                isSingingFinished = true
                this.cancel()
            }
        }
    }


    /**
     * 인트로 이벤트 종료 여부
     */
    private fun onFinishEvent(isFinish: Boolean) = onMain {
        isFinishIntroEvent.value = isFinish
    }

    /**
     * XTF Parsing
     */
    private fun loadXtfDto() {

        curMrAndXtfPath.value.let { it ->
            DLogger.d(TAG, "loadXtfDto")
            it.third.let { xtfPath ->
                Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }.apply {
                    Single.just(xtfPath)
                        .map { path ->
                            runCatching {
                                File(path).let { file ->
                                    FileInputStream(file).let { inputStream ->
                                        val reader =
                                            BufferedReader(InputStreamReader(inputStream)).let { buffer ->
                                                val sb = StringBuilder()
                                                var line: String? = null
                                                while (buffer.readLine()
                                                        .also { line = it } != null
                                                ) {
                                                    sb.append(line).append("\n")
                                                }
                                                buffer.close()
                                                sb.toString()
                                            }
                                        inputStream.close()
                                        reader
                                    }
                                }
                            }
                        }
                        .applyApiScheduler()
                        .subscribe({ result ->
                            result.onSuccess { json ->

                                oriXtfData.value = this.decodeFromString<XTF_DTO>(json).let {

                                    val tmpView = LayoutInflater.from(context).inflate(R.layout.item_lyrics, null, false)
                                    val tmpTjTextView: TJTextView = tmpView.findViewById(R.id.lyrics_text_view)
                                    val tempPaint = tmpTjTextView.paint
                                    tmpView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

                                    var tmpPart = SingingPartType.DEFAULT

                                    it.sections[0].lyrics.forEach { lyricsData ->

                                        //해당 파트에 따라 이미지 처리
                                        if (lyricsData.partType != SingingPartType.DEFAULT && _curSingType.value != SingType.SOLO) {

                                            //이전,다음 파트 중복시 숨김 처리
                                            if (tmpPart == null) {
                                                lyricsData.isImgPartVisible = true
                                            } else {
                                                lyricsData.isImgPartVisible = tmpPart != lyricsData.partType
                                            }

                                            //내 프로필 이미지
                                            val myProfile = _myImgProfile.value
                                            //생성자 프로필 이미지
                                            val orgProfile = _songMainData.value?.let {
                                                it.pfFrImgPath
                                            } ?: run { "" }

                                            //파트별 이미지
                                            if (curPartType.value == PartType.PART_A.code) {
                                                lyricsData.imgPartA = myProfile
                                                lyricsData.imgPartB = orgProfile
                                            } else if (curPartType.value == PartType.PART_B.code) {
                                                lyricsData.imgPartA = orgProfile
                                                lyricsData.imgPartB = myProfile
                                            }

                                            //파트별 텔롭 색상
                                            when (lyricsData.partType) {
                                                SingingPartType.MALE_PART -> {

                                                    if (isTjSong) {
                                                        lyricsData.lineColor = SingingPartType.MALE_PART.color
                                                    } else {
                                                        lyricsData.lineColor = SingingPartType.USER.color
                                                    }

                                                    if (curPartType.value == PartType.PART_B.code) {
                                                        lyricsData.lineDefaultColor = R.color.white
                                                    } else {
                                                        lyricsData.lineDefaultColor = R.color.color_707070
                                                    }
                                                }

                                                SingingPartType.FEMALE_PART -> {

                                                    if (isTjSong) {
                                                        lyricsData.lineColor = SingingPartType.FEMALE_PART.color
                                                    } else {
                                                        lyricsData.lineColor = SingingPartType.USER.color
                                                    }

                                                    if (curPartType.value == PartType.PART_A.code) {
                                                        lyricsData.lineDefaultColor = R.color.white
                                                    } else {
                                                        lyricsData.lineDefaultColor = R.color.color_707070
                                                    }
                                                }

                                                SingingPartType.T_PART -> {

                                                    if (isTjSong) {
                                                        lyricsData.lineColor = SingingPartType.T_PART.color
                                                    } else {
                                                        lyricsData.lineColor = SingingPartType.USER.color
                                                    }

                                                    lyricsData.lineDefaultColor = R.color.white
                                                }

                                                SingingPartType.DEFAULT -> {
                                                    SingingPartType.DEFAULT.color
                                                    lyricsData.lineDefaultColor = R.color.white
                                                }

                                                else -> {}
                                            }

                                            tmpPart = lyricsData.partType

                                            //첫번째 파트 Set
                                            if (_curSingingPartType.value == SingingPartType.DEFAULT) {
                                                _curSingingPartType.value = lyricsData.partType
                                            }

                                        } else {
                                            //솔로 Color
                                            if (isTjSong) {
                                                lyricsData.lineColor = SingingPartType.DEFAULT.color
                                            } else {
                                                lyricsData.lineColor = SingingPartType.USER.color
                                            }
                                            lyricsData.lineDefaultColor = R.color.white
                                        }

                                        lyricsData.textWidth = tempPaint.measureText(lyricsData.realText).toInt()

                                        val lastIndex: Int = lyricsData.events.size - 1
                                        val realText = lyricsData.realText

                                        lyricsData.events.forEachIndexed { index, eventData ->
                                            val startText = realText.substring(0, eventData.startAt)
                                            val lastText = realText.substring(0, eventData.lastAt)
                                            val startWidth = tempPaint.measureText(startText)
                                            val lastWidth = tempPaint.measureText(lastText)
                                            val gapWidth = lastWidth - startWidth
                                            eventData.startWidth = startWidth
                                            eventData.gapWidth = gapWidth
                                            if (index == lastIndex) {
                                                lyricsData.finishTime =
                                                    eventData.eventTime + eventData.duration
                                            }
                                        }
                                    }
                                    it
                                }

                                oriXtfData.value?.let {
                                    curXtfData.value = null
                                    currentEvent.value = null

                                    if (!isSection.value) {
                                        curXtfData.value = it.copy()
                                    } else {
                                        curXtfData.value = makeSectionXtf(it)
                                    }

                                    curXtfData.value?.let {
                                        //최초 이벤트 카운트 Set
                                        it.events.forEachIndexed { index, event ->
                                            if (event.type == SingingCommandType.COUNT_4
                                                || event.type == SingingCommandType.COUNT_3
                                                || event.type == SingingCommandType.COUNT_2
                                                || event.type == SingingCommandType.COUNT_1
                                            ) {
                                                if (currentEvent.value == null) {
                                                    if (event.eventTime > it.sections[0].lyrics.first().events.first().eventTime) {
                                                        currentEvent.value = XTF_EVENT_DTO(type = SingingCommandType.COUNT_4)
                                                        currentEventIndex = -1
                                                    } else {
                                                        currentEvent.value = event
                                                        currentEventIndex = index
                                                    }
                                                    return@forEachIndexed
                                                }
                                            }
                                        }

                                        resizeLyrics(it)
                                    }

                                } ?: run {
                                    DLogger.e("XTF fail ${it}")
                                }

                                DLogger.d("XTF Converted Success=>${curXtfData.value}")

                            }
                            result.onFailure {
                                DLogger.e("XTF ERROR ${it}")
                            }

                        }, {
                            DLogger.e("XTF ERROR ${it}")
                        }).addTo(compositeDisposable)
                }
            }
        }
    }

    private fun makeSectionXtf(orgXtfDto: XTF_DTO): XTF_DTO {
        return XTF_DTO(
            number = orgXtfDto.number,
            title1 = orgXtfDto.title1,
            title2 = orgXtfDto.title2,
            info1 = orgXtfDto.info1,
            info2 = orgXtfDto.info2,
            info3 = orgXtfDto.info3,
            events = if (sectionCurStartIndex.value == 0) orgXtfDto.events else getOrgEvents(),
            sections = getOrgSections(orgXtfDto)
        )
    }

    private fun getOrgEvents(): MutableList<XTF_EVENT_DTO> {

        val tEvents = mutableListOf<XTF_EVENT_DTO>()
        //3 고정
        for (i in 3 downTo 0) {
            val eventDto = XTF_EVENT_DTO()

            if (i == 3) {
                eventDto.type = SingingCommandType.COUNT_3
                eventDto.eventTime = sectionCurStartMs.value
            }
            if (i == 2) {
                eventDto.type = SingingCommandType.COUNT_2
                eventDto.eventTime = sectionCurStartMs.value + 1000
            }
            if (i == 1) {
                eventDto.type = SingingCommandType.COUNT_1
                eventDto.eventTime = sectionCurStartMs.value + 2000
            }
            if (i == 0) {
                eventDto.type = SingingCommandType.COUNT_0
                eventDto.eventTime = sectionCurStartMs.value + 3000
            }
            tEvents.add(eventDto)
        }

        return tEvents
    }

    fun getOrgSections(orgXtfDto: XTF_DTO): MutableList<XTF_SECTION_DTO> {

        val tSections = mutableListOf<XTF_SECTION_DTO>()

        tSections.add(XTF_SECTION_DTO())

        val tLyrics = mutableListOf<XTF_LYRICE_DTO>()

        for (i in sectionCurStartIndex.value until sectionCurEndIndex.value + 1) {
            DLogger.d("SECTION 가사 정보 => ${sectionCurEndIndex.value} / ${orgXtfDto.sections[0].lyrics[i].realText} ")
            tLyrics.add(orgXtfDto.sections[0].lyrics[i].copy())
        }

        tSections[0].lyrics = tLyrics

        return tSections
    }


    /**
     * 텔롭 정보 Resizing
     */
    private fun resizeLyrics(xtfDto: XTF_DTO) {

        val tmpView = LayoutInflater.from(context).inflate(R.layout.item_lyrics, null, false)
        val tmpTjTextView: TJTextView = tmpView.findViewById(R.id.lyrics_text_view)
        tmpView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        var reHeight = -1
        if (!isSection.value) {
            if (curMediaType.value == MediaType.AUDIO.code && curSingType.value != SingType.SOLO) {
                reHeight = tmpView.measuredHeight * RV_ROW_AUDIO
            } else if (curMediaType.value == MediaType.VIDEO.code) {
                reHeight = (tmpView.measuredHeight * RV_ROW_VIDEO)
            }
        } else {
            if (curMediaType.value == MediaType.AUDIO.code) {
                reHeight = tmpView.measuredHeight * xtfDto.sections[0].lyrics.size
            } else {
                reHeight = (tmpView.measuredHeight * RV_ROW_VIDEO)
            }
        }

        if (reHeight > 0) {
            refreshLyricsView.value?.let {
                if (it.second != reHeight) {
                    refreshLyricsView.value = TJ_RV_STATE.RESIZE to reHeight
                }
                onMain {
                    delay(1000)
                    onLoadingDismiss()
                }
            }
        }
    }

    /**
     * 부르기중 정지 후 팝업
     * 프리뷰 재생중 정지만
     */
    fun showPauseAfterPopup() {
        //팝업 체크
        if (showPausePopup.value == false) {
            if (singPageType.value == SingPageType.SING_ING) {
                if (isStartRecording.value) {
                    //1.정지
                    //2.팝업
                    onMain {
                        onPauseRecord()
                        DLogger.d("########  onPauseRecord after ")
                        showPausePopup(true)
                    }
                }
            } else if (singPageType.value == SingPageType.SYNC_SING) {
                //프리뷰 재생중이면 정지
                if (isStartSync.value) {
                    DLogger.d("프리뷰 재생중 정지")
                    pauseSync()
                }
            }
        }
    }

    /**
     * 정지 팝업 노출
     */
    fun showPausePopup(state: Boolean) = onMain {
        showPausePopup.value = state
    }

    /**
     * ======================================================================================
     *  새로 부르기
     */
    fun onSingAgain() {
        DLogger.d(TAG, "onSingAgain")
        prepareSong()
    }

    fun changePart() {
        moveToPage(SingPageType.PREPARE)
    }

    /**
     * ======================================================================================
     * 부르기 시작
     */
    private suspend fun onStartRecord() {
        DLogger.d(TAG, " onStartRecord")
        //sp start
        onStartVoiceRecord()
        //video start
        onStartVideoRecord()
    }


    /**
     * 녹음 시작
     */
    private suspend fun onStartVoiceRecord() {
        superpoweredProvider.togglePlayback()

    }

    /**
     * 녹화 시작
     */
    private suspend fun onStartVideoRecord() {
        //startVideoRecording()

        //듀엣 배틀 녹화 참여시
        if (!isInitContents.value && curMediaType.value == MediaType.VIDEO.code) {
            seekToPlayer(0)
            onPlayVideo()
        }
    }

    /**
     * 싱크 - 재생 시작 voice
     */
    private suspend fun onPlayVoice() {
        superpoweredProvider.togglePlayback()
    }


    /**
     * 싱크 - 재생 시작 video
     */
    private suspend fun onPlayVideo() {
        if (curMediaType.value == MediaType.VIDEO.code || !isInitContents.value) {
//            exoManager.onStartPlayer(0)

            if (curSyncMs.value > 0) {
                seekToPlayer(curSyncMs.value)
            }

            _exoView.value?.let {
                it.player?.playWhenReady = true
            }
        }
    }

    /**
     * 녹화/녹음  재생 정지
     */
    private suspend fun onPauseVoiceAndVideo() {
        //video
        if (curMediaType.value == MediaType.VIDEO.code || !isInitContents.value || singPageType.value == SingPageType.SYNC_SING) {
            _exoView.value?.player?.let {
                it.playWhenReady = false
            }
        }
    }

    private fun seekToPlayer(value: Int) {
        //video
        if (curMediaType.value == MediaType.VIDEO.code || !isInitContents.value) {
            _exoView.value?.player?.let {
                it.seekTo(value.toLong())
            }
        }
    }

    /**
     * ======================================================================================
     * 부르기 정지
     *  back press or 중지 버튼
     *  case1 정지 후 팝업
     *  case2 준비 화면 이동
     */
    fun checkPlaying() {

        when (singPageType.value) {

            SingPageType.SING_ING -> {
                if (isStartRecording.value) {
                    showPauseAfterPopup()
                } else {
                    moveToPage(SingPageType.PREPARE)
                }
            }

            else -> {
                if (singPageType.value == SingPageType.SYNC_SING) {
                    pauseSync()
                }
                showPausePopup(true)
            }
        }
    }

    /**
     * 녹화/녹음 정지
     */
    private suspend fun onPauseRecord() = onWithContextMain {
        DLogger.d("######## onPauseRecord ")
        onPauseVoiceRecord()
        onPauseVideoRecord()
    }

    /**
     * 녹음 정지
     */
    private suspend fun onPauseVoiceRecord() {
        superpoweredProvider.onPause()

        if (singPageType.value == SingPageType.SYNC_SING) {
            setSyncPlayingState(false)
        }
    }


    private fun setSyncPlayingState(state: Boolean) {
        DLogger.d("setSyncPlayingState 현재 상태 ${state}")
        if (isSyncPlayingState.value != state) {
            isSyncPlayingState.value = state
            if (state) {
                showPausePopup(false)
            }
        }
    }

    /**
     * 녹화 정지
     */
    private suspend fun onPauseVideoRecord() {
        if (!isInitContents.value && curMediaType.value == MediaType.VIDEO.code) {
            _exoView.value?.let {
                it.player?.playWhenReady = false
            }
        }
    }

    lateinit var continueSinging: Job

    fun clearContinueSinging() {
        if (::continueSinging.isInitialized) {
            continueSinging.cancel()
            onFinishEvent(true)
        }
    }

    /**
     * ======================================================================================
     * 부르기 재시작 (이어부르기)
     * 3초 후 부르기 재시작
     */
    fun onContinueSinging() {
        onMain {
            if (isFinishIntroEvent.value) {
                onFinishEvent(false)
                //3초 카운팅
                continueSinging = launch { makeCountinueEvent() }
                continueSinging.join()
                onFinishEvent(true)
                if (liftState.value) {
                    onContinueVoiceRecord()
                    onContinueExoVideo()
                    showPausePopup(false)
                }
            } else {
                if (liftState.value) {
                    onContinueVoiceRecord()
                    onContinueExoVideo()
                    showPausePopup(false)
                }
            }
        }
    }

    /**
     * 이어 부르기 재 시작 - 녹음
     */
    private suspend fun onContinueVoiceRecord() {
        superpoweredProvider.togglePlayback()
    }

    /**
     * 이어 부르기 재 시작 - 듀엣 배틀 원본
     */
    private suspend fun onContinueExoVideo() {
        if (curMediaType.value == MediaType.VIDEO.code) {
            _exoView.value?.player?.let {
                it.playWhenReady = !it.playWhenReady
            }
        }
    }


    /**
     * ======================================================================================
     * Sp 녹음 종료
     */
    private suspend fun onStopVoiceRecord() {
        try {
            DLogger.d(TAG, "finished onStopVoiceRecord ")
            if (!superpoweredProvider.isRecordStop()) {
                superpoweredProvider.onStopRecord()
                DLogger.d(TAG, "finished onStopVoiceRecord")
            }
        } catch (e: Exception) {
            DLogger.d(TAG, "finished exeption ${e}")
        }
    }

    /**
     * 사운드 필터 적용
     */
    fun onSoundFilterSelected(position: Int) {

        if (position <= -1) {
            superpoweredProvider.setFilterType(-1)
        }


        DLogger.d("onSoundFilterSelected => ${position} / ${curSoundFilter.value}")
        soundFilterDataList.forEachIndexed { index, soundFilterData ->

            if (position == index) {
                val tf = soundFilterData.isSelected?.value == false

                DLogger.d("onSoundFilterSelected tf=> ${tf} / ${position}")

                soundFilterData.isSelected?.value = tf

                if (tf) {
                    DLogger.d("onSoundFilterSelected curSoundFilter aaa => ${position}")
                    superpoweredProvider.setFilterType(position)
                    curSoundFilter.value = position
                } else {
                    DLogger.d("onSoundFilterSelected curSoundFilter bbb =>  -1")
                    superpoweredProvider.setFilterType(-1)
                    curSoundFilter.value = -1
                }
            } else {
                DLogger.d("onSoundFilterSelected curSoundFilter cccc =>  -1")
                soundFilterData.isSelected?.value = false
            }
        }
    }

    /**
     * 필터  버튼 상태
     */
    fun onEffects(state: Boolean, type: SingEffectType) {
        when (type) {
            SingEffectType.SOUND -> {
                showSoundFilter.value = state
                if (state) {
                    showVolume.value = !showSoundFilter.value
                    showSync.value = !showSoundFilter.value
                    showSection.value = !showSoundFilter.value
                    showPreview.value = !showSoundFilter.value
                }
            }

            SingEffectType.VOLUME -> {
                showVolume.value = state
                if (state) {
                    showSoundFilter.value = !showVolume.value
                    showSync.value = !showVolume.value
                    showSection.value = !showVolume.value
                    showPreview.value = !showVolume.value
                }
            }

            SingEffectType.SYNC -> {
                showSync.value = state
                if (state) {
                    showVolume.value = !showSync.value
                    showSoundFilter.value = !showSync.value
                    showSection.value = !showSync.value
                    showPreview.value = !showSync.value
                }
            }

            SingEffectType.SECTION -> {
                showSection.value = state
                if (state) {
                    showSync.value = !showSection.value
                    showVolume.value = !showSection.value
                    showSoundFilter.value = !showSection.value
                    showPreview.value = !showSection.value
                }
            }

            SingEffectType.PREVIEW -> {
                showPreview.value = state
                if (state) {
                    showSync.value = !showPreview.value
                    showVolume.value = !showPreview.value
                    showSoundFilter.value = !showPreview.value
                    showSection.value = !showPreview.value
                }
            }

            else -> {
                showSoundFilter.value = false
                showVolume.value = false
                showSync.value = false
                showSection.value = false
                showPreview.value = false
            }
        }
    }

    /**
     * 싱크 설정
     */
    fun onChangeSync(isLeft: Boolean) {
        if (superpoweredProvider.isPlaying()) {
            onMain {
                onPauseVoiceRecord()
            }
        }
        if (curMediaType.value == MediaType.VIDEO.code) {
            _exoView.value?.player?.let {
                if (it.isPlaying) {
                    onMain {
                        onPauseVoiceAndVideo()
                    }
                }
            }
        }
        if (isLeft) {
            if (curSyncMs.value <= 0) return
            curSyncMs.value = curSyncMs.value.minus(SYNC_GAP)
        } else {
            if (curSyncMs.value >= SYNC_MAX) return
            curSyncMs.value = curSyncMs.value.plus(SYNC_GAP)
        }
        superpoweredProvider.setCurrentVoiceMs(
            curSyncMs.value.toDouble(),
            sectionCurStartMs.value.toDouble()
        )
    }


    /**
     * 프리뷰 설정
     */
    fun onChangePreview() {

        curPreViewMs.value = run {

            var tmpMs = tmpCurrentMs

            if (sectionCurStartMs.value > 0) {
                tmpMs -= sectionCurStartMs.value
            }

            if ((tmpMs + PREVIEW_GAP) > curTotalMs.value) {
                curTotalMs.value - PREVIEW_GAP
            } else {
                tmpMs
            }
        }
        curStartPreview.value = getTime(curPreViewMs.value.toInt())
        curEndPreview.value = getTime((curPreViewMs.value + PREVIEW_GAP).toInt())
    }

    /**
     * SeekBar Playing Duration
     */
    fun onChangePlayingDuration(progress: Int) = onMain {

        val changeCurrentMs = progress.toFloat() / 100.0 * curTotalMs.value

        seekToPlayer(changeCurrentMs.toInt() + curSyncMs.value)
        progressText.value = getTime(changeCurrentMs.toInt())

        if (sectionCurStartMs.value > 0 && sectionCurStartIndex.value > 0) {
            tmpCurrentMs = changeCurrentMs + sectionCurStartMs.value
        } else {
            tmpCurrentMs = changeCurrentMs
        }
        superpoweredProvider.setCurrentMrAndVoiceMs(tmpCurrentMs, changeCurrentMs)

        DLogger.d(
            TAG,
            "onChangePlayingDuration Result=> ${progress} /  ${sectionCurStartMs.value} / ${tmpCurrentMs} / ${changeCurrentMs}  / ${curTotalMs.value} / ${
                getTime(changeCurrentMs.toInt())
            }"
        )

    }

    /**
     * MR 볼륨 조절
     */
    fun onChangeMRVolume(progress: Int) {
        DLogger.d(TAG, "onChangeMRVolume ${(progress * 0.1).toFloat()}")
        superpoweredProvider.setMrVolume((progress * 0.1).toFloat())
        curInstVolume.value = progress
    }


    /**
     * 마이크 볼륨 조절
     */
    fun onChangeMikeVolume(progress: Int) {
        DLogger.d(TAG, "onChangeMikeVolume ${(progress * 0.1).toFloat()}")
        superpoweredProvider.setVoiceVolume((progress * 0.1).toFloat())
        curMicVolume.value = progress
    }

    /**
     * 스코어 계산
     */
    private fun getScore(): Int {

        var eventCount = 0
        var voiceCount = 0

        curXtfData.value?.let {
            it.sections[0].lyrics.forEach {
                it.events.forEach {
                    eventCount++
                    if (it.onVoice) {
                        voiceCount++
                    }
                }
            }

            // 30, 50, 75
            val percent = voiceCount.toFloat() / eventCount.toFloat()

            var score = if (percent < 0.3) {
                0
            } else if (percent < 0.5) {
                1
            } else if (percent < 0.75) {
                2
            } else {
                3
            }

            DLogger.d(TAG, " 스코어 계산 => ${score}")
            return score
        }
        return 0
    }

    /**
     * 부르기 정보 초기화
     */
    suspend fun onClearSing() {

        //부르기 job
        cancelJob()
        //가사 스크롤 포지션
        scrollPosition.value = -1
        //tmp 스크롤 포지션
        tmpCurrentSingingPosition = 0
        //현재 ms
        tmpCurrentMs = 0.0
        //프로그래스 진행률
        progressPersent.value = 0
        //경과 시간
        progressText.value = 0 to 0
        //이벤트 default 포지션
        currentEventIndex = 0
        //현재 이벤트
        currentEvent.value = null
        //인트로 이벤트 종료 여부
        isFinishIntroEvent.value = false
        showPausePopup.value = false
        //종료 여부
        isSingingFinished = false
        //1분 경과 유무
        isOneMinute.value = false
        //인트로 종료 여부
        onFinishEvent(false)
        //부르기 시작 여부
        isStartRecording.value = false

        //텔롭 대상 view map
        tjTextMap.clear()
        refreshLyricsView.value = TJ_RV_STATE.REMOVE to -1
        //sp clear
        superpoweredProvider.clearAll()
        //video stop
        _exoView.value?.let {
            it.player?.let {
                it.seekTo(0)
                it.playWhenReady = false
            }
        }
        //진행중인 파트
        _curSingingPartType.value = SingingPartType.DEFAULT

        //필터 숨김
        onEffects(false, SingEffectType.NONE)

        if (singPageType.value != SingPageType.SYNC_SING) {
            curTotalMs.value = 0.0
        }

        //구간 부르기 시작/끝 볼륨
        closeVolumeDisposable()


        DLogger.d(TAG, "onClearSing")
    }

    private fun cancelJob() {
        if (::singingJob.isInitialized) {
            singingJob.cancel()
        }
    }

    /**
     * 프리뷰 화면 진입 시 초기화
     */
    private suspend fun onClearSync() {
        //사운드 필터 초기화
        //onSoundFilterSelected(-1)
        //싱크 설정값
        curSyncMs.value = 0
        //프리뷰 ms 값
        curPreViewMs.value = 0.0
        //start 프리뷰
        curStartPreview.value = 0 to 0
        //end 프리뷰
        curEndPreview.value = 0 to (PREVIEW_GAP / 1000)

        onClearPlayer()
        DLogger.d(TAG, "onClearSync")
    }

    private fun onClearPlayer() {
        _exoView.value?.let { exoView ->
            exoView.player?.let { player ->
                player.removeListener(mPlayerEventListener)
                player.release()
            }
            exoView.player = null
        }
    }

    /**
     * 기타 데이터 초기화
     */
    suspend fun onClearVariable() {
        DLogger.d(TAG, "onClearVariable")
        //vp position
        currentPos.value = 0
        //현재 부르기 데이터
        curXtfData.value = null
        //편집 후 저장 여부
        isSaveAndFinish.value = false

        //sp 음성 필터
        if (curSoundFilter.value > -1) {
            soundFilterDataList.forEach {
                it.isSelected?.value = false
            }
            curSoundFilter.value = -1
        }
        //MR 볼륨값
        curInstVolume.value = 5
        //마이크 볼륨값
        curMicVolume.value = 5
        //pause 팝없
        showPausePopup.value = false
        //곡 정보 보기
        songInfoState.value = false

        //현재 솔로/듀엣/배틀 구분
        if (_pageList.value.isNotEmpty() && _pageList.value.size > 0) {
            _curSingType.value = _pageList.value.first()
        }

//        //현재 녹음/녹화/off
//        curMediaType.value = MediaType.NONE.code
//        //A or B Part
//        curPartType.value = ""
//        //구간/전곡/A,B파트/씽패스도전/씽패스 A,B 파트 도전
        onClearSingSelectedVariable()
    }

    /**
     * Vp 위치 변경시 선택값 초기화
     */
    suspend fun onClearSingSelectedVariable() {
        DLogger.d(TAG, "onClearSingSelectedVariable")

        //듀엣 배틀 참여로 들어온 경우 초기화  X
        if (_singTypeParam.value == SingType.SOLO.code) {
            //현재 녹음/녹화/off
//            curMediaType.value = MediaType.NONE.code
            //A or B Part
            curPartType.value = ""
        }
        _off.value = false
        //구간 설정 off
        isSection.value = false
        //구간 시작 ms 초기화
        sectionCurStartMs.value = 0
        //구간 종료 ms 초기화
        sectionCurEndMs.value = 0
        //구간 시작 index
        sectionCurStartIndex.value = 0
        //구간 끝 index
        sectionCurEndIndex.value = 0
        //부르기 이력 적재 요청 플래그
        isRequestHistory = false
    }

    fun onExitSing() {
        onMain {
            if (superpoweredProvider.isPlaying()) {
                onStopVoiceRecord()
            }
            superpoweredProvider.clearAll()
            onClearPlayer()
            cancelJob()

            DLogger.d("onExitSing")
        }
    }

    fun toggleLyricsBlur() {
        if (curSingType.value == SingType.SOLO) {
            toggleLyricsBlur.call()
        }
    }

    /**
     * 연관 음원 이동
     */
    fun moveToRelatedSoundSource() {
        _songMainData.value?.let {
            moveToRelatedSoundSource.value = it.songMngCd
        }
    }

    /**
     * 구간 설정 완료
     */
    override fun onSectionConfirm(
        sectionIndexInfo: Pair<Int, Int>,
        sectionTimeInfo: Pair<Int, Int>
    ) {
        DLogger.d("SECTION onSection 구간 시작: ${sectionIndexInfo.first}/ ${sectionTimeInfo.first} // 구간 끝 : ${sectionIndexInfo.second} / ${sectionTimeInfo.second}")
        DLogger.d("SECTION onSection 구간 시작 시간 : ${getTime(sectionTimeInfo.first)} / ${getTime(sectionTimeInfo.second)}")
        if (sectionIndexInfo.first >= 0 && sectionIndexInfo.second >= 0 && sectionTimeInfo.first >= 0 && sectionTimeInfo.second > 0) {
            isSection.value = true
            sectionCurStartIndex.value = sectionIndexInfo.first
            sectionCurEndIndex.value = sectionIndexInfo.second
            sectionCurStartMs.value = sectionTimeInfo.first
            sectionCurEndMs.value = sectionTimeInfo.second
            showSection.value = false

            if (singPageType.value == SingPageType.SING_ING) {
                onLoadingShow()
                loadXtfDto()
            } else {
                moveToPage(SingPageType.SING_ING)
            }
        }
    }

    /**
     * intro/finish 볼륨
     */
    private fun closeVolumeDisposable() {
        if (volumeFadeDisposable != null) {
            volumeFadeDisposable?.dispose()
            volumeFadeDisposable = null
        }
        volumeFadeState = false
    }

    fun initPlayer(pv: ExoStyledPlayerView, url: String) {
        DLogger.d(TAG, "initPlayer ${url}")
        if (url.isEmpty()) return
        val renderersFactory: RenderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        val player = ExoPlayer.Builder(context).setRenderersFactory(renderersFactory).build()
        player.addListener(mPlayerEventListener)
        player.setMediaSource(buildLocalMediaSource(context, url))
        player.volume = 0f
        pv.player = player
        player.prepare()
        _exoView.value = pv
    }

    private fun buildLocalMediaSource(ctx: Context, url: String): MediaSource {
        return ProgressiveMediaSource.Factory(DefaultDataSource.Factory(ctx))
            .createMediaSource(MediaItem.fromUri(url))
    }

    private val mPlayerEventListener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                //준비
                Player.STATE_BUFFERING -> {
                    DLogger.d("Player init STATE_BUFFERING")
                }
                //준비완료
                Player.STATE_READY -> {
                    DLogger.d("Player init STATE_READY")
                }
                //재생X
                Player.STATE_IDLE -> {
                    DLogger.d("Player init STATE_IDLE")
                }
                //재생 끝
                Player.STATE_ENDED -> {
                }
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (playWhenReady) {
                DLogger.d("Player init onPlayWhenReadyChanged ${playWhenReady}")
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            DLogger.d("Player init onPlayerError ${error}")
        }
    }

    fun isNetwork(): Boolean {
        if (_isNetWork.value == false) {
            showToastIntMsg.value = R.string.str_network_disconnect_msg
        }
        return _isNetWork.value == true
    }

    fun setLifeState(state: Boolean) {
        liftState.value = state
    }
}