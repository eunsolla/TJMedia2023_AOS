package com.verse.app.widget.indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.*
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.extension.dp
import com.verse.app.utility.DLogger

/**
 * Description : Base IndicatorView.
 *
 * Created by jhlee on 2023-03-24
 */
abstract class BaseFragmentIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr), LifecycleOwner, LifecycleObserver {

    enum class Gravity {
        LEFT, CENTER, RIGHT
    }

    abstract fun updateIndicator()
    abstract fun onPageSelect(pos: Int)
    abstract fun onPageScroll(pos: Int, offset: Float)
    abstract fun onPageScrollState(@ViewPager2.ScrollState state: Int)
    abstract fun onIndicatorDraw(canvas: Canvas)
    abstract fun onIndicatorMeasure(measureWidth: Int, measureHeight: Int)

    open fun onCreate() {}
    open fun onResume() {}
    open fun onStop() {}
    open fun onDestroy() {}

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    @ViewPager2.ScrollState
    protected var pageState: Int = ViewPager2.SCROLL_STATE_IDLE
    var dataCnt: Int = 0
        set(value) {
            if (field != value) {
                pageState = ViewPager2.SCROLL_STATE_IDLE
                field = value
                requestLayout()
            }
        }

    var dotSize: Int = 3.dp
        set(value) {
            if (field != value) {
                pageState = ViewPager2.SCROLL_STATE_IDLE
                field = value
                requestLayout()
            }
        }

    var viewPager: ViewPager2? = null
        set(value) {
            if (value != null) {
                value.unregisterOnPageChangeCallback(pageListener)
                value.registerOnPageChangeCallback(pageListener)
            }
            field = value
        }

    protected var position = 0
    protected val backgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }
    protected val backgroundRect: RectF by lazy { RectF() }
    protected val indicatorPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }
    protected val indicatorRect: RectF by lazy { RectF() }
    protected var radius = -1F // Rounding 처리
    protected var isLoop = false
    protected var gravity: Gravity = Gravity.LEFT

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BaseIndicatorView).run {
            try {
                backgroundPaint.color = getColor(R.styleable.BaseIndicatorView_indicatorBackgroundColor, Color.GRAY)
                indicatorPaint.color = getColor(R.styleable.BaseIndicatorView_indicatorColor, Color.BLACK)
                radius = getDimension(R.styleable.BaseIndicatorView_indicatorRadius, -1F)
                dotSize = getInt(R.styleable.BaseIndicatorView_indicatorDotSize, 3.dp)
                isLoop = getBoolean(R.styleable.BaseIndicatorView_indicatorLoop, false)
                gravity = Gravity.values()[getInt(R.styleable.BaseIndicatorView_indicatorGravity, 0)]
            } catch (ex: RuntimeException) {
                DLogger.e(ex.message.toString())
            } catch (ex: UnsupportedOperationException) {
                DLogger.e(ex.message.toString())
            }
            recycle()
        }
    }

    fun updateLayout() {
        requestLayout()
    }

    private val pageListener = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            onPageSelect(position)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            // Offset 튀는 현상 방지 코드
            if (positionOffset < 1.0F) {
                onPageScroll(position, positionOffset)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            onPageScrollState(state)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode || dataCnt == 0) return
        if (canvas != null) {
            onIndicatorDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        onIndicatorMeasure(measuredWidth, measuredHeight)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onStateEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                onCreate()
            }
            Lifecycle.Event.ON_RESUME -> {
                onResume()
            }
            Lifecycle.Event.ON_STOP -> {
                onStop()
            }
            Lifecycle.Event.ON_DESTROY -> {
                onDestroy()
            }
            else -> {
            }
        }
    }

    override fun getLifecycle() = lifecycleRegistry
}