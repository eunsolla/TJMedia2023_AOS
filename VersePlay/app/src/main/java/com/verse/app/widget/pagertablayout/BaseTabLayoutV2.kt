package com.verse.app.widget.pagertablayout

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.utility.DLogger

/**
 * Description : BaseTabLayoutV2
 *
 * Created by juhongmin on 2023/05/17
 */
abstract class BaseTabLayoutV2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr), LifecycleEventObserver, LifecycleOwner {

    interface Listener {
        fun onTabClick(pos: Int, view: View)
    }

    interface TabClickListener {
        fun onTabClick(pos: Int)
    }

    abstract fun onPageSelect(pos: Int)
    abstract fun onPageScroll(pos: Int, offset: Float)
    abstract fun onPageScrollStated(@ViewPager2.ScrollState state: Int)

    enum class Type {
        FIXED, SCROLLABLE
    }

    enum class Style(val value: String) {
        NORMAL("normal"),
        MEDIUM("medium"),
        BOLD("bold")
    }

    /**
     * ViewPager2 PagerListener
     */
    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageSelect(position)
        }

        override fun onPageScrolled(
            pos: Int,
            offset: Float,
            offsetPixel: Int
        ) {
            // Offset 튀는 현상 방지
            if (offset < 1F) {
                onPageScroll(pos, offset)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            onPageScrollStated(state)
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

    class TextStyle {
        @ColorInt
        var color: Int = -1

        var style: String = ""
    }

    // [s] Attribute Set Variable
    protected var type: Type = Type.FIXED

    @StyleRes
    protected var textAppearance: Int = -1

    protected val enableStyle: TextStyle by lazy { TextStyle() }
    protected val disableStyle: TextStyle by lazy { TextStyle() }

    @Dimension
    protected val bottomLineHeight: Float

    protected val bottomLinePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }
    // [e] Attribute Set Variable

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    init {
        isFillViewport = true
        isHorizontalScrollBarEnabled = false

        context.obtainStyledAttributes(attrs, R.styleable.BaseTabLayoutV2).run {
            type = Type.values()[getInt(R.styleable.BaseTabLayoutV2_tabType, 0)]

            textAppearance = getResourceId(R.styleable.BaseTabLayoutV2_tabTextAppearance, -1)

            // Enable Setting
            enableStyle.also {
                it.color = getColor(
                    R.styleable.BaseTabLayoutV2_tabEnableTextColor,
                    ContextCompat.getColor(context, R.color.black)
                )
                it.style =
                    Style.values()[getInt(R.styleable.BaseTabLayoutV2_tabEnableTextStyle, 2)].value
            }

            // Disable Setting
            disableStyle.also {
                it.color = getColor(
                    R.styleable.BaseTabLayoutV2_tabDisableTextColor,
                    ContextCompat.getColor(context, R.color.color_424242)
                )
                it.style =
                    Style.values()[getInt(R.styleable.BaseTabLayoutV2_tabDisableTextStyle, 0)].value
            }

            bottomLineHeight =
                getDimension(R.styleable.BaseTabLayoutV2_tabBottomLineHeight, 0F)
            bottomLinePaint.color = getColor(R.styleable.BaseTabLayoutV2_tabBottomLineColor, NO_ID)

            recycle()
        }
    }

    open fun onCreate() {}
    open fun onResume() {}
    open fun onStop() {}
    open fun onDestroy() {}

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        DLogger.d("onStateChanged $event")
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

            else -> {}
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}