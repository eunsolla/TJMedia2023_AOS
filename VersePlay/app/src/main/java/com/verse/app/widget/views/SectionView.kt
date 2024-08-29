package com.verse.app.widget.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.databinding.ViewSectionBinding
import com.verse.app.extension.initBinding
import com.verse.app.extension.onMain
import com.verse.app.model.xtf.XTF_DTO
import com.verse.app.model.xtf.XTF_SECTION_DTO
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.roundToInt


/**
 * Description : 구간설정
 *
 * Created by jhlee on 2023-05-28
 */
@AndroidEntryPoint
class SectionView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr), LifecycleOwner, LifecycleEventObserver {

    companion object {
        const val TAG = "SectionView"
    }

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    private var _binding: ViewSectionBinding? = null
    val binding: ViewSectionBinding get() = _binding!!

    //가사 정보
    private val _lyricsData: MutableLiveData<XTF_DTO> by lazy { MutableLiveData() }
    val lyricsData: LiveData<XTF_DTO> get() = _lyricsData

    private val _beforeIndexInfo: MutableLiveData<Pair<Int, Int>> by lazy { MutableLiveData() }
    val beforeIndexInfo: LiveData<Pair<Int, Int>> get() = _beforeIndexInfo

    private val _isShow: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isShow: LiveData<Boolean> get() = _isShow


    private val START_END_PLUS_MS = 3000
    val MINIMUM_MS = 15000
    private val DEFAULT_START_LINE = 0
    private val DEFAULT_END_LINE = 5
    private var mCurStartPosition = DEFAULT_START_LINE
    private var mCurEndPosition = DEFAULT_END_LINE
    private var mMaxPosition = 0
    private var mMaxHeight = 0
    private var mLineHeight = 0
    private var mLineHeight_half = 0
    private var mStartOldY = 0
    private var mEndOldY = 0
    private var isStartDrag = false
    private var isEndDrag = false
    private var isActionDownStart = false
    private var isActionDownEnd = false

    @Inject
    lateinit var deviceProvider: DeviceProvider

    init {
        _binding = initBinding(R.layout.view_section, this) {
            setVariable(BR.sectionView, this@SectionView)
        }
    }

    /**
     * Set Data
     */
    fun setLyricsData(xtfDto: XTF_DTO, beforeIndexInfo: Pair<Int, Int>) {
        _lyricsData.value = xtfDto
        _beforeIndexInfo.value = beforeIndexInfo
        _isShow.value = true
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                onMain {
                    delay(100)
                    preInfo()
                }
            }

