package com.verse.app.ui.bottom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.contants.BottomNaviType
import com.verse.app.contants.NaviType
import com.verse.app.contants.ProgressVideoType
import com.verse.app.contants.UploadProgressAudio
import com.verse.app.contants.UploadProgressState
import com.verse.app.contants.UploadProgressVideo
import com.verse.app.databinding.ViewNavigationBarBinding
import com.verse.app.extension.changeVisible
import com.verse.app.extension.getFragmentAct
import com.verse.app.extension.initBinding
import com.verse.app.extension.isServiceRunning
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.SongEncodeService
import com.verse.app.utility.moveToLoginAct
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.internal.toHexString
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : Bottom Navi LinearLayout
 *
 * Created by jhlee on 2023-03-01
 */
@AndroidEntryPoint
class BottomNavigationView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(ctx, attrs, defStyleAttr),
    LifecycleOwner,
    LifecycleEventObserver {

    companion object {
        const val CLICK_INTERVAL = 800
    }

    @Inject
    lateinit var deviceProvider: DeviceProvider

    @Inject
    lateinit var loginManager: LoginManager

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    private var _binding: ViewNavigationBarBinding? = null
    val binding: ViewNavigationBarBinding get() = _binding!!
    private val _selectPage: MutableLiveData<NaviType> by lazy { MutableLiveData(NaviType.MAIN) }
    val selectPage: LiveData<NaviType> get() = _selectPage

    private var progressDisposable: Disposable? = null
    private var audioProgress = 0.0F
    private var videoOriginProgress = 0.0F
    private var videoHighlightProgress = 0.0F
    private var videoUploadProgress = 0.0F
    private var lastClickedTime: Long = 0L

    init {
        if (!isInEditMode) {
            _binding = initBinding<ViewNavigationBarBinding>(BottomNaviType.DEFAULT.id, this) {
                setVariable(BR.bottomNaviView, this@BottomNavigationView)
            }
        }

        initProgressRxBusEvent()
    }

    /**
     * 프로그래스 업로드 Event 초기화 함수
     */
    private fun initProgressRxBusEvent() {
        progressDisposable = RxBus.listen(RxBusEvent.SingUploadProgressEvent::class.java)
            .toFlowable(BackpressureStrategy.BUFFER)
            .map { setPreviewMergeProgress(it) }
            .debounce(10, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { handleUploadProgressEvent(it) }.subscribe()
    }

    /**
     *  메인
     */
    fun onMain() {
        if (isSafe()) {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.MAIN))
        }
        lastClickedTime = System.currentTimeMillis()
    }

    /**
     *  씽패스
     */
    fun onSingPass() {
        if (isSafe()) {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.SING_PASS))
        }
        lastClickedTime = System.currentTimeMillis()
    }

    /**
     *  부르기
     */
    fun onSing() {
        if (!context.isServiceRunning(SongEncodeService::class.java) && !AppData.IS_ENCODE_ING) {
            if (isSafe()) {
                checkLoginAfterMoveToAct(NaviType.SING)
            }
        } else {
            Toast.makeText(context, R.string.encode_ing_msg, Toast.LENGTH_SHORT).show()
        }
        lastClickedTime = System.currentTimeMillis()
    }

    /**
     *  커뮤니티
     */
    fun onCommunity() {
        if (isSafe()) {
            RxBus.publish(RxBusEvent.NaviEvent(NaviType.COMMUNITY))
        }
        lastClickedTime = System.currentTimeMillis()
    }

    /**
     *  마이
     */
    fun onMy() {
        if (isSafe()) {
            checkLoginAfterMoveToAct(NaviType.MY)
        }
        lastClickedTime = System.currentTimeMillis()
    }

    /**
     * 비로그인 -> 로그인 이동
     * 로그인 -> 화면 이동
     */
    private fun checkLoginAfterMoveToAct(type: NaviType) {
        if (!loginManager.isLogin()) {
            context.getFragmentAct()?.moveToLoginAct()
        } else {
            RxBus.publish(RxBusEvent.NaviEvent(type))
        }
    }

    /**
     *  현재 화면
     */
    fun setSelectPage(type: NaviType) {
        _selectPage.value = type
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)

        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                closeDisposable()
            }

            else -> {
            }
        }
    }


    /**
     * Bottom height
     */
    fun naviHeight(): Int {
        binding.naviBodyLinearLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        return binding.naviBodyLinearLayout.measuredHeight
    }

    override fun getLifecycle() = lifecycleRegistry

    private fun closeDisposable() {
        if (progressDisposable != null) {
            progressDisposable?.dispose()
            progressDisposable = null
        }
    }

    /**
     * 각 타입별 진행상황 변수들 초기화 함수
     */
    private fun initProgressPoint() {
        audioProgress = 0.0F
        videoOriginProgress = 0.0F
        videoHighlightProgress = 0.0F
        videoUploadProgress = 0.0F
    }

    /**
     * UI 업데이트 하기전 데이터 먼저 셋팅 하기
     */
    private fun setPreviewMergeProgress(event: RxBusEvent.SingUploadProgressEvent): RxBusEvent.SingUploadProgressEvent {
        if (event is UploadProgressAudio) {
            if (event.state == UploadProgressState.PROGRESS) {
                audioProgress = event.progress
            } else {
                audioProgress = 100.0F
            }
            DLogger.d("Preview UploadProgress ${event.progress}")
        } else if (event is UploadProgressVideo) {
            if (event.state == UploadProgressState.PROGRESS) {
                when (event.type) {
                    ProgressVideoType.ORIGIN -> {
                        videoOriginProgress = event.getRelativePercent()
                    }

                    ProgressVideoType.HIGHLIGHT -> {
                        videoHighlightProgress = event.getRelativePercent()
                    }

                    ProgressVideoType.UPLOAD -> {
                        videoUploadProgress = event.getRelativePercent()
                    }
                }
                DLogger.d("Preview UploadProgress ${event.type} ${videoOriginProgress.plus(videoHighlightProgress).plus(videoUploadProgress)}")
            } else {
                videoOriginProgress = 100.0F
                videoHighlightProgress = 100.0F
                videoUploadProgress = 1000.0F
            }
        }
        return event
    }

    /**
     * 영상 및 녹음 업로드시 ProgressBar UI 업데이트 처리하는 함수
     * @param event 영상 및 녹음 업로드시 이벤트 받는 데이터 모델
     */
    private fun handleUploadProgressEvent(
        event: RxBusEvent.SingUploadProgressEvent
    ) {
        // DLogger.d("UploadProgressEvent $event")
        if (event.state == UploadProgressState.PROGRESS) {
            if (event is UploadProgressAudio) {
                setUploadProgressUi(audioProgress)
            } else if (event is UploadProgressVideo) {
                val mergeProgress = videoOriginProgress
                    .plus(videoHighlightProgress)
                    .plus(videoUploadProgress)
                setUploadProgressUi(mergeProgress)
            }
        } else {
            // END
            initProgressPoint()
            handleProgressEndAniStep1()
        }
    }

    /**
     * 프로그래스바 UI 업데이트 함수
     * @param progress 0.0 ~ 100.0
     */
    private fun setUploadProgressUi(progress: Float) {
        val currProgress = binding.pbUpload.progress
        if (currProgress <= progress.toInt()) {
            binding.pbUpload.progress = progress.toInt()
            if (progress >= 99.0) {
                binding.tvUpload.text = String.format("%d", 100)
            } else {
                binding.tvUpload.text = String.format("%.1f", progress)
            }
        }

        // View Visible Update
        binding.ivSingPlus.changeVisible(false)
        binding.pbUpload.changeVisible(true)
        binding.tvUpload.changeVisible(true)
        binding.tvUploadPercent.changeVisible(true)
    }

    /**
     * 프로그래스가 100 완료되면 0.3 초간 ProgressBar 알파 20%-100% 변경하는 애니메이션
     */
    private fun handleProgressEndAniStep1() {
        val layoutDrawable = binding.pbUpload.progressDrawable as LayerDrawable
        val rotateDrawable = layoutDrawable
            .findDrawableByLayerId(android.R.id.progress) as RotateDrawable
        val progressDrawable = rotateDrawable.drawable as GradientDrawable
        // Alpha 20% ~ 100%
        ValueAnimator.ofInt(51, 255).apply {
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            doOnStart { binding.pbUpload.progress = 100 }
            addUpdateListener {
                val value = it.animatedValue as Int
                val alphaColor = "#${converterAlphaToHex(value)}FFFFFF"
                progressDrawable.setColor(Color.parseColor(alphaColor))
            }
            doOnEnd { handleProgressAniStep2(progressDrawable) }
            start()
        }
    }

    /**
     * 0.5 초 딜레이 후 0.2 초간 percent Text 및 progress alpha 값이 0 바뀌고 히든 처리
     */
    private fun handleProgressAniStep2(
        progressDrawable: GradientDrawable
    ) {
        ValueAnimator.ofInt(100, 0).apply {
            duration = 200
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                val alphaColor = "#${converterAlphaToHex(value)}FFFFFF"
                progressDrawable.setColor(Color.parseColor(alphaColor))
                binding.tvUpload.alpha = value.toFloat() / 100
                binding.tvUploadPercent.alpha = value.toFloat() / 100
            }
            doOnEnd {
                binding.ivSingPlus.changeVisible(true)
                binding.pbUpload.changeVisible(false)
                binding.tvUpload.changeVisible(false)
                binding.tvUploadPercent.changeVisible(false)
                // 애니메이션 끝나면
                initProgressBar(progressDrawable)
            }
            startDelay = 500
            start()
        }
    }

    private fun initProgressBar(
        progressDrawable: GradientDrawable
    ) {
        progressDrawable.setColor(Color.parseColor("#33FFFFFF"))
        binding.tvUpload.alpha = 1.0F
        binding.tvUploadPercent.alpha = 1.0F
        binding.pbUpload.progress = 0
    }

    /**
     * @param alpha 0 ~ 255
     */
    private fun converterAlphaToHex(alpha: Int): String {
        var hexCode = alpha.toHexString()
        if (hexCode.length == 1) {
            hexCode = "0".plus(hexCode)
        }
        return hexCode.uppercase()
    }

    private fun isSafe() = System.currentTimeMillis() - lastClickedTime > CLICK_INTERVAL
}