            Lifecycle.Event.ON_RESUME -> {}
            Lifecycle.Event.ON_PAUSE -> {}
            Lifecycle.Event.ON_STOP -> {}
            Lifecycle.Event.ON_DESTROY -> {}
            else -> {}
        }
    }

    override fun getLifecycle() = lifecycleRegistry


    /**
     * 초기 데이터 Set
     */
    private fun preInfo() = onMain {
        _binding?.let {
            it.tvSection.post {
                mLineHeight = it.tvSection.lineHeight
                mLineHeight_half = it.tvSection.lineHeight / 2
                it.tvSection.updateLayoutParams<LayoutParams> {
                    topMargin = mLineHeight_half
                    bottomMargin = mLineHeight_half
                }
                mMaxHeight = it.tvSection.height
                mMaxPosition = it.tvSection.lineCount

                _beforeIndexInfo.value?.let { bp ->
                    DLogger.d(TAG, "_beforeIndexInfo=> ${bp.first} /  ${bp.second}")
                    if (bp.first == 0 && bp.second == 0) {
                        setDefaultPosition()
                    } else {
                        if (bp.first > 0) {
                            mCurStartPosition = bp.first
                        }
                        if (bp.second > 0) {
                            mCurEndPosition = bp.second + 1
                        }
                    }
                }

                setLine(it.clStart, mCurStartPosition, mLineHeight, true)
                setLine(it.clEnd, mCurEndPosition, mLineHeight, false)
//                it.vEnBg.layoutParams.height = mMaxHeight - it.clEnd.y.toInt()
                it.clStart.setOnTouchListener(mStartLineTouchListener)
                it.clEnd.setOnTouchListener(mEndLineTouchListener)
                initScroll()

                onMain {
                    val animation: Animation = AlphaAnimation(0f, 1f)
                    animation.duration = 200
                    binding.vSelected.visibility = VISIBLE
                    binding.clStart.visibility = VISIBLE
                    binding.clEnd.visibility = VISIBLE
                    binding.clStart.animation = animation
                    binding.clEnd.animation = animation
                    binding.vSelected.animation = animation
                }

            }

        }
    }

    /**
     * 기본 15초 이상
     */
    private fun setDefaultPosition() {
        _lyricsData.value?.let {

            val section: XTF_SECTION_DTO = it.sections[0]
            if (!section.lyrics.isNullOrEmpty()) {

                val firstLyricsLastEvent = getFirstLyricsFirstEvent()

                section.lyrics.forEachIndexed { index, dto ->
                    if (((dto.events.last().duration + dto.events.last().eventTime) - firstLyricsLastEvent) >= MINIMUM_MS) {
                        mCurEndPosition = index + 1
                        DLogger.d(TAG, "구간 가본 end position=> $mCurEndPosition")
                        return
                    }
                }
            }
        }
    }

    /**
     * 초기 스크롤 위치
     */
    private fun initScroll() {
        //Scrollbar 초기 위치
        if (mCurStartPosition > 0) {
            var scrollPosition = mCurStartPosition - 1
            if (scrollPosition < 0) {
                scrollPosition = 0
            }
            val resultScrollY = (mLineHeight * scrollPosition) + mLineHeight_half
            binding.sv.scrollTo(0, resultScrollY)
        }
    }

    /**
     * 라인 초기값
     */
    private fun setLine(
        view: ConstraintLayout,
        position: Int,
        lineHeight: Int,
        isStartLine: Boolean
    ) {

        val resultY = lineHeight * position

        DLogger.d(TAG, "resultY.toFloat() ${resultY.toFloat()}/ ${position}")

        if (isStartLine) {
            view.y = resultY.toFloat()
            mStartOldY = resultY
            setBgStart(view.y)
        } else {
            view.y = (resultY.toFloat() - mLineHeight_half.toFloat())
            mEndOldY = resultY
            setBgEnd(resultY.toFloat())
        }
    }

    /**
     * Start
     */
    @SuppressLint("ClickableViewAccessibility")
    private var mStartLineTouchListener: OnTouchListener = OnTouchListener { v, event ->

        if (isActionDownEnd && !isEndDrag) {
            return@OnTouchListener true
        }
        if (event.action == MotionEvent.ACTION_DOWN) {             //누름.
            isActionDownStart = true
            //스크롤뷰 스크롤 막음 처리
            binding.sv.setShouldStopInterceptingTouchEvent(true)
        } else if (event.action == MotionEvent.ACTION_MOVE) {

            //첫번째
            if (((v.y + event.y) - v.height / 2) <= 0) {
                v.y = 0f
                setBgStart(0f)
                return@OnTouchListener true
            }

            //마지막
            if (((v.y + event.y) - (v.height / 2)) >= mMaxHeight - mLineHeight - mLineHeight_half) {
                setBgStart((mMaxHeight - mLineHeight - mLineHeight_half).toFloat())
                v.y = (mMaxHeight - mLineHeight - mLineHeight_half).toFloat()
                return@OnTouchListener true
            }

            // 라인
            v.y += event.y
            setBgStart(v.y)

            if (v.y >= (binding.clEnd.y - mLineHeight) && !isEndDrag) {
                isStartDrag = true
                isEndDrag = false
                binding.clEnd.dispatchTouchEvent(event)
            }

        } else if (event.action == MotionEvent.ACTION_UP) {         //뗌.
            if (v.y <= 0) {
                v.y = 0f
                mCurStartPosition = 0
                mStartOldY = 0
                setBgStart(0f)
            } else {
                //살짝 이동 시 원래 위치로 보정.
                var gap = if (mStartOldY < v.y) {
                    //내림
                    (v.y - mStartOldY).toInt()
                } else {
                    //올림
                    (mStartOldY - v.y).toInt()
                }

                if (mLineHeight_half > gap) {
                    v.y = mStartOldY.toFloat()
                    setBgStart(mStartOldY.toFloat())
                } else {
                    // 해당 위치로 이동.
                    var pos = getPosition(v.y)
                    if (pos >= mMaxPosition - 1) {
                        pos = mMaxPosition - 1
                    }
                    if (pos > 0) {
                        val resultY = mLineHeight * pos
                        v.y = resultY.toFloat()
                        mCurStartPosition = pos
                        mStartOldY = resultY
                        setBgStart(resultY.toFloat())
                    }
                }
            }
            correctFromStart()
            binding.sv.setShouldStopInterceptingTouchEvent(false)
            isStartDrag = false
            isEndDrag = false
            isActionDownStart = false
        } else if (event.action == MotionEvent.ACTION_CANCEL) {
            binding.sv.setShouldStopInterceptingTouchEvent(false)
        }
        true
    }

    /**
     * End
     */
    @SuppressLint("ClickableViewAccessibility")
    private val mEndLineTouchListener: OnTouchListener = OnTouchListener { v, event ->
        if (isActionDownStart && !isStartDrag) {
            return@OnTouchListener true
        }
        if (event.action == MotionEvent.ACTION_DOWN) {             //누름.
            isActionDownEnd = true
            //스크롤뷰 스크롤 막음 처리
            binding.sv.setShouldStopInterceptingTouchEvent(true)
        } else if (event.action == MotionEvent.ACTION_MOVE) {

            if ((v.y + event.y) - (v.height / 2) <= mLineHeight) {
                v.y = mLineHeight.toFloat()
                setBgEnd(v.y)
                return@OnTouchListener true
            }

            if (((v.y + event.y) - (v.height / 2)) >= (mLineHeight * mMaxPosition)) {
                setBgEnd((mLineHeight * mMaxPosition + mLineHeight_half).toFloat())
                v.y = (mLineHeight * mMaxPosition).toFloat()
                return@OnTouchListener true
            }

            // 라인
            v.y += event.y

            //bottom bg
            setBgEnd(v.y)


            if (((binding.clStart.y + mLineHeight) >= v.y) && !isStartDrag) {
                isStartDrag = false
                isEndDrag = true
                binding.clStart.dispatchTouchEvent(event)
            }
        } else if (event.action == MotionEvent.ACTION_UP) {         //뗌.

            //살짝 이동시 원래 위치로 보정.
            var gap = if (mEndOldY < v.y) {
                //내림
                (v.y - mEndOldY).toInt()
            } else {
                //올림
                (mEndOldY - v.y).toInt()
            }
            if (mLineHeight_half > gap) {
                v.y = mEndOldY.toFloat()
                setBgEnd(v.y)
            } else {
                //이동시 해당 위치로 이동.
                var pos: Int = getPosition(v.y)

                //startposition
                if (pos <= 0) {
                    pos++
                }

                if (pos in 0..mMaxPosition) {
                    val resultY = mLineHeight * pos
                    v.y = resultY.toFloat()
                    mCurEndPosition = pos
                    mEndOldY = resultY
                    setBgEnd(resultY.toFloat())
                } else {
                    v.y = (mLineHeight * mMaxPosition).toFloat()
                    mCurEndPosition = mMaxPosition
                    mEndOldY = v.y.toInt()
                    setBgEnd(v.y)
                }
            }

            if (mCurEndPosition - mCurStartPosition <= 1) {
                val tempStartPosition = mCurEndPosition - 1

                if (0 <= tempStartPosition) {
                    mCurStartPosition = tempStartPosition
                    val resultY = mLineHeight * mCurStartPosition

                    setAniLine(binding.clStart, resultY)
                    setAniBgStart(resultY.toFloat())
                    mStartOldY = resultY
                } else {
                    binding.clStart.y = 0f
                    mCurStartPosition = 0
                    mStartOldY = 0
                    setBgStart(0f)
                    mCurEndPosition = 2
                    val resultY = mLineHeight * mCurEndPosition
                    binding.clEnd.y = resultY.toFloat()
                    mEndOldY = resultY
                    setBgEnd(resultY.toFloat())
                }
            } else {
                //start 보정
                val startPos: Int = getPosition(binding.clStart.y)
                val resultY = mLineHeight * startPos
                binding.clStart.y = resultY.toFloat()
                mCurStartPosition = startPos
                mStartOldY = resultY
                setBgStart(resultY.toFloat())
            }
            binding.sv.setShouldStopInterceptingTouchEvent(false)
            isStartDrag = false
            isEndDrag = false
            isActionDownEnd = false
        } else if (event.action == MotionEvent.ACTION_CANCEL) {
            binding.sv.setShouldStopInterceptingTouchEvent(false)
        }
        true
    }


    private fun correctFromStart() {

        if (mCurEndPosition - mCurStartPosition <= 1) {
            val tempEndPosition = mCurStartPosition + 1
            if (mMaxPosition >= tempEndPosition) {
                mCurEndPosition = tempEndPosition
                val resultY = mLineHeight * mCurEndPosition
                setAniLine(binding.clEnd, resultY)
                setAniBgEnd(resultY.toFloat())
                mEndOldY = resultY
            } else {
                mCurStartPosition -= 1
                val startY = mLineHeight * mCurStartPosition
                binding.clStart.y = startY.toFloat()
                mStartOldY = startY
                setBgStart(startY.toFloat())
                binding.clEnd.y = (mLineHeight * mMaxPosition).toFloat()
                mCurEndPosition = mMaxPosition
                mEndOldY = binding.clEnd.y.toInt()
                setBgEnd(binding.clEnd.y + mLineHeight_half / 2)
            }
        } else {
            //end 보정
            val endPos = getPosition(binding.clEnd.y)
            val endResultY = mLineHeight * endPos
            binding.clEnd.y = endResultY.toFloat()
            mCurEndPosition = endPos
            mEndOldY = endResultY
            setBgEnd(endResultY.toFloat())
        }
    }

    /**
     * get Index
     *
     * @param y
     * @return
     */
    private fun getPosition(y: Float): Int {
        return (y / mLineHeight).roundToInt()
    }

    fun setBgStart(y: Float) {

        binding.vSelected.updateLayoutParams<LayoutParams> {
            topMargin = y.toInt() + mLineHeight_half
        }

//        binding.vStBg.updateLayoutParams<LayoutParams> {
//            if (y > 0) {
//                this.height = y.toInt() + mLineHeight_half
//            } else {
//                this.height = 0
//            }
//        }

        val position = getPosition(y)
        val position_end = getPosition(binding.clEnd.y)
        val span = binding.tvSection.text as Spannable
        val startLine = binding.tvSection.layout.getLineStart(0)
        val endLine = binding.tvSection.layout.getLineEnd(position - 1)
        val endPosition = binding.tvSection.layout.getLineEnd(position_end - 1)

        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_707070)), startLine, endLine, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        val spansToRemove = span.getSpans(endLine, endPosition, ForegroundColorSpan::class.java)
        for (spans in spansToRemove) {
            span.removeSpan(spans)
        }
    }


    fun setBgEnd(y: Float) {

        binding.vSelected.updateLayoutParams<LayoutParams> {
            bottomMargin = (mMaxHeight - y.toInt())
        }

//        binding.vEnBg.updateLayoutParams<LayoutParams> {
//            DLogger.d("~~~~vEnBgvEnBg=>  ${y}")
//            if (y > 0) {
//                this.height = mMaxHeight - y.toInt()
//            } else {
//                this.height = 0
//            }
//        }

        val position = getPosition(y)
        val maxPosition = getPosition(mMaxHeight.toFloat()) - 1

        if (position > maxPosition) {
            return
        }

        val span = binding.tvSection.text as Spannable
        val startPos = binding.tvSection.layout.getLineStart(getPosition(binding.clStart.y))
        val endPos = binding.tvSection.layout.getLineStart(position)
        val lineCount = binding.tvSection.layout.getLineEnd(binding.tvSection.lineCount - 1)

        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_707070)), endPos, lineCount, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        if (startPos != endPos) {
            val spansToRemove = span.getSpans(startPos, endPos, ForegroundColorSpan::class.java)
            for (spans in spansToRemove) {
                span.removeSpan(spans)
            }
        }

    }

    fun setAniLine(view: ConstraintLayout, y: Int) {
        view.animate().x(0f).y(y.toFloat()).setDuration(300).start()
    }

    fun setAniBgEnd(y: Float) {

        val newBottomMargin = mMaxHeight - y.toInt()

        binding.vSelected.updateLayoutParams<LayoutParams> {
            val animator = ValueAnimator.ofInt(bottomMargin, newBottomMargin)
            animator.addUpdateListener { valueAnimator: ValueAnimator ->
                bottomMargin = (valueAnimator.animatedValue as Int)
                binding.vSelected.requestLayout()
            }
            animator.duration = 300
            animator.start()
        }

        setBgEnd(y)
    }

    fun setAniBgStart(y: Float) {
        val newTopMargin = (y.toInt() + mLineHeight_half)
        binding.vSelected.updateLayoutParams<LayoutParams> {
            val animator = ValueAnimator.ofInt(topMargin, newTopMargin)
            animator.addUpdateListener { valueAnimator: ValueAnimator ->
                topMargin = (valueAnimator.animatedValue as Int)
                binding.vSelected.requestLayout()
            }
            animator.duration = 300
            animator.start()
        }
        setBgStart(y)
    }

    fun getSectionIndexInfo(): Pair<Int, Int> {

        _lyricsData.value?.let {
            DLogger.d(TAG, "SECTION 구간 완료 Index-> ${mCurStartPosition} / ${mCurEndPosition - 1}")
            DLogger.d(TAG, "SECTION 구간 완료 가사-> ${it.sections[0].lyrics[mCurStartPosition].realText} / ${it.sections[0].lyrics[mCurEndPosition - 1].realText}")
        }
        return mCurStartPosition to mCurEndPosition - 1
    }

    fun getSectionTimeInfo(): Pair<Int, Int> {
        _lyricsData.value?.let {
            return getStartEventTime(it) to getEndEventTime(it)
        } ?: run {
            return Pair(0, 0)
        }
    }

    fun getFirstLyricsFirstEvent(): Int {
        return _lyricsData.value?.let {
            it.sections[0].lyrics[mCurStartPosition].events.first().eventTime
        } ?: 0
    }

    fun getFirstLyricsLastEvent(): Int {
        return _lyricsData.value?.let {
            it.sections[0].lyrics[0].events.last().duration + it.sections[0].lyrics[0].events.last().eventTime
        } ?: 0
    }

    fun getLastEventTime(): Int {
        return _lyricsData.value?.let {
            it.events.last().eventTime
        } ?: 0
    }

    /**
     *  시작이 0이면 데이터 그대로
     *  시작이 0이고 3초 이하면 그대로
     *  시작이 0이고 3초 이상이면 3초 앞당김.
     */
    fun getStartEventTime(xtfDto: XTF_DTO): Int {
        if (mCurStartPosition <= 0) {
            return 0
        } else {
            return if (xtfDto.sections[0].lyrics[mCurStartPosition].events[0].eventTime < START_END_PLUS_MS) {
                0
            } else {
                xtfDto.sections[0].lyrics[mCurStartPosition].events[0].eventTime - START_END_PLUS_MS
            }
        }
    }

    /**
     * 마지막이면 그대로
     * 마지막이 아니면 +3초
     */
    fun getEndEventTime(xtfDto: XTF_DTO): Int {

        val endTime = xtfDto.sections[0].lyrics[mCurEndPosition - 1].events[xtfDto.sections[0].lyrics[mCurEndPosition - 1].events.size - 1].eventTime + xtfDto.sections[0].lyrics[mCurEndPosition - 1].events[xtfDto.sections[0].lyrics[mCurEndPosition - 1].events.size - 1].duration

        return if (mCurEndPosition < xtfDto.sections[0].lyrics.size) {
            endTime + START_END_PLUS_MS
        } else {
            endTime
        }
    }

    fun getEndPlusMs(): Int {
        return START_END_PLUS_MS
    }
}

